package io.github.yeyuhl.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.yeyuhl.backend.entity.dto.Account;
import io.github.yeyuhl.backend.entity.vo.request.ConfirmResetVO;
import io.github.yeyuhl.backend.entity.vo.request.EmailRegisterVO;
import io.github.yeyuhl.backend.entity.vo.request.EmailResetVO;
import io.github.yeyuhl.backend.mapper.AccountMapper;
import io.github.yeyuhl.backend.service.AccountService;
import io.github.yeyuhl.backend.utils.Const;
import io.github.yeyuhl.backend.utils.FlowUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 账户服务实现类
 *
 * @author yeyuhl
 * @since 2023/10/04
 */
@Service
public class AccountServiceImpl extends ServiceImpl<AccountMapper, Account> implements AccountService {

    @Value("${spring.web.verify.mail-limit}")
    int verifyLimit;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    AmqpTemplate rabbitTemplate;

    @Autowired
    FlowUtils flowUtils;


    /**
     * 从数据库中通过用户名或者邮箱查找用户详细信息
     *
     * @param username 用户名或者邮箱
     * @return 用户详细信息
     * @throws UsernameNotFoundException 如果用户未找到则抛出此异常
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = this.findAccountByNameOrEmail(username);
        if (account == null) {
            throw new UsernameNotFoundException("用户名或密码错误");
        }
        return User
                // 不能使用account的username，因为这个username是用户输入的，可能是邮箱
                .withUsername(username)
                .password(account.getPassword())
                .roles(account.getRole())
                .build();
    }

    @Override
    public String registerEmailVerifyCode(String type, String email, String address) {
        // String的intern方法会将字符串放入常量池中，如果常量池中已经存在该字符串，则返回常量池中的字符串
        synchronized (address.intern()) {
            if (!this.verifyLimit(address)) {
                return "请求过于频繁，请稍后再试";
            }
            Random random = new Random();
            int code = random.nextInt(899999) + 100000;
            Map<String, Object> data = Map.of("type", type, "email", email, "code", code);
            rabbitTemplate.convertAndSend(Const.MQ_MAIL, data);
            redisTemplate.opsForValue().set(Const.VERIFY_EMAIL_DATA + email, String.valueOf(code), 5, TimeUnit.MINUTES);
            return null;
        }
    }

    @Override
    public String registerEmailAccount(EmailRegisterVO info) {
        String email = info.getEmail();
        String code = this.getEmailVerifyCode(email);
        if (code == null) {
            return "请先获取验证码";
        }
        if (!code.equals(info.getCode())) {
            return "验证码错误，请重新输入";
        }
        if (this.existsAccountByEmail(email)) {
            return "该邮箱地址已被注册";
        }
        String password = passwordEncoder.encode(info.getPassword());
        Account account = new Account(null, info.getUsername(), password, email, Const.ROLE_DEFAULT, new Date());
        if (!this.save(account)) {
            return "注册失败，请稍后再试";
        } else {
            this.deleteEmailVerifyCode(email);
            return null;
        }
    }

    @Override
    public String resetEmailAccountPassword(EmailResetVO info) {
        String verify = resetConfirm(new ConfirmResetVO(info.getEmail(), info.getCode()));
        if (verify != null) {
            return verify;
        }
        String email = info.getEmail();
        String password = passwordEncoder.encode(info.getPassword());
        boolean update = this.update().eq("email", email).set("password", password).update();
        if (update) {
            this.deleteEmailVerifyCode(email);
        }
        return update ? null : "重置密码失败，请稍后再试";
    }

    @Override
    public String resetConfirm(ConfirmResetVO info) {
        String email = info.getEmail();
        if (!this.existsAccountByEmail(email)) {
            return "该邮箱地址未注册";
        }
        String code = this.getEmailVerifyCode(email);
        if (code == null) {
            return "请先获取验证码";
        }
        if (!code.equals(info.getCode())) {
            return "验证码错误，请重新输入";
        }
        return null;
    }

    public Account findAccountByNameOrEmail(String text) {
        // 比较text与username或者email是否相等
        return this.query()
                .eq("username", text)
                .or()
                .eq("email", text)
                .one();
    }


    /**
     * 针对IP地址进行获取邮件验证码的限流
     *
     * @param address IP地址
     * @return 是否通过限流检查
     */
    private boolean verifyLimit(String address) {
        String key = Const.VERIFY_EMAIL_LIMIT + address;
        return flowUtils.limitOnceCheck(key, verifyLimit);
    }

    /**
     * 从Redis中获取邮件验证码
     *
     * @param email 邮箱地址
     * @return 验证码
     */
    private String getEmailVerifyCode(String email) {
        return redisTemplate.opsForValue().get(Const.VERIFY_EMAIL_DATA + email);
    }

    /**
     * 检查邮箱是否已经被注册
     *
     * @param email 邮箱地址
     * @return 是否已经被注册
     */
    private boolean existsAccountByEmail(String email) {
        return this.query().eq("email", email).exists();
    }

    /**
     * 删除Redis中的邮件验证码
     *
     * @param email 邮箱地址
     */
    private void deleteEmailVerifyCode(String email) {
        redisTemplate.delete(Const.VERIFY_EMAIL_DATA + email);
    }

}

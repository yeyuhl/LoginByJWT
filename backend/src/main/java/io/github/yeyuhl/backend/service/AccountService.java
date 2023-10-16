package io.github.yeyuhl.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.github.yeyuhl.backend.entity.dto.Account;
import io.github.yeyuhl.backend.entity.vo.request.ConfirmResetVO;
import io.github.yeyuhl.backend.entity.vo.request.EmailRegisterVO;
import io.github.yeyuhl.backend.entity.vo.request.EmailResetVO;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * 账户服务接口
 *
 * @author yeyuhl
 * @since 2023/10/04
 */
public interface AccountService extends IService<Account>, UserDetailsService {
    /**
     * 通过用户名或者邮箱地址查找用户
     *
     * @param text 用户名或邮件
     * @return 账户实体
     */
    Account findAccountByNameOrEmail(String text);

    /**
     * 生成注册验证码并放入Redis中，将邮件发送请求提交到消息队列等待发送
     *
     * @param type    类型
     * @param email   邮件地址
     * @param address 请求IP地址
     * @return 操作结果，null表示正常，否则为错误原因
     */
    String registerEmailVerifyCode(String type, String email, String address);

    /**
     * 使用邮件验证码注册帐号操作，需要检查验证码是否正确以及邮箱，用户名是否重名
     *
     * @param info 注册所需的基本信息
     * @return 操作结果，null表示正常，否则为错误原因
     */
    String registerEmailAccount(EmailRegisterVO info);

    /**
     * 邮件验证码重置密码操作，需要检查验证码是否正确
     *
     * @param info 重置基本信息
     * @return 操作结果，null表示正常，否则为错误原因
     */
    String resetEmailAccountPassword(EmailResetVO info);

    /**
     * 重置密码确认操作，校验验证码是否正确
     *
     * @param info 验证基本信息
     * @return 操作结果，null表示正常，否则为错误原因
     */
    String resetConfirm(ConfirmResetVO info);
}

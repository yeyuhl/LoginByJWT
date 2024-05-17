package io.github.yeyuhl.backend.listener;

import jakarta.annotation.Resource;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 处理邮件发送的消息队列监听器
 *
 * @author yeyuhl
 * @since 2023/10/14
 */
@Component
@RabbitListener(queues = "mail")
public class MailQueueListener {
    @Resource
    JavaMailSender sender;

    @Value("${spring.mail.username}")
    String username;

    /**
     * 处理邮件发送
     *
     * @param data 邮件数据
     */
    @RabbitHandler
    public void sendMailMessage(Map<String, Object> data) {
        String email = data.get("email").toString();
        Integer code = (Integer) data.get("code");
        SimpleMailMessage message = switch (data.get("type").toString()) {
            case "register" -> createMessage("欢迎注册我们网站",
                    "您的邮件注册码是：" + code + "，有效时间为5分钟，为了保障您的账户安全，请勿向他人泄露验证码信息。",
                    email);
            case "reset" -> createMessage("您正在进行密码重置操作",
                    "验证码为：" + code + "有效时间为5分钟，若非本人操作，请无视该邮件。",
                    email);
            default -> null;
        };
        if (message == null) {
            return;
        }
        sender.send(message);
    }

    /**
     * 快速封装SimpleMailMessage
     *
     * @param title   标题
     * @param content 内容
     * @param email   收件人
     * @return SimpleMailMessage
     */
    private SimpleMailMessage createMessage(String title, String content, String email) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setSubject(title);
        message.setText(content);
        message.setTo(email);
        message.setFrom(username);
        return message;
    }
}

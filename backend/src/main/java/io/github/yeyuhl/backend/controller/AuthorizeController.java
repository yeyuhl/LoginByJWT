package io.github.yeyuhl.backend.controller;

import io.github.yeyuhl.backend.entity.RestBean;
import io.github.yeyuhl.backend.entity.vo.request.ConfirmResetVO;
import io.github.yeyuhl.backend.entity.vo.request.EmailRegisterVO;
import io.github.yeyuhl.backend.entity.vo.request.EmailResetVO;
import io.github.yeyuhl.backend.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.function.Supplier;

/**
 * 验证相关的controller
 * 包含用户注册，密码重置等操作
 *
 * @author yeyuhl
 * @since 2023/10/14
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/api/auth")
@Tag(name = "登录校验相关", description = "包括用户登录、注册、验证码请求等操作。")
public class AuthorizeController {
    @Autowired
    AccountService accountService;

    /**
     * 请求邮件验证码
     *
     * @param email   请求邮件
     * @param type    操作类型
     * @param request 请求
     * @return 是否请求成功
     */
    @GetMapping("/ask-code")
    @Operation(summary = "请求邮件验证码")
    public RestBean<Void> askVerifyCode(@RequestParam @Email String email,
                                        @RequestParam @Pattern(regexp = "(register|reset)") String type,
                                        HttpServletRequest request) {
        return this.messageHandle(() -> accountService.registerEmailVerifyCode(type, String.valueOf(email), request.getRemoteAddr()));
    }

    /**
     * 进行用户注册，需要先请求邮件验证码
     *
     * @param info 注册消息
     * @return 是否注册成功
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册")
    public RestBean<Void> register(@RequestBody @Valid EmailRegisterVO info) {
        return this.messageHandle(() -> accountService.registerEmailAccount(info));
    }

    /**
     * 执行密码重置确认，检查验证码是否正确
     *
     * @param info 验证信息
     * @return 是否重置成功
     */
    @PostMapping("/reset-confirm")
    @Operation(summary = "重置密码确认")
    public RestBean<Void> resetConfirm(@RequestBody @Valid ConfirmResetVO info) {
        return this.messageHandle(() -> accountService.resetConfirm(info));
    }

    /**
     * 执行密码重置操作
     *
     * @param info 密码重置信息
     * @return 是否操作成功
     */
    @PostMapping("/reset-password")
    @Operation(summary = "重置密码操作")
    public RestBean<Void> resetPassword(@RequestBody @Valid EmailResetVO info) {
        return this.messageHandle(() -> accountService.resetEmailAccountPassword(info));
    }

    /**
     * 针对返回值为String作为错误信息的方法进行统一处理
     * Supplier<T>可以用于生成值，并且不需要指定任何参数
     * 生成之后可以调用其get方法返回这个值
     *
     * @param action 具体操作
     * @param <T>    响应结果类型
     * @return 响应结果
     */
    private <T> RestBean<T> messageHandle(Supplier<String> action) {
        String message = action.get();
        if (message == null) {
            return RestBean.success();
        } else {
            return RestBean.failure(400, message);
        }
    }
}

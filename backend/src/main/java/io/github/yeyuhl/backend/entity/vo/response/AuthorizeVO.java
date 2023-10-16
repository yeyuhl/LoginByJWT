package io.github.yeyuhl.backend.entity.vo.response;

import lombok.Data;

import java.util.Date;

/**
 * 登录验证成功的用户信息响应
 *
 * @author yeyuhl
 * @since 2023/10/04
 */
@Data
public class AuthorizeVO {
    String username;
    String role;
    String token;
    Date expire;
}

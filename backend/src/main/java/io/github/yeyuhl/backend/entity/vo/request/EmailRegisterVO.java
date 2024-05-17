package io.github.yeyuhl.backend.entity.vo.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * 用户注册时的请求参数
 * @author yeyuhl
 * @since 2023/10/14
 */
@Data
public class EmailRegisterVO {
    @Email
    String email;
    @Length(max = 6, min = 6)
    String code;
    @Pattern(regexp = "^[a-zA-Z0-9\\u4e00-\\u9fa5]+$")
    @Length(max = 10, min = 1)
    String username;
    @Length(max = 20, min = 6)
    String password;
}

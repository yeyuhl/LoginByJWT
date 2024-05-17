package io.github.yeyuhl.backend.entity.vo.request;

import jakarta.validation.constraints.Email;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * 重置密码时表单实体
 *
 * @author yeyuhl
 * @since 2023/10/15
 */
@Data
public class EmailResetVO {
    @Email
    String email;
    @Length(max = 6, min = 6)
    String code;
    @Length(max = 20, min = 6)
    String password;
}

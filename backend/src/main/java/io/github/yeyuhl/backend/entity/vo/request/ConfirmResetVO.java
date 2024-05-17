package io.github.yeyuhl.backend.entity.vo.request;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * 确认重置密码时的请求参数
 *
 * @author yeyuhl
 * @since 2023/10/15
 */
@Data
@AllArgsConstructor
public class ConfirmResetVO {
    @Email
    String email;
    @Length(max = 6, min = 6)
    String code;
}

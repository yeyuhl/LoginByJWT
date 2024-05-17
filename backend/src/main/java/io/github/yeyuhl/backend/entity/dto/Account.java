package io.github.yeyuhl.backend.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.yeyuhl.backend.entity.BaseData;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

/**
 * 用户信息
 *
 * @author yeyuhl
 * @since 2023/10/05
 */
@Data
@TableName("db_account")
@AllArgsConstructor
public class Account implements BaseData {
    @TableId(type = IdType.AUTO)
    Integer id;
    String username;
    String password;
    String email;
    String role;
    Date registerTime;
}

package io.github.yeyuhl.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.yeyuhl.backend.entity.dto.Account;
import org.apache.ibatis.annotations.Mapper;

/**
 * AccountMapper
 *
 * @author yeyuhl
 * @since 2023/10/05
 */
@Mapper
public interface AccountMapper extends BaseMapper<Account> {
}

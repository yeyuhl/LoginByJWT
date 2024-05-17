package io.github.yeyuhl.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web Configuration
 * 这里只是设置一个PasswordEncoder
 * 之所以要设置这个是因为在AccountServiceImpl中需要使用这个PasswordEncoder
 * 如果放到SecurityConfiguration中，而它又引用了AccountServiceImpl，AccountServiceImpl又引用了PasswordEncoder
 * 就会出现循环依赖的问题
 *
 * @author yeyuhl
 * @since 2023/10/05
 */
@Configuration
public class WebConfiguration implements WebMvcConfigurer {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

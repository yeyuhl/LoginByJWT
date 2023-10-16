package io.github.yeyuhl.backend.config;

import io.github.yeyuhl.backend.entity.RestBean;
import io.github.yeyuhl.backend.entity.dto.Account;
import io.github.yeyuhl.backend.entity.vo.response.AuthorizeVO;
import io.github.yeyuhl.backend.filter.JwtAuthenticationFilter;
import io.github.yeyuhl.backend.filter.RequestLogFilter;
import io.github.yeyuhl.backend.service.AccountService;
import io.github.yeyuhl.backend.utils.Const;
import io.github.yeyuhl.backend.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * SpringSecurity Configuration
 *
 * @author yeyuhl
 * @since 2023/10/04
 */
@Configuration
public class SecurityConfiguration {
    @Autowired
    JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    AccountService accountService;

    @Autowired
    RequestLogFilter requestLogFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http.
                authorizeHttpRequests(conf -> conf
                        .requestMatchers("/api/auth/**", "/error").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .anyRequest().hasAnyRole(Const.ROLE_DEFAULT)
                )
                .formLogin(conf -> conf
                        .loginProcessingUrl("/api/auth/login")
                        .successHandler(this::handleProcess)
                        .failureHandler(this::handleProcess)
                        .permitAll()
                )
                .logout(conf -> conf
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessHandler(this::onLogoutSuccess)
                )
                .exceptionHandling(conf -> conf
                        .accessDeniedHandler(this::handleProcess)
                        .authenticationEntryPoint(this::handleProcess)
                )
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(conf -> conf.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(requestLogFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, RequestLogFilter.class)
                .build();
    }

    /**
     * 将多种类型的Handler整合到一个方法：
     * - 登录成功
     * - 登录失败
     * - 未登录拦截/无权限拦截
     */
    private void handleProcess(HttpServletRequest request,
                               HttpServletResponse response,
                               Object exceptionOrAuthentication) throws IOException {
        response.setContentType("application/json;charset=utf-8");
        PrintWriter writer = response.getWriter();
        if (exceptionOrAuthentication instanceof AccessDeniedException exception) {
            writer.write(RestBean.forbidden(exception.getMessage()).asJsonString());
        } else if (exceptionOrAuthentication instanceof Exception exception) {
            writer.write(RestBean.unauthorized(exception.getMessage()).asJsonString());
        } else if (exceptionOrAuthentication instanceof Authentication authentication) {
            User user = (User) authentication.getPrincipal();
            Account account = accountService.findAccountByNameOrEmail(user.getUsername());
            String jwt = jwtUtils.createJwt(user, account.getUsername(), account.getId());
            if (jwt == null) {
                writer.write(RestBean.forbidden("登录验证频繁，请稍后再试").asJsonString());
            } else {
                // 将Account转换为AuthorizeVO，只保留需要的字段
                AuthorizeVO vo = account.asViewObject(AuthorizeVO.class, v -> {
                    v.setToken(jwt);
                    v.setExpire(jwtUtils.expireTime());
                });
                // 上面的转换实际上在造轮子，熟悉原理，实际开发中直接用下面的方法即可
                // BeanUtils.copyProperties(account, vo);
                writer.write(RestBean.success(vo).asJsonString());
            }
        }
    }

    /**
     * 退出登录处理，需要将对于的JWT令牌进行失效处理
     */
    private void onLogoutSuccess(HttpServletRequest request,
                                 HttpServletResponse response,
                                 Authentication authentication) throws IOException {
        response.setContentType("application/json;charset=utf-8");
        PrintWriter writer = response.getWriter();
        String authorization = request.getHeader("Authorization");
        if (jwtUtils.invalidateJwt(authorization)) {
            writer.write(RestBean.success("退出登录成功").asJsonString());
            return;
        }
        writer.write(RestBean.failure(400, "退出登录失败").asJsonString());
    }
}

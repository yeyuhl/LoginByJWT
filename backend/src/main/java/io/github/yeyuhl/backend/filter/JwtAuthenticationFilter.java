package io.github.yeyuhl.backend.filter;

import com.auth0.jwt.interfaces.DecodedJWT;
import io.github.yeyuhl.backend.utils.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT验证过滤器
 * 对于所有请求的请求头中的JWT令牌进行验证，并将用户ID存放到请求对象属性中，方便后续使用
 *
 * @author yeyuhl
 * @since 2023/10/04
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Autowired
    JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        DecodedJWT jwt = jwtUtils.resolveJwt(request.getHeader("Authorization"));
        if (jwt != null) {
            UserDetails user = jwtUtils.toUser(jwt);
            // UsernamePasswordAuthenticationToken是用于封装用户名密码认证信息的一个类
            UsernamePasswordAuthenticationToken token
                    = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
            token.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(token);
            request.setAttribute("id", jwtUtils.toId(jwt));
        }
        filterChain.doFilter(request, response);
    }
}

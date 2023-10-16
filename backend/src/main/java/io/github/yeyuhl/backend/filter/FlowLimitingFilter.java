package io.github.yeyuhl.backend.filter;

import io.github.yeyuhl.backend.entity.RestBean;
import io.github.yeyuhl.backend.utils.Const;
import io.github.yeyuhl.backend.utils.FlowUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * 流量控制过滤器，防止用户高频请求接口，借助Redis进行限流
 * 当然可以直接用Sentinel
 *
 * @author yeyuhl
 * @since 2023/10/13
 */
@Slf4j
@Component
@Order(Const.ORDER_FLOW_LIMIT)
public class FlowLimitingFilter extends HttpFilter {
    @Autowired
    StringRedisTemplate redisTemplate;
    /**
     * 限定时间内的最大请求次数
     */
    @Value("${spring.web.flow.limit}")
    int limit;
    /**
     * 限定时间
     */
    @Value("${spring.web.flow.period}")
    int period;
    /**
     * 超过限流阈值后的封禁时间
     */
    @Value("${spring.web.flow.block}")
    int block;

    @Autowired
    FlowUtils utils;

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        String address = request.getRemoteAddr();
        if (!tryCount(address)) {
            this.writeBlockMessage(response);
        }
        else {
            chain.doFilter(request, response);
        }
    }

    /**
     * 尝试对指定IP地址请求计数，如果被限制则无法继续访问
     *
     * @param address 请求IP地址
     * @return 是否操作成功
     */
    private boolean tryCount(String address) {
        synchronized (address.intern()) {
            if (Boolean.TRUE.equals(redisTemplate.hasKey(Const.FLOW_LIMIT_BLOCK + address)))
                return false;
            String counterKey = Const.FLOW_LIMIT_COUNTER + address;
            String blockKey = Const.FLOW_LIMIT_BLOCK + address;
            return utils.limitPeriodCheck(counterKey, blockKey, block, limit, period);
        }
    }

    /**
     * 为响应编写拦截内容，提示用户操作频繁
     *
     * @param response 响应
     * @throws IOException 可能的异常
     */
    private void writeBlockMessage(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=utf-8");
        PrintWriter writer = response.getWriter();
        writer.write(RestBean.forbidden("操作频繁，请稍后再试").asJsonString());
    }
}

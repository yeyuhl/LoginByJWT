package io.github.yeyuhl.backend.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 限流工具类
 *
 * @author yeyuhl
 * @since 2023/10/13
 */
@Slf4j
@Component
public class FlowUtils {
    @Autowired
    StringRedisTemplate redisTemplate;

    /**
     * 针对单次频率的限制，请求成功后，在一定时间内不允许再次请求
     *
     * @param key       键
     * @param blockTime 限制时间
     * @return 是否通过限流检查
     */
    public boolean limitOnceCheck(String key, int blockTime) {
        return this.internalCheck(key, 1, blockTime, (overclock -> false));
    }

    /**
     * 针对单次频率的限制，请求成功后，在一定时间内不允许再次请求
     *
     * @param key         键
     * @param frequency   请求的频率
     * @param blockTime   基础限制时间
     * @param upgradeTime 升级限制时间
     * @return 是否通过限流检查
     */
    public boolean limitOnceUpgradeCheck(String key, int frequency, int blockTime, int upgradeTime) {
        return this.internalCheck(key, frequency, blockTime, (overclock -> {
            if (overclock) {
                // 如果超过限制频率，则将限制时间升级
                redisTemplate.opsForValue().set(key, "1", upgradeTime, TimeUnit.SECONDS);
            }
            return false;
        }));
    }

    /**
     * 针对一段时间内的多次请求的限制，如果超过限制频率，则封禁一段时间
     *
     * @param counterKey 计数key
     * @param blockKey   封禁key
     * @param blockTime  封禁时间
     * @param frequency  请求频率
     * @param period     时间周期
     * @return 是否通过限流检查
     */
    public boolean limitPeriodCheck(String counterKey, String blockKey, int blockTime, int frequency, int period) {
        return this.internalCheck(counterKey, frequency, period, (overclock -> {
            if (overclock) {
                // 如果超过限制频率，则将限制时间升级
                redisTemplate.opsForValue().set(blockKey, "", blockTime, TimeUnit.SECONDS);
            }
            return !overclock;
        }));
    }

    /**
     * 内部使用请求限制的主要逻辑
     *
     * @param key       计数key
     * @param frequency 请求的频率
     * @param period    时间周期
     * @param action    限制行为与策略
     * @return 是否通过限流检查
     */
    private boolean internalCheck(String key, int frequency, int period, LimitAction action) {
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            // 如果key存在，其value+1，并且其value超过frequency则采取限流策略
            Long value = Optional.ofNullable(redisTemplate.opsForValue().increment(key)).orElse(0L);
            return action.run(value > frequency);
        } else {
            // 如果key不存在，说明该请求不在blockTime中，设置key的过期时间（也就是计数周期）
            redisTemplate.opsForValue().set(key, "1", period, TimeUnit.SECONDS);
            return true;
        }
    }

    /**
     * 内部使用，限制行为与策略
     */
    private interface LimitAction {
        boolean run(boolean overclock);
    }
}

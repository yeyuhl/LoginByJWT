package io.github.yeyuhl.backend.entity;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import org.slf4j.MDC;

import java.util.Optional;

/**
 * 响应实体类封装，基于Rest风格
 *
 * @author yeyuhl
 * @since 2023/10/04
 */
public record RestBean<T>(long id, int code, T data, String message) {
    public static <T> RestBean<T> success(T data) {
        return new RestBean<>(requestId(), 200, data, "请求成功");
    }

    public static <T> RestBean<T> success() {
        return success(null);
    }

    public static <T> RestBean<T> forbidden(String message) {
        return failure(403, message);
    }

    public static <T> RestBean<T> unauthorized(String message) {
        return failure(401, message);
    }

    public static <T> RestBean<T> failure(int code, String message) {
        return new RestBean<>(requestId(), code, null, message);
    }

    /**
     * 快速将当前实体类转换为JSON字符串格式
     */
    public String asJsonString() {
        // JSONWriter.Feature.WriteNulls是一个JSONWriter类的枚举类型
        // 表示是否将null值写入JSON字符串。如果设置为true，则null值将写入JSON字符串
        return JSONObject.toJSONString(this, JSONWriter.Feature.WriteNulls);
    }

    /**
     * 获取当前请求ID以便快速定位错误
     */
    private static long requestId() {
        // MDC是Mapped Diagnostic Context的缩写，它是一个线程安全的哈希表，用于存储诊断信息
        // 在日志记录中，MDC可以用于添加自定义信息，以便在日志中区分不同来源的日志记录
        String requestId = Optional.ofNullable(MDC.get("reqId")).orElse("0");
        return Long.parseLong(requestId);
    }
}

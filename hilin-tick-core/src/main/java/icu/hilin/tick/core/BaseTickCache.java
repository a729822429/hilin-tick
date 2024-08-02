package icu.hilin.tick.core;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.concurrent.TimeUnit;

public class BaseTickCache {

    private static final int DEFAULT_INIT_SIZE = 1000;
    private static final int DEFAULT_MAX_SIZE = Integer.MAX_VALUE;

    public static <T> Cache<String, T> createCache(int initSize, int maxSize) {
        return createCache(initSize, maxSize, 0);
    }

    public static <T> Cache<String, T> createCache(int expireAfterWrite) {
        return createCache(DEFAULT_INIT_SIZE, DEFAULT_MAX_SIZE, expireAfterWrite);
    }

    public static <T> Cache<String, T> createCache() {
        return createCache(DEFAULT_INIT_SIZE, DEFAULT_MAX_SIZE, 0);
    }


    /**
     * @param initSize         初始化缓存条数
     * @param maxSize          最大缓存条数
     * @param expireAfterWrite 写入之后，超过这个时间过期，单位毫秒。0表示默认永不过期
     * @param <T>              value类型
     */
    public static <T> Cache<String, T> createCache(int initSize, int maxSize, int expireAfterWrite) {
        Caffeine<Object, Object> caffeine = Caffeine.newBuilder()
                // 初始的缓存空间大小
                .initialCapacity(initSize)
                // 缓存的最大条数
                .maximumSize(maxSize);
        if (expireAfterWrite > 0) {
            caffeine.expireAfterWrite(expireAfterWrite, TimeUnit.MILLISECONDS);
        }
        return caffeine.build();
    }

}

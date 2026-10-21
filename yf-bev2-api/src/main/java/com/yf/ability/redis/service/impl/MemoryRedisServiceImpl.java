package com.yf.ability.redis.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yf.ability.redis.service.RedisService;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 内存版 Redis 实现（本地开发无 Redis 时使用）
 * 仅 @Profile("dev") 生效，生产仍用 RedisServiceImpl
 *
 * @author keryo
 */
@Log4j2
@Service
@Profile("dev")
public class MemoryRedisServiceImpl implements RedisService {

    private final ConcurrentHashMap<String, Object> store = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> expirations = new ConcurrentHashMap<>();

    private Object getValue(String key) {
        Long expiresAt = expirations.get(key);
        if (expiresAt != null && expiresAt <= System.currentTimeMillis()) {
            store.remove(key);
            expirations.remove(key, expiresAt);
            return null;
        }
        return store.get(key);
    }

    private void putValue(String key, Object value) {
        store.put(key, value);
        expirations.remove(key);
    }

    @Override
    public boolean tryLock(String key, Long ms, int tryCount, long tryWait) {
        getValue(key);
        boolean acquired = store.putIfAbsent(key, "LOCK") == null;
        if (acquired) {
            setExpiration(key, ms);
        }
        return acquired;
    }

    @Override
    public boolean tryLock(String key, Long ms) {
        getValue(key);
        boolean acquired = store.putIfAbsent(key, "LOCK") == null;
        if (acquired) {
            setExpiration(key, ms);
        }
        return acquired;
    }

    @Override
    public void unlock(String key) {
        store.remove(key);
        expirations.remove(key);
    }

    @Override
    public void del(String... keys) {
        for (String key : keys) {
            store.remove(key);
            expirations.remove(key);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> getJson(String key) {
        Object v = getValue(key);
        if (v instanceof Map) return (Map<String, Object>) v;
        if (v instanceof String) {
            try {
                return new ObjectMapper().readValue((String) v, Map.class);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    @Override
    public String getString(String key) {
        Object v = getValue(key);
        return v == null ? null : String.valueOf(v);
    }

    @Override
    public boolean set(String key, String data) {
        putValue(key, data);
        return true;
    }

    @Override
    public boolean set(String key, String value, Long time) {
        store.put(key, value);
        setExpiration(key, time == null ? null : time * 1000L);
        return true;
    }

    private void setExpiration(String key, Long durationMillis) {
        if (durationMillis != null && durationMillis > 0) {
            expirations.put(key, System.currentTimeMillis() + durationMillis);
        } else {
            expirations.remove(key);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean putList(String key, String value) {
        Object existing = getValue(key);
        List<String> list;
        if (existing instanceof List) {
            list = (List<String>) existing;
        } else {
            list = new ArrayList<>();
            putValue(key, list);
        }
        if (!list.contains(value)) {
            list.add(value);
        }
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean removeList(String key, String value) {
        Object v = getValue(key);
        if (v instanceof List) {
            ((List<String>) v).remove(value);
        }
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> findList(String key) {
        Object v = getValue(key);
        return v instanceof List ? (List<String>) v : new ArrayList<>();
    }

    @Override
    public boolean hasKey(String key) {
        return getValue(key) != null;
    }
}


package com.yf.ability.redis.service.impl;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class MemoryRedisServiceImplTest {

    @Test
    public void setWithTtlExpiresAfterTheConfiguredSeconds() throws Exception {
        MemoryRedisServiceImpl redis = new MemoryRedisServiceImpl();

        redis.set("login:locked:admin", "1", 1L);
        assertEquals("1", redis.getString("login:locked:admin"));

        Thread.sleep(1100L);

        assertNull(redis.getString("login:locked:admin"));
    }
}


package com.yf.ability.shiro.jwt;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/** JWT 必须验证签名，不能只依赖可伪造的 decode 结果。 */
public class JwtUtilsTest {

    @Before
    public void configureSecret() {
        JwtUtils.configure("unit-test-jwt-secret-which-is-long-enough-123456");
    }

    @Test
    public void validToken_isVerifiedAndUsernameCanBeRead() {
        String token = JwtUtils.sign("nurse-1", 60);

        assertEquals("nurse-1", JwtUtils.getUsername(token));
        assertTrue(JwtUtils.verify(token, "nurse-1"));
        assertFalse(JwtUtils.expired(token));
    }

    @Test
    public void tamperedToken_isRejectedEvenWhenPayloadStillDecodes() {
        String token = JwtUtils.sign("nurse-1", 60);
        int signatureStart = token.lastIndexOf('.') + 1;
        char original = token.charAt(signatureStart);
        char replacement = original == 'a' ? 'b' : 'a';
        String tampered = token.substring(0, signatureStart)
                + replacement + token.substring(signatureStart + 1);

        assertFalse(JwtUtils.verify(tampered, "nurse-1"));
        try {
            JwtUtils.getUsername(tampered);
        } catch (Exception expected) {
            return;
        }
        throw new AssertionError("篡改签名的 JWT 不得解析为认证身份");
    }

    @Test
    public void refreshMarker_isPartOfSignedUsernameClaim() {
        String token = JwtUtils.sign("nurse-1:refresh", 60);

        assertEquals("nurse-1:refresh", JwtUtils.getUsername(token));
        assertTrue(JwtUtils.verify(token, "nurse-1:refresh"));
    }
}


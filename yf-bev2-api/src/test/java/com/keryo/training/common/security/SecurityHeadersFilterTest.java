package com.keryo.training.common.security;

import jakarta.servlet.FilterChain;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

public class SecurityHeadersFilterTest {

    private SecurityHeadersFilter filter;

    @Before
    public void setUp() throws Exception {
        filter = new SecurityHeadersFilter();
        Field field = SecurityHeadersFilter.class.getDeclaredField("configuredAllowedOrigins");
        field.setAccessible(true);
        field.set(filter, "http://localhost:8000");
    }

    @Test
    public void originContainingAllowedHostIsRejected() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/sys/user/login");
        request.setServerName("localhost");
        request.setServerPort(8080);
        request.addHeader("Origin", "http://localhost.evil.example:8000");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertEquals(403, response.getStatus());
        verifyNoInteractions(chain);
    }

    @Test
    public void configuredOriginIsAllowed() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/sys/user/login");
        request.setServerName("localhost");
        request.setServerPort(8080);
        request.addHeader("Origin", "http://localhost:8000");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }
}


package org.study.redis.config.filter;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;
import org.study.redis.service.redis.RedisService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class RedisAuthenticationFilter extends GenericFilterBean {

    private final RedisService redisService;

    public RedisAuthenticationFilter(RedisService redisService) {
        this.redisService = redisService;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws
            IOException,
            ServletException {

        String sessionId = ((HttpServletRequest)servletRequest).getSession().getId();

        if (StringUtils.isBlank(sessionId)) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        SecurityContext securityContext = null;

        Object result = redisService.getValuesForHash(sessionId, "sessionAttr:SPRING_SECURITY_CONTEXT");

        if (result != null) {
            securityContext = (SecurityContext)result;
        }

        if (securityContext == null) {
            filterChain.doFilter(servletRequest, servletResponse);
        } else {
            log.info("security context : {}", securityContext);
            SecurityContextHolder.getContext().setAuthentication(securityContext.getAuthentication());
            filterChain.doFilter(servletRequest, servletResponse);
        }

    }

}

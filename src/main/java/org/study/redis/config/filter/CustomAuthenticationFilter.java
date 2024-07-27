package org.study.redis.config.filter;

import java.io.IOException;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.study.redis.service.redis.RedisService;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CustomAuthenticationFilter implements Filter {

    private final RedisService redisService;

    public CustomAuthenticationFilter(RedisService redisService) {
        this.redisService = redisService;
    }

    /**
     *
     * @param servletRequest
     * @param servletResponse
     * @param filterChain
     * @throws IOException
     * @throws ServletException
     */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws
            IOException,
            ServletException {

        // 이미 인증 처리된 건은 이 이상 진행할 필요가 없음
        if (SecurityContextHolder.getContext() != null &&
                SecurityContextHolder.getContext().getAuthentication() != null &&
                SecurityContextHolder.getContext().getAuthentication().isAuthenticated()) {

            doFilter(servletRequest, servletResponse, filterChain);
            return;
        }

        // session id 는 ServletRequest 가 아닌 HttpServletRequest 에 있다.
        String sessionId = ((HttpServletRequest)servletRequest).getSession().getId();

        // Redis 에 세션 아이디에 해당하는 SecurityContext 가 있는지 확인
        SecurityContext securityContext = redisService.getValues(sessionId, SecurityContext.class);

        // 이전에 인증 처리했던 SecurityContext 를
        if (SecurityContextHolder.getContext() != null && securityContext != null) {
            SecurityContextHolder.getContext().setAuthentication(securityContext.getAuthentication());
        } else {

            if (securityContext != null) {
                SecurityContextHolder.setContext(securityContext);
            } else {
                log.info("Redis 에 보관 중이던 SecurityContext 를 설정하는 중 문제 있음!!");
            }

        }

        doFilter(servletRequest, servletResponse, filterChain);

    }
}

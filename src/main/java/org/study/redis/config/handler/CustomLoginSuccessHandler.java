package org.study.redis.config.handler;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CustomLoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    /**
     *
     * @param request
     * @param response
     * @param authentication
     * @throws ServletException
     * @throws IOException
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws ServletException, IOException {

        RequestCache requestCache = new HttpSessionRequestCache();

        /*

         * SecurityContextHolder.getContext().setAuthentication(authentication); 하는 경우에는 다시 로그인 페이지로 돌아가게 된다.
         * SecurityContextHolder.getContext() 시에 SecurityContext 가 없어서인 것 같다.
         *
         * 따라서 아래와 같이 securityContext 를 생성해서 인증 처리된 authentication 를 넣어주고
         * 생성한 securityContext 를 SecurityContextHolder 에 넣어줘야 한다.
         *
         * */
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);

        // SecurityContextHolder.getContext().setAuthentication(authentication);

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            log.info("security context is null");
        } else {
            log.info("security context is " + SecurityContextHolder.getContext().getAuthentication());
        }

        SavedRequest savedRequest = requestCache.getRequest(request, response);

        String targetUrl = savedRequest.getRedirectUrl();
        clearAuthenticationAttributes(request);

        log.info("리다이렉트 ~ : {}", targetUrl);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

}

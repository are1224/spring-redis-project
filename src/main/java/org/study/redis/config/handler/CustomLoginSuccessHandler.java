package org.study.redis.config.handler;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomLoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private RequestCache requestCache = new HttpSessionRequestCache();
    private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

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

        SecurityContextHolder.getContext().setAuthentication(authentication);

        SavedRequest savedRequest = requestCache.getRequest(request, response);

        String targetUrl = savedRequest.getRedirectUrl();
        
        // redirectStrategy.sendRedirect(request, response, targetUrl);

        String newUrl = targetUrl.substring(targetUrl.lastIndexOf("/redis"));

        response.sendRedirect(newUrl);
    }
}

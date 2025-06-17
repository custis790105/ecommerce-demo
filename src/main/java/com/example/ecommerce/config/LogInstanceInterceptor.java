package com.example.ecommerce.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;

public class LogInstanceInterceptor implements HandlerInterceptor {
    private static final Logger log = LoggerFactory.getLogger(LogInstanceInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String hostname = System.getenv("HOSTNAME");
        if (hostname == null) {
            hostname = "local-dev";
        }
        log.info("Incoming request [{} {}] handled by instance: {}",request.getMethod(),request.getRequestURI(),hostname);
        return true;
    }
}

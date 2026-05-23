package org.example.attendance.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcLoggingConfig implements WebMvcConfigurer {

    private static final Logger log = LoggerFactory.getLogger("http.access");

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AccessLogInterceptor());
    }

    static class AccessLogInterceptor implements HandlerInterceptor {
        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
            request.setAttribute("_startTimeMs", System.currentTimeMillis());
            return true;
        }

        @Override
        public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
            Object st = request.getAttribute("_startTimeMs");
            long start = st instanceof Long l ? l : System.currentTimeMillis();
            long ms = Math.max(0, System.currentTimeMillis() - start);

            String method = request.getMethod();
            String uri = request.getRequestURI();
            String qs = request.getQueryString();
            String path = qs == null ? uri : (uri + "?" + qs);

            int status = response.getStatus();
            String remote = request.getRemoteAddr();

            if (ex != null) {
                log.warn("{} {} -> {} ({}ms) remote={} ex={}", method, path, status, ms, remote, ex.getClass().getSimpleName());
            } else {
                log.info("{} {} -> {} ({}ms) remote={}", method, path, status, ms, remote);
            }
        }
    }
}

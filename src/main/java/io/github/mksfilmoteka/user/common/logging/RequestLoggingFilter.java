package io.github.mksfilmoteka.user.common.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        long startTime = System.nanoTime();

        try {
            filterChain.doFilter(request, response);
        } finally {
            long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);

            if (log.isInfoEnabled()) {
                RequestLog requestLog = createRequestLog(request, response, durationMs);
                log.info(
                        "Request completed: method={}, path={}, status={}, durationMs={}",
                        requestLog.method(),
                        requestLog.path(),
                        requestLog.status(),
                        requestLog.durationMs()
                );
            }
        }
    }

    RequestLog createRequestLog(HttpServletRequest request, HttpServletResponse response, long durationMs) {
        return new RequestLog(request.getMethod(), getRequestPath(request), response.getStatus(), durationMs);
    }

    private String getRequestPath(HttpServletRequest request) {
        String queryString = request.getQueryString();
        if (queryString == null || queryString.isBlank()) {
            return request.getRequestURI();
        }
        return request.getRequestURI() + "?" + queryString;
    }

    record RequestLog(String method, String path, int status, long durationMs) {
    }
}

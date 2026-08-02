package io.github.mksfilmoteka.user.common.logging;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class RequestLoggingFilterTest {

    private final RequestLoggingFilter filter = new RequestLoggingFilter();

    @Test
    void shouldCreateRequestLogWithPathWithoutQueryString() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/users/1/film-lists");
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(200);

        RequestLoggingFilter.RequestLog requestLog = filter.createRequestLog(request, response, 100);

        assertThat(requestLog.method()).isEqualTo("GET");
        assertThat(requestLog.path()).isEqualTo("/api/v1/users/1/film-lists");
        assertThat(requestLog.status()).isEqualTo(200);
        assertThat(requestLog.durationMs()).isEqualTo(100);
    }

    @Test
    void shouldCreateRequestLogWithPathAndQueryString() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/users/1/film-lists");
        request.setQueryString("page=0&size=100");

        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(200);

        RequestLoggingFilter.RequestLog requestLog = filter.createRequestLog(request, response, 100);

        assertThat(requestLog.method()).isEqualTo("GET");
        assertThat(requestLog.path()).isEqualTo("/api/v1/users/1/film-lists?page=0&size=100");
        assertThat(requestLog.status()).isEqualTo(200);
        assertThat(requestLog.durationMs()).isEqualTo(100);
    }
}

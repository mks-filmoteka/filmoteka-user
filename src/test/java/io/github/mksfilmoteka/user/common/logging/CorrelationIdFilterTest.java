package io.github.mksfilmoteka.user.common.logging;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class CorrelationIdFilterTest {

    private final CorrelationIdFilter filter = new CorrelationIdFilter();

    @Test
    void shouldGenerateCorrelationIdWhenHeaderIsMissing() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/users/1/film-lists");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> correlationIdInsideChain = new AtomicReference<>();

        filter.doFilter(request, response, (_, _) ->
                correlationIdInsideChain.set(MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY))
        );

        String responseCorrelationId = response.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER);

        assertThat(responseCorrelationId).isNotBlank();
        if (responseCorrelationId != null) {
            assertThat(UUID.fromString(responseCorrelationId)).isNotNull();
        }
        assertThat(correlationIdInsideChain.get()).isEqualTo(responseCorrelationId);
        assertThat(MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY)).isNull();
    }

    @Test
    void shouldUseExistingCorrelationIdFromHeader() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/users/1/film-lists");
        request.addHeader(CorrelationIdFilter.CORRELATION_ID_HEADER, "test-123");

        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> correlationIdInsideChain = new AtomicReference<>();

        filter.doFilter(request, response, (_, _) ->
                correlationIdInsideChain.set(MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY))
        );

        assertThat(response.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER)).isEqualTo("test-123");
        assertThat(correlationIdInsideChain.get()).isEqualTo("test-123");
        assertThat(MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY)).isNull();
    }
}

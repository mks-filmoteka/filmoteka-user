package io.github.mksfilmoteka.user.catalog;

import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.time.Duration;

import static io.github.mksfilmoteka.user.common.logging.CorrelationIdFilter.CORRELATION_ID_HEADER;
import static io.github.mksfilmoteka.user.common.logging.CorrelationIdFilter.CORRELATION_ID_MDC_KEY;

@Configuration(proxyBeanMethods = false)
public class CatalogClientConfig {

    @Bean
    CatalogApi catalogApi(
            @Value("${app.clients.catalog.base-url}") String baseUrl,
            @Value("${app.clients.catalog.connect-timeout:2s}") Duration connectTimeout,
            @Value("${app.clients.catalog.read-timeout:5s}") Duration readTimeout) {

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeout);
        requestFactory.setReadTimeout(readTimeout);

        RestClient restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .requestInterceptor((request, body, execution) -> {
                    String correlationId = MDC.get(CORRELATION_ID_MDC_KEY);
                    if (correlationId != null) {
                        request.getHeaders().set(CORRELATION_ID_HEADER, correlationId);
                    }
                    return execution.execute(request, body);
                })
                .build();

        RestClientAdapter adapter = RestClientAdapter.create(restClient);

        HttpServiceProxyFactory factory =
                HttpServiceProxyFactory.builderFor(adapter).build();

        return factory.createClient(CatalogApi.class);
    }
}

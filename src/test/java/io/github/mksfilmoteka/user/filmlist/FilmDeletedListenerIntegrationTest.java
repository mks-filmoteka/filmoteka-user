package io.github.mksfilmoteka.user.filmlist;

import io.github.mksfilmoteka.user.config.KafkaConsumerConfig;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.kafka.autoconfigure.KafkaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = FilmDeletedListenerIntegrationTest.TestConfig.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                "spring.kafka.admin.auto-create=true",
                "spring.kafka.listener.auto-startup=true",
                "spring.kafka.consumer.auto-offset-reset=earliest"
        })
@EmbeddedKafka(partitions = 1, topics = "${app.kafka.topics.film-deleted.name}")
@DirtiesContext
class FilmDeletedListenerIntegrationTest {

    @Value("${app.kafka.topics.film-deleted.name}")
    private String filmDeletedTopic;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    @MockitoBean
    private FilmListService filmListService;

    @Test
    void shouldPublishToDltAfterThreeFailedAttempts() throws Exception {
        Long filmId = 123L;
        String key = filmId.toString();
        String dltTopic = filmDeletedTopic + ".user.dlt";

        String payload = """
                {
                  "eventId": "11111111-1111-4111-8111-111111111111",
                  "filmId": 123,
                  "posterName": null,
                  "occurredAt": "2026-09-21T10:00:00Z"
                }
                """;

        doThrow(new IllegalStateException("Simulated cleanup failure"))
                .when(filmListService).removeDeletedFilmFromAllLists(filmId);

        DefaultKafkaConsumerFactory<String, String> consumerFactory = new DefaultKafkaConsumerFactory<>(
                KafkaTestUtils.consumerProps(embeddedKafka, "film-deleted-dlt-reader", false),
                new StringDeserializer(), new StringDeserializer());

        try (Consumer<String, String> consumer = consumerFactory.createConsumer()) {
            consumer.subscribe(List.of(dltTopic));

            kafkaTemplate.send(filmDeletedTopic, key, payload).get(10, TimeUnit.SECONDS);

            ConsumerRecord<String, String> received = KafkaTestUtils.getSingleRecord(consumer, dltTopic);

            assertThat(received.key()).isEqualTo(key);
            assertThat(received.value()).isEqualTo(payload);

            verify(filmListService, times(3)).removeDeletedFilmFromAllLists(filmId);
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ImportAutoConfiguration({KafkaAutoConfiguration.class, JacksonAutoConfiguration.class})
    @Import({KafkaConsumerConfig.class, FilmDeletedListener.class})
    static class TestConfig {
    }
}

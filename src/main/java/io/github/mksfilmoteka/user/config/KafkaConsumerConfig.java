package io.github.mksfilmoteka.user.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

import java.time.Duration;

@Configuration(proxyBeanMethods = false)
public class KafkaConsumerConfig {

    @Bean
    public NewTopic filmDeletedDltTopic(@Value("${app.kafka.topics.film-deleted.name}") String filmDeletedTopic) {
        return TopicBuilder
                .name(filmDeletedTopic + ".user.dlt")
                .config(TopicConfig.RETENTION_MS_CONFIG, Long.toString(Duration.ofDays(30).toMillis()))
                .build();
    }
}

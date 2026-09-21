package io.github.mksfilmoteka.user.filmlist;

import io.github.mksfilmoteka.user.filmlist.dto.FilmDeletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class FilmDeletedListener {

    private final JsonMapper jsonMapper;
    private final FilmListService filmListService;

    @RetryableTopic(
            backOff = @BackOff(multiplier = 2.0),
            retryTopicSuffix = ".user.retry",
            dltTopicSuffix = ".user.dlt",
            autoStartDltHandler = "false"
    )
    @KafkaListener(
            topics = "${app.kafka.topics.film-deleted.name}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void onFilmDeleted(String payload) {
        FilmDeletedEvent event = jsonMapper.readValue(payload, FilmDeletedEvent.class);

        if (event == null || event.filmId() == null || event.filmId() <= 0) {
            throw new IllegalArgumentException("FilmDeletedEvent must contain a positive filmId");
        }

        filmListService.removeDeletedFilmFromAllLists(event.filmId());

        log.info("Processed film deletion eventId={}, filmId={}", event.eventId(), event.filmId());
    }
}

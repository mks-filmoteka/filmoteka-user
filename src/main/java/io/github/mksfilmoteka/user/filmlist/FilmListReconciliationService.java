package io.github.mksfilmoteka.user.filmlist;

import io.github.mksfilmoteka.user.catalog.CatalogClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Limit;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmListReconciliationService {

    private static final int BATCH_SIZE = 200;

    private final FilmListRepository filmListRepository;
    private final CatalogClient catalogClient;
    private final FilmListService filmListService;

    @Value("${app.reconciliation.enabled}")
    private boolean reconciliationEnabled;

    void reconcile() {
        long afterFilmId = 0L;

        while (true) {
            List<Long> filmIds = filmListRepository.findDistinctFilmIdsAfter(afterFilmId, Limit.of(BATCH_SIZE));
            if (filmIds.isEmpty()) {
                return;
            }
            Set<Long> missingFilmIds = catalogClient.findMissingFilmIds(Set.copyOf(filmIds));
            for (Long filmId : missingFilmIds) {
                filmListService.removeDeletedFilmFromAllLists(filmId);
            }
            afterFilmId = filmIds.getLast();
        }
    }

    @Scheduled(initialDelay = 1, fixedDelay = 10, timeUnit = TimeUnit.MINUTES)
    public void reconcileOnSchedule() {
        if (!reconciliationEnabled) {
            return;
        }
        try {
            reconcile();
        } catch (RuntimeException ex) {
            log.error("Film-list reconciliation failed; will retry on the next scheduled run", ex);
        }
    }
}

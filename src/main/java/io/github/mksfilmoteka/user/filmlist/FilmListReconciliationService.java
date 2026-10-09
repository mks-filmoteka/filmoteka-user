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

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmListReconciliationService {

    private static final int BATCH_SIZE = 200;
    private static final int GUARD_MIN_BATCH_SIZE = 10;

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
            afterFilmId = filmIds.getLast();

            if (filmIds.size() >= GUARD_MIN_BATCH_SIZE && missingFilmIds.size() * 2 > filmIds.size()) {
                log.error("Skipped film-list reconciliation batch: catalog reported {} of {} films missing",
                        missingFilmIds.size(), filmIds.size());
                continue;
            }

            for (Long filmId : missingFilmIds) {
                filmListService.removeDeletedFilmFromAllLists(filmId);
            }
        }
    }

    @Scheduled(initialDelayString = "PT10M", fixedDelayString = "PT24H")
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

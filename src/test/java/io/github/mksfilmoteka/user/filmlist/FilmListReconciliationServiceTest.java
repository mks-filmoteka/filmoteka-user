package io.github.mksfilmoteka.user.filmlist;

import io.github.mksfilmoteka.user.catalog.CatalogClient;
import io.github.mksfilmoteka.user.common.exception.ServiceUnavailableException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Limit;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FilmListReconciliationServiceTest {

    @Mock
    private FilmListRepository filmListRepository;

    @Mock
    private CatalogClient catalogClient;

    @Mock
    private FilmListService filmListService;

    @InjectMocks
    private FilmListReconciliationService reconciliationService;

    @Test
    void shouldProcessBatchesAndRemoveMissingFilms() {
        when(filmListRepository.findDistinctFilmIdsAfter(anyLong(), any(Limit.class)))
                .thenReturn(List.of(10L, 20L))
                .thenReturn(List.of(30L, 40L))
                .thenReturn(List.of());
        when(catalogClient.findMissingFilmIds(Set.of(10L, 20L))).thenReturn(Set.of(10L));
        when(catalogClient.findMissingFilmIds(Set.of(30L, 40L))).thenReturn(Set.of(40L));

        reconciliationService.reconcile();

        verify(filmListService).removeDeletedFilmFromAllLists(10L);
        verify(filmListService).removeDeletedFilmFromAllLists(40L);
        verifyNoMoreInteractions(filmListService);

        InOrder batches = inOrder(filmListRepository);

        batches.verify(filmListRepository).findDistinctFilmIdsAfter(eq(0L), any(Limit.class));
        batches.verify(filmListRepository).findDistinctFilmIdsAfter(eq(20L), any(Limit.class));
        batches.verify(filmListRepository).findDistinctFilmIdsAfter(eq(40L), any(Limit.class));
        batches.verifyNoMoreInteractions();
    }

    @Test
    void shouldStopWhenCatalogIsUnavailable() {
        when(filmListRepository.findDistinctFilmIdsAfter(anyLong(), any(Limit.class))).thenReturn(List.of(10L, 20L));
        ServiceUnavailableException failure = new ServiceUnavailableException("Catalog service is unavailable");
        when(catalogClient.findMissingFilmIds(Set.of(10L, 20L))).thenThrow(failure);

        assertThrows(ServiceUnavailableException.class, reconciliationService::reconcile);
        verifyNoInteractions(filmListService);
        verify(filmListRepository).findDistinctFilmIdsAfter(eq(0L), any(Limit.class));
        verifyNoMoreInteractions(filmListRepository);
    }
}

package io.github.mksfilmoteka.user.filmlist;

import io.github.mksfilmoteka.user.common.exception.ConflictException;
import io.github.mksfilmoteka.user.common.exception.ResourceNotFoundException;
import io.github.mksfilmoteka.user.filmlist.dto.FilmListItemRequest;
import io.github.mksfilmoteka.user.filmlist.dto.FilmListItemResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static io.github.mksfilmoteka.user.filmlist.FilmListTestData.*;
import static io.github.mksfilmoteka.user.profile.UserProfileTestData.USER_PROFILE_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FilmListItemServiceTest {

    @Mock
    private FilmListRepository filmListRepository;

    @Mock
    private FilmListItemRepository filmListItemRepository;

    @Mock
    private FilmListItemMapper filmListItemMapper;

    @InjectMocks
    private FilmListItemService filmListItemService;

    @Test
    void shouldReturnFilmListItemsByFilmListId() {
        FilmList loadedFilmList = loadedFilmList();
        List<FilmListItem> filmListItems = List.of(loadedFilmListItem());
        List<FilmListItemResponse> responses = List.of(filmListItemResponse());

        when(filmListRepository.findByIdAndUserId(LIST_ID, USER_PROFILE_ID)).thenReturn(Optional.of(loadedFilmList));
        when(filmListItemRepository.findAllByFilmListId(LIST_ID)).thenReturn(filmListItems);
        when(filmListItemMapper.filmListItemsToFilmListItemResponses(filmListItems)).thenReturn(responses);

        List<FilmListItemResponse> loadedResponses = filmListItemService.getFilmListItems(USER_PROFILE_ID, LIST_ID);

        assertThat(loadedResponses).containsExactly(filmListItemResponse());
        verify(filmListRepository).findByIdAndUserId(LIST_ID, USER_PROFILE_ID);
        verify(filmListItemRepository).findAllByFilmListId(LIST_ID);
        verify(filmListItemMapper).filmListItemsToFilmListItemResponses(filmListItems);
    }

    @Test
    void shouldThrowOnGetFilmListItemsIfFilmListDoesNotExist() {
        when(filmListRepository.findByIdAndUserId(LIST_ID, USER_PROFILE_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> filmListItemService.getFilmListItems(USER_PROFILE_ID, LIST_ID));

        verify(filmListRepository).findByIdAndUserId(LIST_ID, USER_PROFILE_ID);
        verifyNoInteractions(filmListItemRepository, filmListItemMapper);
    }

    @Test
    void shouldAddFilmListItem() {
        FilmList loadedFilmList = loadedFilmList();
        FilmListItem filmListItem = filmListItem();
        FilmListItem loadedFilmListItem = loadedFilmListItem();

        when(filmListRepository.findByIdAndUserId(LIST_ID, USER_PROFILE_ID)).thenReturn(Optional.of(loadedFilmList));
        when(filmListItemRepository.existsByFilmListIdAndFilmId(LIST_ID, FILM_ID)).thenReturn(false);
        when(filmListItemMapper.filmListItemRequestToFilmListItem(filmListItemRequest())).thenReturn(filmListItem);
        when(filmListItemRepository.save(filmListItem)).thenReturn(loadedFilmListItem);
        when(filmListItemMapper.filmListItemToFilmListItemResponse(loadedFilmListItem))
                .thenReturn(filmListItemResponse());

        FilmListItemResponse response =
                filmListItemService.addFilmListItem(USER_PROFILE_ID, LIST_ID, filmListItemRequest());

        assertThat(response).isEqualTo(filmListItemResponse());
        assertThat(filmListItem.getFilmList()).isSameAs(loadedFilmList);
        verify(filmListItemRepository).existsByFilmListIdAndFilmId(LIST_ID, FILM_ID);
        verify(filmListItemRepository).save(filmListItem);
        verify(filmListItemMapper).filmListItemToFilmListItemResponse(loadedFilmListItem);
    }

    @Test
    void shouldThrowOnAddIfFilmListDoesNotExist() {
        when(filmListRepository.findByIdAndUserId(LIST_ID, USER_PROFILE_ID)).thenReturn(Optional.empty());
        FilmListItemRequest request = filmListItemRequest();

        assertThrows(ResourceNotFoundException.class,
                () -> filmListItemService.addFilmListItem(USER_PROFILE_ID, LIST_ID, request));

        verify(filmListRepository).findByIdAndUserId(LIST_ID, USER_PROFILE_ID);
        verifyNoInteractions(filmListItemRepository, filmListItemMapper);
    }

    @Test
    void shouldThrowOnAddIfFilmAlreadyExistsInFilmList() {
        when(filmListRepository.findByIdAndUserId(LIST_ID, USER_PROFILE_ID)).thenReturn(Optional.of(loadedFilmList()));
        when(filmListItemRepository.existsByFilmListIdAndFilmId(LIST_ID, FILM_ID)).thenReturn(true);
        FilmListItemRequest request = filmListItemRequest();

        assertThrows(ConflictException.class,
                () -> filmListItemService.addFilmListItem(USER_PROFILE_ID, LIST_ID, request));

        verify(filmListItemRepository).existsByFilmListIdAndFilmId(LIST_ID, FILM_ID);
        verify(filmListItemRepository, never()).save(any());
        verifyNoInteractions(filmListItemMapper);
    }

    @Test
    void shouldDeleteFilmListItem() {
        FilmListItem loadedFilmListItem = loadedFilmListItem();
        when(filmListRepository.findByIdAndUserId(LIST_ID, USER_PROFILE_ID)).thenReturn(Optional.of(loadedFilmList()));
        when(filmListItemRepository.findByFilmListIdAndFilmId(LIST_ID, FILM_ID))
                .thenReturn(Optional.of(loadedFilmListItem));

        filmListItemService.deleteFilmListItem(USER_PROFILE_ID, LIST_ID, FILM_ID);

        verify(filmListItemRepository).delete(loadedFilmListItem);
    }

    @Test
    void shouldThrowOnDeleteIfFilmListDoesNotExist() {
        when(filmListRepository.findByIdAndUserId(LIST_ID, USER_PROFILE_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> filmListItemService.deleteFilmListItem(USER_PROFILE_ID, LIST_ID, FILM_ID));

        verify(filmListRepository).findByIdAndUserId(LIST_ID, USER_PROFILE_ID);
        verifyNoInteractions(filmListItemRepository);
    }

    @Test
    void shouldThrowOnDeleteIfFilmListItemDoesNotExist() {
        when(filmListRepository.findByIdAndUserId(LIST_ID, USER_PROFILE_ID)).thenReturn(Optional.of(loadedFilmList()));
        when(filmListItemRepository.findByFilmListIdAndFilmId(LIST_ID, FILM_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> filmListItemService.deleteFilmListItem(USER_PROFILE_ID, LIST_ID, FILM_ID));

        verify(filmListItemRepository).findByFilmListIdAndFilmId(LIST_ID, FILM_ID);
        verify(filmListItemRepository, never()).delete(any(FilmListItem.class));
    }
}

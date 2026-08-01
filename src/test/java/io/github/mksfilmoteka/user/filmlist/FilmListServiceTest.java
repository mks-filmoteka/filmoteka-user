package io.github.mksfilmoteka.user.filmlist;

import io.github.mksfilmoteka.user.common.exception.ConflictException;
import io.github.mksfilmoteka.user.common.exception.ResourceNotFoundException;
import io.github.mksfilmoteka.user.filmlist.dto.FilmListRequest;
import io.github.mksfilmoteka.user.filmlist.dto.FilmListResponse;
import io.github.mksfilmoteka.user.profile.UserProfile;
import io.github.mksfilmoteka.user.profile.UserProfileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import java.util.List;
import java.util.Optional;

import static io.github.mksfilmoteka.user.filmlist.FilmListTestData.*;
import static io.github.mksfilmoteka.user.profile.UserProfileTestData.USER_PROFILE_ID;
import static io.github.mksfilmoteka.user.profile.UserProfileTestData.loadedUserProfile;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FilmListServiceTest {

    @Mock
    private FilmListRepository filmListRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private FilmListMapper filmListMapper;

    @InjectMocks
    private FilmListService filmListService;

    @Test
    void shouldReturnFilmListsByUserId() {
        List<FilmList> filmLists = List.of(loadedFilmList());
        List<FilmListResponse> responses = List.of(filmListResponse());

        when(userProfileRepository.findById(USER_PROFILE_ID)).thenReturn(Optional.of(loadedUserProfile()));
        when(filmListRepository.findAllByUserId(USER_PROFILE_ID)).thenReturn(filmLists);
        when(filmListMapper.filmListsToFilmListResponses(filmLists)).thenReturn(responses);

        List<FilmListResponse> loadedResponses = filmListService.getFilmLists(USER_PROFILE_ID);

        assertThat(loadedResponses).containsExactly(filmListResponse());
        verify(userProfileRepository).findById(USER_PROFILE_ID);
        verify(filmListRepository).findAllByUserId(USER_PROFILE_ID);
        verify(filmListMapper).filmListsToFilmListResponses(filmLists);
    }

    @Test
    void shouldThrowOnGetFilmListsIfUserDoesNotExist() {
        when(userProfileRepository.findById(USER_PROFILE_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> filmListService.getFilmLists(USER_PROFILE_ID));

        verify(userProfileRepository).findById(USER_PROFILE_ID);
        verifyNoInteractions(filmListRepository, filmListMapper);
    }

    @Test
    void shouldFindFilmListByIdIfExists() {
        FilmList loadedFilmList = loadedFilmList();

        when(filmListRepository.findByIdAndUserId(LIST_ID, USER_PROFILE_ID)).thenReturn(Optional.of(loadedFilmList));
        when(filmListMapper.filmListToFilmListResponse(loadedFilmList)).thenReturn(filmListResponse());

        FilmListResponse response = filmListService.findById(USER_PROFILE_ID, LIST_ID);

        assertThat(response).isEqualTo(filmListResponse());
        verify(filmListRepository).findByIdAndUserId(LIST_ID, USER_PROFILE_ID);
        verify(filmListMapper).filmListToFilmListResponse(loadedFilmList);
    }

    @Test
    void shouldThrowIfDoesNotExist() {
        when(filmListRepository.findByIdAndUserId(LIST_ID, USER_PROFILE_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> filmListService.findById(USER_PROFILE_ID, LIST_ID));

        verify(filmListRepository).findByIdAndUserId(LIST_ID, USER_PROFILE_ID);
        verifyNoInteractions(filmListMapper);
    }

    @Test
    void shouldCreateFilmList() {
        UserProfile loadedUserProfile = loadedUserProfile();
        FilmList filmList = filmList();
        FilmList loadedFilmList = loadedFilmList();

        when(userProfileRepository.findById(USER_PROFILE_ID)).thenReturn(Optional.of(loadedUserProfile));
        when(filmListRepository.existsByNameIgnoreCaseAndUserId(LIST_NAME, USER_PROFILE_ID)).thenReturn(false);
        when(filmListMapper.filmListRequestToFilmList(filmListRequest())).thenReturn(filmList);
        when(filmListRepository.save(filmList)).thenReturn(loadedFilmList);
        when(filmListMapper.filmListToFilmListResponse(loadedFilmList)).thenReturn(filmListResponse());

        FilmListResponse response = filmListService.createFilmList(USER_PROFILE_ID, filmListRequest());

        assertThat(response).isEqualTo(filmListResponse());
        assertThat(filmList.getUser()).isSameAs(loadedUserProfile);
        verify(userProfileRepository).findById(USER_PROFILE_ID);
        verify(filmListRepository).existsByNameIgnoreCaseAndUserId(LIST_NAME, USER_PROFILE_ID);
        verify(filmListRepository).save(filmList);
        verify(filmListMapper).filmListToFilmListResponse(loadedFilmList);
    }

    @Test
    void shouldThrowOnCreateIfUserDoesNotExist() {
        when(userProfileRepository.findById(USER_PROFILE_ID)).thenReturn(Optional.empty());
        FilmListRequest request = filmListRequest();

        assertThrows(ResourceNotFoundException.class, () -> filmListService.createFilmList(USER_PROFILE_ID, request));

        verify(userProfileRepository).findById(USER_PROFILE_ID);
        verifyNoInteractions(filmListRepository, filmListMapper);
        verify(filmListRepository, never()).save(any());
    }

    @Test
    void shouldThrowOnCreateIfNameAlreadyExistsForUser() {
        when(userProfileRepository.findById(USER_PROFILE_ID)).thenReturn(Optional.of(loadedUserProfile()));
        when(filmListRepository.existsByNameIgnoreCaseAndUserId(LIST_NAME, USER_PROFILE_ID)).thenReturn(true);
        FilmListRequest request = filmListRequest();

        assertThrows(ConflictException.class, () -> filmListService.createFilmList(USER_PROFILE_ID, request));

        verify(filmListRepository).existsByNameIgnoreCaseAndUserId(LIST_NAME, USER_PROFILE_ID);
        verify(filmListRepository, never()).save(any());
        verifyNoInteractions(filmListMapper);
    }

    @Test
    void shouldUpdateFilmListIfExists() {
        FilmList loadedFilmList = loadedFilmList();
        FilmListRequest request = updateFilmListRequest();

        when(filmListRepository.findByIdAndUserId(LIST_ID, USER_PROFILE_ID)).thenReturn(Optional.of(loadedFilmList));
        doAnswer(updateNameOnly()).when(filmListMapper).updateFilmListRequestToFilmList(any(), any());
        when(filmListRepository.save(any(FilmList.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(filmListMapper.filmListToFilmListResponse(any(FilmList.class))).thenReturn(filmListResponse());

        FilmListResponse response = filmListService.updateFilmList(USER_PROFILE_ID, LIST_ID, request);

        assertThat(response).isEqualTo(filmListResponse());
        ArgumentCaptor<FilmList> captor = ArgumentCaptor.forClass(FilmList.class);

        verify(filmListRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo(UPDATED_LIST_NAME);
        assertThat(captor.getValue().getFilmIds()).isEqualTo(filmIds());
        verify(filmListMapper).updateFilmListRequestToFilmList(request, loadedFilmList);
        verify(filmListMapper).filmListToFilmListResponse(any(FilmList.class));
    }

    @Test
    void shouldUpdateForNamaEqualIgnoringCase() {
        FilmList filmList = loadedFilmList();
        FilmListRequest request = new FilmListRequest(LIST_NAME.toUpperCase());
        FilmListResponse expected = new FilmListResponse(LIST_ID, LIST_NAME.toUpperCase(), filmIds());
        when(filmListRepository.findByIdAndUserId(LIST_ID, USER_PROFILE_ID)).thenReturn(Optional.of(filmList));
        doAnswer(updateNameOnly()).when(filmListMapper).updateFilmListRequestToFilmList(request, filmList);
        when(filmListRepository.save(filmList)).thenReturn(filmList);
        when(filmListMapper.filmListToFilmListResponse(filmList)).thenReturn(expected);

        FilmListResponse response = filmListService.updateFilmList(USER_PROFILE_ID, LIST_ID, request);

        assertThat(response).isEqualTo(expected);

        verify(filmListRepository, never()).existsByNameIgnoreCaseAndUserId(anyString(), anyLong());
    }

    @Test
    void shouldThrowOnUpdateIfNameAlreadyExistsForUser() {
        FilmList loadedFilmList = loadedFilmList();
        FilmListRequest request = updateFilmListRequest();

        when(filmListRepository.findByIdAndUserId(LIST_ID, USER_PROFILE_ID)).thenReturn(Optional.of(loadedFilmList));
        when(filmListRepository.existsByNameIgnoreCaseAndUserId(request.name(), USER_PROFILE_ID)).thenReturn(true);

        assertThrows(ConflictException.class, () -> filmListService.updateFilmList(USER_PROFILE_ID, LIST_ID, request));

        verify(filmListRepository).existsByNameIgnoreCaseAndUserId(request.name(), USER_PROFILE_ID);
        verify(filmListRepository, never()).save(any());
        verifyNoInteractions(filmListMapper);
    }

    @Test
    void shouldThrowOnUpdateIfFilmListDoesNotExist() {
        FilmListRequest request = updateFilmListRequest();

        when(filmListRepository.findByIdAndUserId(LIST_ID, USER_PROFILE_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> filmListService.updateFilmList(USER_PROFILE_ID, LIST_ID, request));

        verify(filmListRepository, never()).existsByNameIgnoreCaseAndUserId(anyString(), anyLong());
        verify(filmListRepository, never()).save(any());
        verifyNoInteractions(filmListMapper);
    }

    @Test
    void shouldDeleteFilmList() {
        FilmList filmList = loadedFilmList();
        when(filmListRepository.findByIdAndUserId(LIST_ID, USER_PROFILE_ID)).thenReturn(Optional.of(filmList));

        filmListService.deleteFilmList(USER_PROFILE_ID, LIST_ID);

        verify(filmListRepository).delete(filmList);
    }

    @Test
    void shouldThrowOnDeleteIfDoesNotExist() {
        when(filmListRepository.findByIdAndUserId(LIST_ID, USER_PROFILE_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> filmListService.deleteFilmList(USER_PROFILE_ID, LIST_ID));

        verify(filmListRepository, never()).delete(any(FilmList.class));
    }

    @Test
    void shouldAddFilmToFilmList() {
        FilmList filmList = loadedFilmList();
        filmList.setFilmIds(filmIds(FILM_ID));

        when(filmListRepository.findByIdAndUserId(LIST_ID, USER_PROFILE_ID)).thenReturn(Optional.of(filmList));
        when(filmListRepository.save(filmList)).thenReturn(filmList);
        when(filmListMapper.filmListToFilmListResponse(filmList)).thenReturn(filmListResponse());

        FilmListResponse response = filmListService.addFilm(USER_PROFILE_ID, LIST_ID, OTHER_FILM_ID);

        assertThat(response).isEqualTo(filmListResponse());
        assertThat(filmList.getFilmIds()).containsExactlyInAnyOrder(FILM_ID, OTHER_FILM_ID);
        verify(filmListRepository).save(filmList);
        verify(filmListMapper).filmListToFilmListResponse(filmList);
    }

    @Test
    void shouldThrowOnAddFilmIfFilmListDoesNotExist() {
        when(filmListRepository.findByIdAndUserId(LIST_ID, USER_PROFILE_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> filmListService.addFilm(USER_PROFILE_ID, LIST_ID, FILM_ID));

        verify(filmListRepository, never()).save(any());
        verifyNoInteractions(filmListMapper);
    }

    @Test
    void shouldDeleteFilmFromFilmList() {
        FilmList filmList = loadedFilmList();
        when(filmListRepository.findByIdAndUserId(LIST_ID, USER_PROFILE_ID)).thenReturn(Optional.of(filmList));
        when(filmListRepository.save(filmList)).thenReturn(filmList);

        filmListService.removeFilm(USER_PROFILE_ID, LIST_ID, FILM_ID);

        assertThat(filmList.getFilmIds()).doesNotContain(FILM_ID);
        verify(filmListRepository).save(filmList);
    }

    @Test
    void shouldThrowOnRemoveFilmIfFilmListDoesNotExist() {
        when(filmListRepository.findByIdAndUserId(LIST_ID, USER_PROFILE_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> filmListService.removeFilm(USER_PROFILE_ID, LIST_ID, FILM_ID));

        verify(filmListRepository, never()).save(any());
    }

    @Test
    void shouldThrowOnRemoveFilmIfDoesNotExist() {
        FilmList filmList = loadedFilmList();
        filmList.setFilmIds(filmIds(FILM_ID));
        when(filmListRepository.findByIdAndUserId(LIST_ID, USER_PROFILE_ID)).thenReturn(Optional.of(filmList));

        assertThrows(ResourceNotFoundException.class, () ->
                filmListService.removeFilm(USER_PROFILE_ID, LIST_ID, OTHER_FILM_ID));

        verify(filmListRepository, never()).save(any(FilmList.class));
    }

    private static Answer<Void> updateNameOnly() {
        return invocation -> {
            FilmListRequest request = invocation.getArgument(0);
            FilmList filmList = invocation.getArgument(1);
            filmList.setName(request.name());
            return null;
        };
    }
}

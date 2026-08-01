package io.github.mksfilmoteka.user.filmlist;

import io.github.mksfilmoteka.user.config.RepositoryTestConfig;
import io.github.mksfilmoteka.user.profile.UserProfile;
import io.github.mksfilmoteka.user.profile.UserProfileTestData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

import static io.github.mksfilmoteka.user.filmlist.FilmListTestData.*;
import static io.github.mksfilmoteka.user.profile.UserProfileTestData.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(RepositoryTestConfig.class)
@Testcontainers(disabledWithoutDocker = true)
class FilmListRepositoryTest {

    @Autowired
    private FilmListRepository filmListRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldFindAllByUserId() {
        UserProfile savedUserProfile = entityManager.persistAndFlush(userProfile());
        FilmList savedFilmList = filmListRepository.saveAndFlush(filmList(savedUserProfile));

        List<FilmList> loadedFilmLists = filmListRepository.findAllByUserId(savedUserProfile.getId());

        assertNotNull(savedFilmList.getId());
        assertThat(loadedFilmLists).containsExactlyInAnyOrder(savedFilmList);
    }

    @Test
    void shouldFindByIdAndUserId() {
        UserProfile savedUserProfile = entityManager.persistAndFlush(userProfile());
        FilmList savedFilmList = filmListRepository.saveAndFlush(filmList(savedUserProfile));

        Optional<FilmList> loadedFilmList =
                filmListRepository.findByIdAndUserId(savedFilmList.getId(), savedUserProfile.getId());

        assertNotNull(savedFilmList.getId());
        assertTrue(loadedFilmList.isPresent());
        assertEquals(savedFilmList.getId(), loadedFilmList.get().getId());
        assertEquals(LIST_NAME, loadedFilmList.get().getName());
        assertEquals(savedUserProfile.getId(), loadedFilmList.get().getUser().getId());
        assertNotNull(loadedFilmList.get().getCreatedTs());
        assertNotNull(loadedFilmList.get().getUpdatedTs());
    }

    @Test
    void shouldPersistFilmIds() {
        UserProfile savedUserProfile = entityManager.persistAndFlush(userProfile());
        FilmList savedFilmList = filmListRepository.saveAndFlush(filmList(savedUserProfile));

        Optional<FilmList> loadedFilmList =
                filmListRepository.findByIdAndUserId(savedFilmList.getId(), savedUserProfile.getId());

        assertTrue(loadedFilmList.isPresent());
        assertThat(loadedFilmList.get().getFilmIds()).containsExactlyInAnyOrder(FILM_ID, OTHER_FILM_ID);
    }

    @Test
    void shouldNotFindFilmListOfAnotherUser() {
        UserProfile savedUserProfile = entityManager.persistAndFlush(userProfile());
        FilmList savedFilmList = filmListRepository.saveAndFlush(filmList(savedUserProfile));
        UserProfile otherUserProfile = userProfile();
        otherUserProfile.setIdentitySub("other-sub");
        otherUserProfile.setEmail("other@gmail.com");
        UserProfile savedOtherUserProfile = entityManager.persistAndFlush(otherUserProfile);

        Optional<FilmList> loadedFilmList =
                filmListRepository.findByIdAndUserId(savedFilmList.getId(), savedOtherUserProfile.getId());

        assertFalse(loadedFilmList.isPresent());
    }

    @Test
    void shouldCheckIfNameExistsIgnoringCaseForUser() {
        UserProfile savedUserProfile = entityManager.persistAndFlush(userProfile());
        filmListRepository.saveAndFlush(filmList(savedUserProfile));

        boolean exists = filmListRepository.existsByNameIgnoreCaseAndUserId("TeSt NaMe", savedUserProfile.getId());

        assertTrue(exists);
    }

    @Test
    void shouldCheckIfNameDoesNotExistForAnotherUser() {
        UserProfile savedUserProfile = entityManager.persistAndFlush(userProfile());
        UserProfile otherUserProfile = entityManager.persistAndFlush(UserProfileTestData.userProfile("other-sub", "other@gmail.com"));
        filmListRepository.saveAndFlush(filmList(savedUserProfile));

        boolean exists = filmListRepository.existsByNameIgnoreCaseAndUserId(LIST_NAME, otherUserProfile.getId());

        assertFalse(exists);
    }

    @Test
    void shouldAllowSameNameForDifferentUsers() {
        UserProfile savedUserProfile = entityManager.persistAndFlush(userProfile());
        UserProfile otherUserProfile = entityManager.persistAndFlush(UserProfileTestData.userProfile("other-sub", "other@gmail.com"));

        FilmList savedFilmList = filmListRepository.saveAndFlush(filmList(savedUserProfile));
        FilmList savedOtherUserFilmList = filmListRepository.saveAndFlush(filmList(otherUserProfile));

        assertNotNull(savedFilmList.getId());
        assertNotNull(savedOtherUserFilmList.getId());
    }

    @Test
    void shouldThrowOnDuplicateNameConflictForUserIgnoringCase() {
        UserProfile savedUserProfile = entityManager.persistAndFlush(userProfile());
        filmListRepository.saveAndFlush(filmList(savedUserProfile));

        FilmList duplicate = filmList(savedUserProfile);

        assertThrows(DataIntegrityViolationException.class, () -> filmListRepository.saveAndFlush(duplicate));
    }

    @Test
    void shouldRemoveFilmId() {
        UserProfile savedUserProfile = entityManager.persistAndFlush(userProfile());
        FilmList savedFilmList = filmListRepository.saveAndFlush(filmList(savedUserProfile));

        savedFilmList.getFilmIds().remove(FILM_ID);
        filmListRepository.saveAndFlush(savedFilmList);

        Optional<FilmList> loadedFilmList =
                filmListRepository.findByIdAndUserId(savedFilmList.getId(), savedUserProfile.getId());

        assertTrue(loadedFilmList.isPresent());
        assertThat(loadedFilmList.get().getFilmIds()).containsExactly(OTHER_FILM_ID);
    }
}

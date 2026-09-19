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

    @Test
    void shouldRemoveFilmFromAllLists() {
        UserProfile savedUserProfile = entityManager.persistAndFlush(userProfile());
        UserProfile otherUserProfile = entityManager.persistAndFlush(UserProfileTestData.userProfile("other-sub", "other@gmail.com"));
        FilmList savedFilmList = filmListRepository.saveAndFlush(filmList(savedUserProfile));
        FilmList savedOtherFilmList = filmListRepository.saveAndFlush(filmList(otherUserProfile));

        FilmList anotherFilmList = filmList(savedUserProfile);
        anotherFilmList.setName("Another list");
        FilmList savedAnotherFilmList = filmListRepository.saveAndFlush(anotherFilmList);

        FilmList unaffectedFilmList = filmList(savedUserProfile);
        unaffectedFilmList.setName("Unaffected list");
        unaffectedFilmList.setFilmIds(filmIds(OTHER_FILM_ID));
        FilmList savedUnaffectedFilmList = filmListRepository.saveAndFlush(unaffectedFilmList);

        int removedCount = filmListRepository.removeFilmFromAllLists(FILM_ID);
        entityManager.clear();

        FilmList loadedFilmList = filmListRepository.findById(savedFilmList.getId()).orElseThrow();
        FilmList loadedOtherFilmList = filmListRepository.findById(savedOtherFilmList.getId()).orElseThrow();

        FilmList loadedAnotherFilmList = filmListRepository.findById(savedAnotherFilmList.getId()).orElseThrow();
        FilmList loadedUnaffectedFilmList = filmListRepository.findById(savedUnaffectedFilmList.getId()).orElseThrow();

        assertEquals(3, removedCount);
        assertThat(loadedFilmList.getFilmIds()).containsExactly(OTHER_FILM_ID);
        assertThat(loadedOtherFilmList.getFilmIds()).containsExactly(OTHER_FILM_ID);
        assertThat(loadedAnotherFilmList.getFilmIds()).containsExactly(OTHER_FILM_ID);
        assertThat(loadedUnaffectedFilmList.getFilmIds()).containsExactly(OTHER_FILM_ID);
    }

    @Test
    void shouldIgnoreRepeatedFilmDeletion() {
        UserProfile savedUserProfile = entityManager.persistAndFlush(userProfile());
        FilmList savedFilmList = filmListRepository.saveAndFlush(filmList(savedUserProfile));

        int firstRemovedCount = filmListRepository.removeFilmFromAllLists(FILM_ID);
        entityManager.clear();
        int secondRemovedCount = filmListRepository.removeFilmFromAllLists(FILM_ID);
        entityManager.clear();

        FilmList loadedFilmList = filmListRepository.findById(savedFilmList.getId()).orElseThrow();

        assertEquals(1, firstRemovedCount);
        assertEquals(0, secondRemovedCount);
        assertThat(loadedFilmList.getFilmIds()).containsExactly(OTHER_FILM_ID);
    }

    @Test
    void shouldKeepFilmListWhenItsLastFilmIsDeleted() {
        UserProfile savedUserProfile = entityManager.persistAndFlush(userProfile());
        FilmList filmList = filmList(savedUserProfile);
        filmList.setFilmIds(filmIds(FILM_ID));
        FilmList savedFilmList = filmListRepository.saveAndFlush(filmList);

        int removedCount = filmListRepository.removeFilmFromAllLists(FILM_ID);
        entityManager.clear();

        FilmList loadedFilmList = filmListRepository.findById(savedFilmList.getId()).orElseThrow();

        assertEquals(1, removedCount);
        assertEquals(LIST_NAME, loadedFilmList.getName());
        assertThat(loadedFilmList.getFilmIds()).isEmpty();
    }

    @Test
    void shouldReturnZeroWhenFilmIsNotInAnyList() {
        UserProfile savedUserProfile = entityManager.persistAndFlush(userProfile());
        FilmList filmList = filmList(savedUserProfile);
        filmList.setFilmIds(filmIds(OTHER_FILM_ID));
        FilmList savedFilmList = filmListRepository.saveAndFlush(filmList);

        int removedCount = filmListRepository.removeFilmFromAllLists(FILM_ID);
        entityManager.clear();

        FilmList loadedFilmList = filmListRepository.findById(savedFilmList.getId()).orElseThrow();

        assertEquals(0, removedCount);
        assertThat(loadedFilmList.getFilmIds()).containsExactly(OTHER_FILM_ID);
    }
}

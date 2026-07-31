package io.github.mksfilmoteka.user.filmlist;

import io.github.mksfilmoteka.user.config.RepositoryTestConfig;
import io.github.mksfilmoteka.user.profile.UserProfile;
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
import static io.github.mksfilmoteka.user.profile.UserProfileTestData.userProfile;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(RepositoryTestConfig.class)
@Testcontainers(disabledWithoutDocker = true)
class FilmListItemRepositoryTest {

    @Autowired
    private FilmListItemRepository filmListItemRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldFindAllByFilmListId() {
        UserProfile savedUserProfile = entityManager.persistAndFlush(userProfile());
        FilmList savedFilmList = entityManager.persistAndFlush(FilmListTestData.filmList(savedUserProfile));
        FilmListItem savedFilmListItem = filmListItemRepository.saveAndFlush(filmListItem(savedFilmList, FILM_ID));

        List<FilmListItem> loadedFilmListItems = filmListItemRepository.findAllByFilmListId(savedFilmList.getId());

        assertNotNull(savedFilmListItem.getId());
        assertThat(loadedFilmListItems).containsExactlyInAnyOrder(savedFilmListItem);
    }

    @Test
    void shouldFindByFilmListIdAndFilmId() {
        UserProfile savedUserProfile = entityManager.persistAndFlush(userProfile());
        FilmList savedFilmList = entityManager.persistAndFlush(FilmListTestData.filmList(savedUserProfile));
        FilmListItem savedFilmListItem = filmListItemRepository.saveAndFlush(filmListItem(savedFilmList, FILM_ID));

        Optional<FilmListItem> loadedFilmListItem =
                filmListItemRepository.findByFilmListIdAndFilmId(savedFilmList.getId(), FILM_ID);

        assertNotNull(savedFilmListItem.getId());
        assertTrue(loadedFilmListItem.isPresent());
        assertEquals(savedFilmListItem.getId(), loadedFilmListItem.get().getId());
        assertEquals(FILM_ID, loadedFilmListItem.get().getFilmId());
        assertNotNull(loadedFilmListItem.get().getAddedTs());
    }

    @Test
    void shouldCheckIfFilmListItemExists() {
        UserProfile savedUserProfile = entityManager.persistAndFlush(userProfile());
        FilmList savedFilmList = entityManager.persistAndFlush(FilmListTestData.filmList(savedUserProfile));
        filmListItemRepository.saveAndFlush(filmListItem(savedFilmList, FILM_ID));

        boolean exists = filmListItemRepository.existsByFilmListIdAndFilmId(savedFilmList.getId(), FILM_ID);

        assertTrue(exists);
    }

    @Test
    void shouldNotFindFilmListItemOfAnotherFilmList() {
        UserProfile savedUserProfile = entityManager.persistAndFlush(userProfile());
        FilmList savedFilmList = entityManager.persistAndFlush(FilmListTestData.filmList(savedUserProfile));
        FilmList anotherFilmList =
                entityManager.persistAndFlush(FilmListTestData.filmList(savedUserProfile,"new name"));
        filmListItemRepository.saveAndFlush(filmListItem(savedFilmList, FILM_ID));

        Optional<FilmListItem> loadedFilmListItem =
                filmListItemRepository.findByFilmListIdAndFilmId(anotherFilmList.getId(), FILM_ID);

        assertFalse(loadedFilmListItem.isPresent());
    }

    @Test
    void shouldDeleteFilmListItem() {
        UserProfile savedUserProfile = entityManager.persistAndFlush(userProfile());
        FilmList savedFilmList = entityManager.persistAndFlush(FilmListTestData.filmList(savedUserProfile));
        FilmListItem filmListItem = filmListItemRepository.saveAndFlush(filmListItem(savedFilmList, FILM_ID));
        filmListItemRepository.saveAndFlush(filmListItem(savedFilmList, OTHER_FILM_ID));

        filmListItemRepository.delete(filmListItem);
        filmListItemRepository.flush();

        assertFalse(filmListItemRepository.existsByFilmListIdAndFilmId(savedFilmList.getId(), FILM_ID));
        assertTrue(filmListItemRepository.existsByFilmListIdAndFilmId(savedFilmList.getId(), OTHER_FILM_ID));
    }

    @Test
    void shouldThrowOnDuplicateFilmListItemConflict() {
        UserProfile savedUserProfile = entityManager.persistAndFlush(userProfile());
        FilmList savedFilmList = entityManager.persistAndFlush(FilmListTestData.filmList(savedUserProfile));
        filmListItemRepository.saveAndFlush(filmListItem(savedFilmList, FILM_ID));

        FilmListItem duplicate = filmListItem(savedFilmList, FILM_ID);

        assertThrows(DataIntegrityViolationException.class, () -> filmListItemRepository.saveAndFlush(duplicate));
    }
}

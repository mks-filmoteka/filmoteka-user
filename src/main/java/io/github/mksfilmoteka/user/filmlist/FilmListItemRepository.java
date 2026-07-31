package io.github.mksfilmoteka.user.filmlist;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FilmListItemRepository extends JpaRepository<FilmListItem,Long> {
    List<FilmListItem> findAllByFilmListId(Long filmListId);

    Optional<FilmListItem> findByFilmListIdAndFilmId(Long filmListId, Long filmId);

    boolean existsByFilmListIdAndFilmId(Long filmListId, Long filmId);
}

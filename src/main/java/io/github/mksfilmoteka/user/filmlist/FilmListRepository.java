package io.github.mksfilmoteka.user.filmlist;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FilmListRepository extends JpaRepository<FilmList, Long> {
    @EntityGraph(attributePaths = "filmIds")
    List<FilmList> findAllByUserId(Long userId);

    @EntityGraph(attributePaths = "filmIds")
    Optional<FilmList> findByIdAndUserId(Long id, Long userId);

    boolean existsByNameIgnoreCaseAndUserId(String name, Long userId);
}

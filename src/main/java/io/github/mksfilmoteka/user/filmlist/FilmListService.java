package io.github.mksfilmoteka.user.filmlist;

import io.github.mksfilmoteka.user.auth.AuthUser;
import io.github.mksfilmoteka.user.catalog.CatalogClient;
import io.github.mksfilmoteka.user.common.exception.ConflictException;
import io.github.mksfilmoteka.user.common.exception.ResourceNotFoundException;
import io.github.mksfilmoteka.user.filmlist.dto.FilmListRequest;
import io.github.mksfilmoteka.user.filmlist.dto.FilmListResponse;
import io.github.mksfilmoteka.user.filmlist.dto.ListedFilmsRequest;
import io.github.mksfilmoteka.user.profile.UserProfile;
import io.github.mksfilmoteka.user.profile.UserProfileProvisionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FilmListService {

    private final FilmListRepository filmListRepository;
    private final FilmListMapper filmListMapper;
    private final UserProfileProvisionService userProfileProvisionService;
    private final CatalogClient catalogClient;

    public List<FilmListResponse> getFilmLists(AuthUser authUser) {
        UserProfile userProfile = userProfileProvisionService.getOrCreate(authUser);
        Long userId = userProfile.getId();

        log.debug("Searching film lists. userId={}", userId);

        List<FilmList> filmLists = filmListRepository.findAllByUserId(userId);
        return filmListMapper.filmListsToFilmListResponses(filmLists);
    }

    public FilmListResponse findById(AuthUser authUser, Long id) {
        Long userId = getUserId(authUser);

        FilmList filmList = getFilmListOrThrow(userId, id);
        return filmListMapper.filmListToFilmListResponse(filmList);
    }

    public FilmListResponse createFilmList(AuthUser authUser, FilmListRequest request) {
        UserProfile userProfile = userProfileProvisionService.getOrCreate(authUser);
        Long userId = userProfile.getId();

        if (filmListRepository.existsByNameIgnoreCaseAndUserId(request.name(), userId)) {
            throw new ConflictException("Film list with name '" + request.name() + "' already exists");
        }

        FilmList filmList = filmListMapper.filmListRequestToFilmList(request);
        filmList.setUser(userProfile);

        FilmList saved = filmListRepository.save(filmList);
        log.info("Created film list id={}, userId={}, name={}", saved.getId(), userId, saved.getName());

        return filmListMapper.filmListToFilmListResponse(saved);
    }

    public FilmListResponse updateFilmList(AuthUser authUser, Long id, FilmListRequest request) {
        Long userId = getUserId(authUser);
        FilmList filmList = getFilmListOrThrow(userId, id);
        if (!filmList.getName().equalsIgnoreCase(request.name())
                && filmListRepository.existsByNameIgnoreCaseAndUserId(request.name(), userId)) {
            throw new ConflictException("Film list with name '" + request.name() + "' already exists");
        }

        filmListMapper.updateFilmListRequestToFilmList(request, filmList);

        FilmList saved = filmListRepository.save(filmList);
        log.info("Updated film list id={}, userId={} with name={}", saved.getId(), userId, saved.getName());

        return filmListMapper.filmListToFilmListResponse(saved);
    }

    public void deleteFilmList(AuthUser authUser, Long id) {
        Long userId = getUserId(authUser);
        FilmList filmList = getFilmListOrThrow(userId, id);
        filmListRepository.delete(filmList);
        log.info("Deleted film list id={}, userId={}", id, userId);
    }

    public FilmListResponse addFilm(AuthUser authUser, Long id, Long filmId) {
        Long userId = getUserId(authUser);
        FilmList filmList = getFilmListOrThrow(userId, id);
        catalogClient.requireFilmExists(filmId);

        filmList.getFilmIds().add(filmId);

        FilmList saved = filmListRepository.save(filmList);
        log.info("Added film id={} to film list id={}, userId={}", filmId, saved.getId(), userId);

        return filmListMapper.filmListToFilmListResponse(saved);
    }

    public FilmListResponse patchFilms(AuthUser authUser, Long id, ListedFilmsRequest request) {
        Long userId = getUserId(authUser);
        FilmList filmList = getFilmListOrThrow(userId, id);

        Set<Long> toAdd = request.addedFilmIds() == null ? Set.of() : request.addedFilmIds();
        Set<Long> toRemove = request.removedFilmIds() == null ? Set.of() : request.removedFilmIds();
        Set<Long> filmIds = filmList.getFilmIds();

        Set<Long> filmIdsToValidate = new HashSet<>(toAdd);
        filmIdsToValidate.removeAll(filmIds);
        filmIdsToValidate.removeAll(toRemove);

        if (!filmIdsToValidate.isEmpty()) {
            catalogClient.requireFilmsExist(filmIdsToValidate);
        }

        int countBefore = filmIds.size();
        filmIds.addAll(toAdd);
        int addedCount = filmIds.size() - countBefore;

        countBefore = filmIds.size();
        filmIds.removeAll(toRemove);
        int removedCount = countBefore - filmIds.size();

        if (addedCount == 0 && removedCount == 0) {
            log.debug("No film changes applied for film list id={}, userId={}", id, userId);
            return filmListMapper.filmListToFilmListResponse(filmList);
        }

        FilmList saved = filmListRepository.save(filmList);
        log.info("Patched film list id={}, userId={}, films added={}, films removed={}",
                id, userId, addedCount, removedCount);

        return filmListMapper.filmListToFilmListResponse(saved);
    }

    public void removeFilm(AuthUser authUser, Long id, Long filmId) {
        Long userId = getUserId(authUser);
        FilmList filmList = getFilmListOrThrow(userId, id);
        if (!filmList.getFilmIds().remove(filmId)) {
            throw new ResourceNotFoundException("Film with id " + filmId + " not found in film list " + id);
        }

        filmListRepository.save(filmList);
        log.info("Removed film id={} from film list id={}, userId={}", filmId, id, userId);
    }

    private FilmList getFilmListOrThrow(Long userId, Long id) {
        return filmListRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Film list with id " + id + " not found"));
    }

    private Long getUserId(AuthUser authUser) {
        return userProfileProvisionService.getOrCreate(authUser).getId();
    }
}

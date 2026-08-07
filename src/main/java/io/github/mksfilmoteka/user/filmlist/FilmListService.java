package io.github.mksfilmoteka.user.filmlist;

import io.github.mksfilmoteka.user.auth.AuthUser;
import io.github.mksfilmoteka.user.common.exception.ConflictException;
import io.github.mksfilmoteka.user.common.exception.ResourceNotFoundException;
import io.github.mksfilmoteka.user.filmlist.dto.FilmListRequest;
import io.github.mksfilmoteka.user.filmlist.dto.FilmListResponse;
import io.github.mksfilmoteka.user.profile.UserProfile;
import io.github.mksfilmoteka.user.profile.UserProfileProvisionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FilmListService {

    private final FilmListRepository filmListRepository;
    private final FilmListMapper filmListMapper;
    private final UserProfileProvisionService userProfileProvisionService;

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

    @Transactional
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

    @Transactional
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

    @Transactional
    public void deleteFilmList(AuthUser authUser, Long id) {
        Long userId = getUserId(authUser);
        FilmList filmList = getFilmListOrThrow(userId, id);
        filmListRepository.delete(filmList);
        log.info("Deleted film list id={}, userId={}", id, userId);
    }

    @Transactional
    public FilmListResponse addFilm(AuthUser authUser, Long id, Long filmId) {
        Long userId = getUserId(authUser);
        FilmList filmList = getFilmListOrThrow(userId, id);
        filmList.getFilmIds().add(filmId);

        FilmList saved = filmListRepository.save(filmList);
        log.info("Added film id={} to film list id={}, userId={}", filmId, saved.getId(), userId);

        return filmListMapper.filmListToFilmListResponse(saved);
    }

    @Transactional
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

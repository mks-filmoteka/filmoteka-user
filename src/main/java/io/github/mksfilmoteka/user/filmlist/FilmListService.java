package io.github.mksfilmoteka.user.filmlist;

import io.github.mksfilmoteka.user.common.exception.ConflictException;
import io.github.mksfilmoteka.user.common.exception.ResourceNotFoundException;
import io.github.mksfilmoteka.user.filmlist.dto.FilmListRequest;
import io.github.mksfilmoteka.user.filmlist.dto.FilmListResponse;
import io.github.mksfilmoteka.user.profile.UserProfile;
import io.github.mksfilmoteka.user.profile.UserProfileRepository;
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
    private final UserProfileRepository userProfileRepository;
    private final FilmListMapper filmListMapper;

    public List<FilmListResponse> getFilmLists(Long userId) {
        log.debug("Searching film lists. userId={}", userId);

        getUserProfileOrThrow(userId);
        List<FilmList> filmLists = filmListRepository.findAllByUserId(userId);
        return filmListMapper.filmListsToFilmListResponses(filmLists);
    }

    public FilmListResponse findById(Long userId, Long id) {
        FilmList filmList = getFilmListOrThrow(userId, id);
        return filmListMapper.filmListToFilmListResponse(filmList);
    }

    @Transactional
    public FilmListResponse createFilmList(Long userId, FilmListRequest request) {
        UserProfile userProfile = getUserProfileOrThrow(userId);
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
    public FilmListResponse updateFilmList(Long userId, Long id, FilmListRequest request) {
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
    public void deleteFilmList(Long userId, Long id) {
        FilmList filmList = getFilmListOrThrow(userId, id);
        filmListRepository.delete(filmList);
        log.info("Deleted film list id={}, userId={}", id, userId);
    }

    @Transactional
    public FilmListResponse addFilm(Long userId, Long id, Long filmId) {
        FilmList filmList = getFilmListOrThrow(userId, id);
        filmList.getFilmIds().add(filmId);

        FilmList saved = filmListRepository.save(filmList);
        log.info("Added film id={} to film list id={}, userId={}", filmId, saved.getId(), userId);

        return filmListMapper.filmListToFilmListResponse(saved);
    }

    @Transactional
    public void removeFilm(Long userId, Long id, Long filmId) {
        FilmList filmList = getFilmListOrThrow(userId, id);
        if (!filmList.getFilmIds().remove(filmId)) {
            throw new ResourceNotFoundException("Film with id " + filmId + " not found in film list " + id);
        }

        filmListRepository.save(filmList);
        log.info("Removed film id={} from film list id={}, userId={}", filmId, id, userId);
    }

    private FilmList getFilmListOrThrow(Long userId, Long id) {
        return filmListRepository.findByIdAndUserId(id, userId).orElseThrow(() ->
                new ResourceNotFoundException("Film list with id " + id + " not found"));
    }

    private UserProfile getUserProfileOrThrow(Long userId) {
        return userProfileRepository.findById(userId).orElseThrow(() ->
                new ResourceNotFoundException("User profile with id " + userId + " not found"));
    }
}

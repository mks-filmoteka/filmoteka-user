package io.github.mksfilmoteka.user.filmlist;

import io.github.mksfilmoteka.user.common.exception.ConflictException;
import io.github.mksfilmoteka.user.common.exception.ResourceNotFoundException;
import io.github.mksfilmoteka.user.filmlist.dto.FilmListItemRequest;
import io.github.mksfilmoteka.user.filmlist.dto.FilmListItemResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FilmListItemService {

    private final FilmListRepository filmListRepository;
    private final FilmListItemRepository filmListItemRepository;
    private final FilmListItemMapper filmListItemMapper;

    public List<FilmListItemResponse> getFilmListItems(Long userId, Long filmListId) {
        getFilmListOrThrow(userId, filmListId);
        List<FilmListItem> filmListItems = filmListItemRepository.findAllByFilmListId(filmListId);
        return filmListItemMapper.filmListItemsToFilmListItemResponses(filmListItems);
    }

    @Transactional
    public FilmListItemResponse addFilmListItem(Long userId, Long filmListId, FilmListItemRequest request) {
        FilmList filmList = getFilmListOrThrow(userId, filmListId);
        if (filmListItemRepository.existsByFilmListIdAndFilmId(filmListId, request.filmId())) {
            throw new ConflictException("Film with id " + request.filmId() + " already exists in film list " + filmListId);
        }

        FilmListItem filmListItem = filmListItemMapper.filmListItemRequestToFilmListItem(request);
        filmListItem.setFilmList(filmList);

        FilmListItem saved = filmListItemRepository.save(filmListItem);
        return filmListItemMapper.filmListItemToFilmListItemResponse(saved);
    }

    @Transactional
    public void deleteFilmListItem(Long userId, Long filmListId, Long filmId) {
        getFilmListOrThrow(userId, filmListId);
        FilmListItem filmListItem = filmListItemRepository.findByFilmListIdAndFilmId(filmListId, filmId).orElseThrow(() ->
                new ResourceNotFoundException("Film with id " + filmId + " not found in film list " + filmListId));

        filmListItemRepository.delete(filmListItem);
    }

    private FilmList getFilmListOrThrow(Long userId, Long filmListId) {
        return filmListRepository.findByIdAndUserId(filmListId, userId).orElseThrow(() ->
                new ResourceNotFoundException("Film list with id " + filmListId + " not found"));
    }
}

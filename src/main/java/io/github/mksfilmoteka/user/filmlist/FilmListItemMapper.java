package io.github.mksfilmoteka.user.filmlist;

import io.github.mksfilmoteka.user.filmlist.dto.FilmListItemRequest;
import io.github.mksfilmoteka.user.filmlist.dto.FilmListItemResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FilmListItemMapper {

    FilmListItem filmListItemRequestToFilmListItem(FilmListItemRequest request);

    FilmListItemResponse filmListItemToFilmListItemResponse(FilmListItem filmListItem);

    List<FilmListItemResponse> filmListItemsToFilmListItemResponses(List<FilmListItem> filmListItems);
}

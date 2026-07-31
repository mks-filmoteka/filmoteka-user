package io.github.mksfilmoteka.user.filmlist;

import io.github.mksfilmoteka.user.filmlist.dto.FilmListRequest;
import io.github.mksfilmoteka.user.filmlist.dto.FilmListResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FilmListMapper {

    FilmList filmListRequestToFilmList(FilmListRequest request);

    void updateFilmListRequestToFilmList(FilmListRequest request, @MappingTarget FilmList filmList);

    FilmListResponse filmListToFilmListResponse(FilmList filmList);

    List<FilmListResponse> filmListsToFilmListResponses(List<FilmList> filmLists);
}

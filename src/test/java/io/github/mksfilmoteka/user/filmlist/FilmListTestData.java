package io.github.mksfilmoteka.user.filmlist;

import io.github.mksfilmoteka.user.filmlist.dto.FilmListRequest;
import io.github.mksfilmoteka.user.filmlist.dto.FilmListResponse;
import io.github.mksfilmoteka.user.profile.UserProfile;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import static io.github.mksfilmoteka.user.profile.UserProfileTestData.loadedUserProfile;

public final class FilmListTestData {
    public static final String LIST_NAME = "test name";
    public static final String UPDATED_LIST_NAME = "updated name";
    public static final long LIST_ID = 1L;
    public static final long FILM_ID = 100L;
    public static final long OTHER_FILM_ID = 200L;
    public static final String FILM_LISTS_URL = "/api/v1/film-lists";

    public static FilmList filmList() {
        FilmList filmList = new FilmList();
        filmList.setUser(loadedUserProfile());
        filmList.setName(LIST_NAME);
        filmList.setFilmIds(filmIds());
        return filmList;
    }

    public static FilmList loadedFilmList() {
        FilmList filmList = filmList();
        filmList.setId(LIST_ID);
        return filmList;
    }

    public static FilmList filmList(UserProfile userProfile) {
        FilmList filmList = filmList();
        filmList.setUser(userProfile);
        return filmList;
    }

    public static FilmListRequest filmListRequest() {
        return new FilmListRequest(LIST_NAME);
    }

    public static FilmListRequest updateFilmListRequest() {
        return new FilmListRequest(UPDATED_LIST_NAME);
    }

    public static FilmListResponse filmListResponse() {
        return new FilmListResponse(LIST_ID, LIST_NAME, filmIds());
    }

    public static Set<Long> filmIds(Long... filmIds) {
        return new LinkedHashSet<>(Arrays.asList(filmIds));
    }

    public static Set<Long> filmIds() {
        return filmIds(FILM_ID, OTHER_FILM_ID);
    }
}

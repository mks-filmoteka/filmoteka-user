package io.github.mksfilmoteka.user.filmlist;

import io.github.mksfilmoteka.user.filmlist.dto.FilmListItemRequest;
import io.github.mksfilmoteka.user.filmlist.dto.FilmListItemResponse;
import io.github.mksfilmoteka.user.filmlist.dto.FilmListRequest;
import io.github.mksfilmoteka.user.filmlist.dto.FilmListResponse;
import io.github.mksfilmoteka.user.profile.UserProfile;

import static io.github.mksfilmoteka.user.profile.UserProfileTestData.loadedUserProfile;

public final class FilmListTestData {
    public static final String LIST_NAME = "test name";
    public static final long LIST_ID = 1L;
    public static final long ITEM_ID = 10L;
    public static final long FILM_ID = 100L;
    public static final long OTHER_FILM_ID = 200L;

    public static FilmList filmList() {
        FilmList filmList = new FilmList();
        filmList.setUser(loadedUserProfile());
        filmList.setName(LIST_NAME);
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

    public static FilmList filmList(UserProfile userProfile, String listName) {
        FilmList filmList = filmList();
        filmList.setUser(userProfile);
        filmList.setName(listName);
        return filmList;
    }

    public static FilmListItem filmListItem() {
        FilmListItem filmListItem = new FilmListItem();
        filmListItem.setFilmList(filmList());
        filmListItem.setFilmId(FILM_ID);
        return filmListItem;
    }

    public static FilmListItem loadedFilmListItem() {
        FilmListItem filmListItem = filmListItem(loadedFilmList(), FILM_ID);
        filmListItem.setId(ITEM_ID);
        return filmListItem;
    }

    public static FilmListItem filmListItem(FilmList filmList, Long filmId) {
        FilmListItem filmListItem = filmListItem();
        filmListItem.setFilmList(filmList);
        filmListItem.setFilmId(filmId);
        return filmListItem;
    }

    public static FilmListRequest filmListRequest() {
        return new FilmListRequest(LIST_NAME);
    }

    public static FilmListRequest updateFilmListRequest() {
        return new FilmListRequest("updated name");
    }

    public static FilmListResponse filmListResponse() {
        return new FilmListResponse(LIST_ID, LIST_NAME);
    }

    public static FilmListItemRequest filmListItemRequest() {
        return new FilmListItemRequest(FILM_ID);
    }

    public static FilmListItemResponse filmListItemResponse() {
        return new FilmListItemResponse(ITEM_ID, FILM_ID);
    }
}

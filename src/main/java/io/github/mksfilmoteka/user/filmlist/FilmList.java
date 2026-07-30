package io.github.mksfilmoteka.user.filmlist;

import io.github.mksfilmoteka.user.common.BaseEntity;
import io.github.mksfilmoteka.user.profile.UserProfile;
import jakarta.persistence.*;
import lombok.*;

import static lombok.AccessLevel.PRIVATE;

@Getter
@Setter
@AllArgsConstructor(access = PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "film_list")
public class FilmList extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserProfile user;

    @Column
    private String name;
}

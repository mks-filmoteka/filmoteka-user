package io.github.mksfilmoteka.user.filmlist;

import io.github.mksfilmoteka.user.common.BaseEntity;
import io.github.mksfilmoteka.user.profile.UserProfile;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

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

    @ElementCollection
    @CollectionTable(
            name = "list_item",
            joinColumns = @JoinColumn(name = "film_list_id")
    )
    @Column(name = "film_id")
    private Set<Long> filmIds = new HashSet<>();
}

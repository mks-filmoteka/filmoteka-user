package io.github.mksfilmoteka.user.filmlist;

import io.github.mksfilmoteka.user.common.BaseEntity;
import io.github.mksfilmoteka.user.profile.UserProfile;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

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

    @OnDelete(action = OnDeleteAction.CASCADE)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserProfile user;

    @Column(nullable = false, length = 100)
    private String name;

    @OnDelete(action = OnDeleteAction.CASCADE)
    @ElementCollection
    @CollectionTable(
            name = "list_item",
            joinColumns = @JoinColumn(name = "film_list_id", nullable = false),
            indexes = @Index(columnList = "film_id")
    )
    @Column(name = "film_id", nullable = false)
    private Set<Long> filmIds = new HashSet<>();
}

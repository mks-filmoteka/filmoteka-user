package io.github.mksfilmoteka.user.filmlist;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

import static lombok.AccessLevel.PRIVATE;

@Getter
@Setter
@AllArgsConstructor(access = PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "film_list_item")
public class FilmListItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "film_list_id")
    private FilmList filmList;

    @Column(name = "film_id")
    private Long filmId;

    @CreationTimestamp
    @Column(name = "added_ts", updatable = false)
    private LocalDateTime addedTs;
}

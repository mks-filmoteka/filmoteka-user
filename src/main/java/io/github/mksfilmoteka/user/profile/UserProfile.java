package io.github.mksfilmoteka.user.profile;

import io.github.mksfilmoteka.user.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.*;

import static lombok.AccessLevel.PRIVATE;

@Getter
@Setter
@AllArgsConstructor(access = PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "user_profile",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "identity_sub"),
                @UniqueConstraint(columnNames = "email")
        }
)
public class UserProfile extends BaseEntity {

    @Column(name = "identity_sub", nullable = false)
    private String identitySub;

    @Column(nullable = false)
    private String email;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;
}

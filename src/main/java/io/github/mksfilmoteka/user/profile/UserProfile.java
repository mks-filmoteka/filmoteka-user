package io.github.mksfilmoteka.user.profile;

import io.github.mksfilmoteka.user.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import static lombok.AccessLevel.PRIVATE;

@Getter
@Setter
@AllArgsConstructor(access = PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "user_profile")
public class UserProfile extends BaseEntity {

    @Column(name = "identity_sub")
    private String identitySub;

    @Column
    private String email;

    @Column(name = "display_name")
    private String displayName;
}

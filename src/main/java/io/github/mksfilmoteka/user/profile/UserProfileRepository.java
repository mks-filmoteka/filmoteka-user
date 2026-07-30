package io.github.mksfilmoteka.user.profile;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    Optional<UserProfile> findByIdentitySub(String identitySub);

    Optional<UserProfile> findByEmail(String email);

    boolean existsByIdentitySub(String identitySub);

    boolean existsByEmail(String email);
}

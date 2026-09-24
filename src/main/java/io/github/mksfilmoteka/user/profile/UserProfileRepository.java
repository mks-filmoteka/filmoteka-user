package io.github.mksfilmoteka.user.profile;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    Optional<UserProfile> findByIdentitySub(String identitySub);

    Optional<UserProfile> findByEmail(String email);

    boolean existsByIdentitySub(String identitySub);

    boolean existsByEmail(String email);

    @Modifying
    @Query("""
            INSERT INTO UserProfile
                (identitySub, email, displayName)
            VALUES
                (:identitySub, :email, :displayName)
            ON CONFLICT DO NOTHING
            """)
    int insertIfAbsent(
            @Param("identitySub") String identitySub,
            @Param("email") String email,
            @Param("displayName") String displayName
    );
}

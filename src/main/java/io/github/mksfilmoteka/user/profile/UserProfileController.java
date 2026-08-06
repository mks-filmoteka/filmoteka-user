package io.github.mksfilmoteka.user.profile;

import io.github.mksfilmoteka.user.auth.AuthUser;
import io.github.mksfilmoteka.user.auth.AuthUserConverter;
import io.github.mksfilmoteka.user.profile.dto.UserProfileRequest;
import io.github.mksfilmoteka.user.profile.dto.UserProfileResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;
    private final AuthUserConverter authUserConverter;

    @GetMapping
    public UserProfileResponse getUserProfile(@AuthenticationPrincipal Jwt jwt) {
        AuthUser authUser = authUserConverter.from(jwt);
        return userProfileService.getUserProfile(authUser);
    }

    @PutMapping
    public UserProfileResponse updateUserProfile(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UserProfileRequest request) {
        AuthUser authUser = authUserConverter.from(jwt);
        return userProfileService.updateUserProfile(authUser, request);
    }
}

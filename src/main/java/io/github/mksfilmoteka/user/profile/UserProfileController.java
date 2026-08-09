package io.github.mksfilmoteka.user.profile;

import io.github.mksfilmoteka.user.auth.AuthUser;
import io.github.mksfilmoteka.user.auth.AuthUserConverter;
import io.github.mksfilmoteka.user.common.exception.ErrorResponse;
import io.github.mksfilmoteka.user.profile.dto.UserProfileRequest;
import io.github.mksfilmoteka.user.profile.dto.UserProfileResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Profile", description = "Operations related to authenticated user's profile")
@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;
    private final AuthUserConverter authUserConverter;

    @Operation(
            summary = "Get authenticated user profile",
            description = "Returns the authenticated user's profile, provisioning it from identity claims when needed"
    )
    @ApiResponse(responseCode = "200", description = "Profile returned",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserProfileResponse.class)
            )
    )
    @ApiResponse(responseCode = "409", description = "Profile email conflict",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @GetMapping
    public UserProfileResponse getUserProfile(@Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {
        AuthUser authUser = authUserConverter.from(jwt);
        return userProfileService.getUserProfile(authUser);
    }

    @Operation(
            summary = "Update authenticated user profile",
            description = "Updates the authenticated user's profile fields"
    )
    @ApiResponse(responseCode = "200", description = "Profile updated",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserProfileResponse.class)
            )
    )
    @ApiResponse(responseCode = "400", description = "Validation error",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @ApiResponse(responseCode = "409", description = "Profile email conflict",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @PutMapping
    public UserProfileResponse updateUserProfile(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UserProfileRequest request) {
        AuthUser authUser = authUserConverter.from(jwt);
        return userProfileService.updateUserProfile(authUser, request);
    }
}

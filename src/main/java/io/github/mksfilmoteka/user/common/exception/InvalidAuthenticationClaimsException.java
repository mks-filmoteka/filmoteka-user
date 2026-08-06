package io.github.mksfilmoteka.user.common.exception;

public class InvalidAuthenticationClaimsException extends RuntimeException {
    public InvalidAuthenticationClaimsException(String claimName) {
        super("Authenticated JWT is missing required claim: " + claimName);
    }
}

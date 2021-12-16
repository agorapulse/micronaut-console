package micronaut.console.example;

import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.authentication.AuthenticationResponse;

import java.util.Optional;

public class SimpleAuthenticationResponse implements AuthenticationResponse {

    private final Authentication authentication;

    public SimpleAuthenticationResponse(Authentication authentication) {
        this.authentication = authentication;
    }

    @Override
    public Optional<Authentication> getAuthentication() {
        return Optional.of(authentication);
    }

    @Override
    public boolean isAuthenticated() {
        return true;
    }

    @Override
    public Optional<String> getMessage() {
        return Optional.empty();
    }

}

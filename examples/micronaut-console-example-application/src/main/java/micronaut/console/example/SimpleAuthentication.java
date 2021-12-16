package micronaut.console.example;

import io.micronaut.security.authentication.Authentication;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class SimpleAuthentication implements Authentication {

    private static final String JSON_KEY_NAME = "name";
    private static final String JSON_KEY_ATTRIBUTES = "attributes";
    private final String name;

    public SimpleAuthentication(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return Collections.emptyMap();
    }

    @Override
    public Collection<String> getRoles() {
        return Collections.emptySet();
    }

}

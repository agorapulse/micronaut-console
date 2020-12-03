package com.agorapulse.micronaut.console.advisors;

import com.agorapulse.micronaut.console.ConsoleConfiguration;
import com.agorapulse.micronaut.console.Script;
import com.agorapulse.micronaut.console.SecurityAdvisor;
import io.micronaut.context.annotation.Requires;

import javax.inject.Singleton;

@Singleton
@Requires(property = "console.users")
public class UsersAdvisor implements SecurityAdvisor {

    private final ConsoleConfiguration configuration;

    public UsersAdvisor(ConsoleConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public boolean isExecutionAllowed(Script script) {
        if (script.getUser() == null || script.getUser().getId() == null) {
            // id must be known
            return false;
        }
        return configuration.getUsers().contains(script.getUser().getId());
    }

    @Override
    public String toString() {
        return "Users advisor for user IDs " + String.join(", ", configuration.getUsers());
    }

}


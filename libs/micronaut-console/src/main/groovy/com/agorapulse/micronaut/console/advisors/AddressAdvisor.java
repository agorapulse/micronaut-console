package com.agorapulse.micronaut.console.advisors;

import com.agorapulse.micronaut.console.ConsoleConfiguration;
import com.agorapulse.micronaut.console.Script;
import com.agorapulse.micronaut.console.SecurityAdvisor;
import io.micronaut.context.annotation.Requires;

import javax.inject.Singleton;

@Singleton
@Requires(property = "console.addresses")
public class AddressAdvisor implements SecurityAdvisor {

    private final ConsoleConfiguration configuration;

    public AddressAdvisor(ConsoleConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public boolean isExecutionAllowed(Script script) {
        if (script.getUser() == null || script.getUser().getAddress() == null) {
            // address must be known
            return false;
        }
        return configuration.getAddresses().contains(script.getUser().getAddress());
    }

    @Override
    public String toString() {
        return "Address advisor for addresses " + String.join(", ", configuration.getAddresses());
    }

}


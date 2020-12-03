package com.agorapulse.micronaut.console.advisors;

import com.agorapulse.micronaut.console.ConsoleConfiguration;
import com.agorapulse.micronaut.console.Script;
import com.agorapulse.micronaut.console.SecurityAdvisor;
import io.micronaut.context.annotation.Requires;

import javax.inject.Singleton;
import java.time.Instant;

@Singleton
@Requires(property = "console.until")
public class UntilAdvisor implements SecurityAdvisor {

    private final ConsoleConfiguration configuration;

    public UntilAdvisor(ConsoleConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public boolean isExecutionAllowed(Script script) {
        return Instant.now().isBefore(configuration.convertUntil());
    }

    @Override
    public String toString() {
        return "Until advisor for date before " + configuration.convertUntil();
    }

}


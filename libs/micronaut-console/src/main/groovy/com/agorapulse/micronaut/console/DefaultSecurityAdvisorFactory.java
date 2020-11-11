package com.agorapulse.micronaut.console;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;

import javax.inject.Singleton;

@Factory
public class DefaultSecurityAdvisorFactory {

    @Bean
    @Singleton
    @Requires(property = "console.addresses")
    public SecurityAdvisor addressFilter(ConsoleConfiguration configuration) {
        return script -> {
            if (script.getUser() == null || script.getUser().getAddress() == null) {
                // address must be known
                return false;
            }
            return configuration.getAddresses().contains(script.getUser().getAddress());
        };
    }

    @Bean
    @Singleton
    @Requires(property = "console.users")
    public SecurityAdvisor userFilter(ConsoleConfiguration configuration) {
        return script -> {
            if (script.getUser() == null || script.getUser().getId() == null) {
                // id must be known
                return false;
            }
            return configuration.getUsers().contains(script.getUser().getId());
        };
    }

}

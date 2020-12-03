package com.agorapulse.micronaut.console.advisors;

import com.agorapulse.micronaut.console.ConsoleConfiguration;
import com.agorapulse.micronaut.console.Script;
import com.agorapulse.micronaut.console.SecurityAdvisor;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.env.Environment;

import javax.inject.Singleton;

@Singleton
public class CloudAdvisor implements SecurityAdvisor {

    private final ConsoleConfiguration configuration;
    private final ApplicationContext context;

    public CloudAdvisor(ConsoleConfiguration configuration, ApplicationContext context) {
        this.configuration = configuration;
        this.context = context;
    }

    @Override
    public boolean isExecutionAllowed(Script script) {
        if (configuration.isEnabled()) {
            return true;
        }

        // functions has their own security checks
        if (context.getEnvironment().getActiveNames().contains(Environment.FUNCTION)) {
            return true;
        }

        // disable by default for the cloud environment (deployed apps)
        return !context.getEnvironment().getActiveNames().contains(Environment.CLOUD);
    }

    @Override
    public String toString() {
        return "Cloud advisor for environments " + String.join(", ", context.getEnvironment().getActiveNames()) + ", enabled = " + configuration.isEnabled();
    }

}


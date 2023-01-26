/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2023 Agorapulse.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.agorapulse.micronaut.console.advisors;

import com.agorapulse.micronaut.console.ConsoleConfiguration;
import com.agorapulse.micronaut.console.Script;
import com.agorapulse.micronaut.console.SecurityAdvisor;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.env.Environment;

import javax.inject.Singleton;
import java.time.Instant;

@Singleton
public class EnabledAdvisor implements SecurityAdvisor {

    private final ConsoleConfiguration configuration;
    private final ApplicationContext context;

    public EnabledAdvisor(ConsoleConfiguration configuration, ApplicationContext context) {
        this.configuration = configuration;
        this.context = context;
    }

    @Override
    public boolean isExecutionAllowed(Script script) {
        //CHECKSTYLE:OFF
        if (configuration.isEnabled() || (configuration.convertUntil() != null && configuration.convertUntil().isAfter(Instant.now()))) {
            return true;
        }
        //CHECKSTYLE:ON

        // functions have their own security checks
        // otherwise return false
        return context.getEnvironment().getActiveNames().contains(Environment.FUNCTION);

        // disable by default
    }

    @Override
    public String toString() {
        return "Enabled advisor while enabled = " + configuration.isEnabled() + " and until is " + configuration.getUntil();
    }

}


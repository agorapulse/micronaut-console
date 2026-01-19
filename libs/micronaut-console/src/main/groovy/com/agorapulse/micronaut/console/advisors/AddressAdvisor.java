/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2026 Agorapulse.
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
import io.micronaut.context.annotation.Requires;

import jakarta.inject.Singleton;

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


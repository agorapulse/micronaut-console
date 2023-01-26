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
package com.agorapulse.micronaut.console;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Singleton
public class DefaultConsoleService implements ConsoleService {

    private final List<AuditService> auditServices;
    private final List<BindingProvider> bindingProviders;
    private final List<SecurityAdvisor> securityAdvisors;
    private final Map<String, ConsoleEngine> engines;
    private final ConsoleConfiguration configuration;

    public DefaultConsoleService(
        List<AuditService> auditServices,
        List<BindingProvider> bindingProviders,
        List<SecurityAdvisor> securityAdvisors,
        List<ConsoleEngineFactory> engines,
        ConsoleConfiguration configuration
    ) {
        this.auditServices = auditServices;
        this.bindingProviders = bindingProviders;
        this.securityAdvisors = securityAdvisors;
        this.configuration = configuration;

        Map<String, ConsoleEngine> map = new HashMap<>();
        engines.forEach(f -> f.getEngines().forEach(e -> map.put(e.getLanguage(), e)));

        this.engines = map;
    }

    @Override @Nonnull
    public ExecutionResult execute(Script script) {
        for (SecurityAdvisor a : securityAdvisors) {
            if (!a.isExecutionAllowed(script)) {
                throw new ConsoleSecurityException(script, "Execution forbidden by " + a);
            }
        }

        ConsoleEngine engine = engines.get(script.getLanguage());

        if (engine == null) {
            throw new ConsoleException(script, "No engine present for " + script.getLanguage()
                + ". Set 'io.micronaut.context.condition' logging to trace debug missing engines");
        }

        Map<String, Object> bindings = new TreeMap<>();
        bindings.put("user", script.getBody());

        bindingProviders.forEach(p -> bindings.putAll(p.getBinding()));

        auditServices.forEach(a -> a.beforeExecute(script, bindings));

        try {
            ExecutionResult result = engine.execute(script.getBody(), bindings);
            auditServices.forEach(a -> a.afterExecute(script, result));
            return result;
        } catch (Throwable throwable) {
            auditServices.forEach(a -> a.onError(script, throwable));
            throw new ConsoleException(script, "Exception during script execution", throwable);
        }
    }

    @Override @Nullable
    public String getLanguageForMimeType(@Nullable String contentType) {
        for (ConsoleEngine engine : engines.values()) {
            if (engine.getSupportedMimeTypes().contains(contentType)) {
                return engine.getLanguage();
            }
        }
        return configuration.getLanguage();
    }
}

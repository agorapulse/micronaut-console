/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020 Agorapulse.
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
package com.agorapulse.micronaut.console.groovy;

import com.agorapulse.micronaut.console.ConsoleEngine;
import com.agorapulse.micronaut.console.ExecutionResult;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import io.micronaut.context.annotation.Requires;
import org.codehaus.groovy.control.CompilerConfiguration;

import javax.inject.Singleton;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Singleton
@Requires(classes = GroovyShell.class)
public class GroovyConsoleEngine implements ConsoleEngine {

    private static final String LANGUAGE = "groovy";
    private static final List<String> MIME_TYPES = Arrays.asList("application/groovy", "text/groovy");

    private final List<CompilerConfigurationCustomizer> customizers;

    public GroovyConsoleEngine(List<CompilerConfigurationCustomizer> customizers) {
        this.customizers = customizers;
    }

    @Override
    public ExecutionResult execute(String code, Map<String, Object> bindings) {
        StringWriter sw = new StringWriter();
        PrintWriter writer = new PrintWriter(sw);

        Map<String, Object> groovyBindings = new LinkedHashMap<>(bindings);
        groovyBindings.put("out", writer);

        CompilerConfiguration configuration = new CompilerConfiguration();
        customizers.forEach(c -> c.customize(configuration));

        GroovyShell groovyShell = new GroovyShell(new Binding(groovyBindings), configuration);

        Object result = groovyShell.evaluate(code);

        return new ExecutionResult(result, sw.toString());
    }

    @Override
    public String getLanguage() {
        return LANGUAGE;
    }

    @Override
    public List<String> getSupportedMimeTypes() {
        return MIME_TYPES;
    }

}

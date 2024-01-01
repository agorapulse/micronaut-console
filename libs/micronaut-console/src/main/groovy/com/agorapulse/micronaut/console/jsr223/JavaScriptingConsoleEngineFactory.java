/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2024 Agorapulse.
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
package com.agorapulse.micronaut.console.jsr223;

import com.agorapulse.micronaut.console.ConsoleEngine;
import com.agorapulse.micronaut.console.ConsoleEngineFactory;

import jakarta.inject.Singleton;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
public class JavaScriptingConsoleEngineFactory implements ConsoleEngineFactory {

    @Override
    public List<ConsoleEngine> getEngines() {
        ScriptEngineManager mgr = new ScriptEngineManager();
        List<ScriptEngineFactory> factories = mgr.getEngineFactories();

        List<ConsoleEngine> engines = new ArrayList<>();

        for (ScriptEngineFactory factory : factories) {
            Set<String> uniqueNames = factory.getNames().stream().map(String::toLowerCase).collect(Collectors.toSet());
            for (String name : uniqueNames) {
                engines.add(new JavaScriptingConsoleEngine(
                    factory.getScriptEngine(),
                    name,
                    factory.getMimeTypes()
                ));
            }
        }

        return engines;
    }
}

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
package com.agorapulse.micronaut.console;

import jakarta.inject.Singleton;
import java.util.List;

/**
 * Default implementation of the console engine factory simply collects the instances from the application context.
 */
@Singleton
public class DefaultConsoleEngineFactory implements ConsoleEngineFactory {

    private final List<ConsoleEngine> engines;

    public DefaultConsoleEngineFactory(List<ConsoleEngine> engines) {
        this.engines = engines;
    }

    @Override
    public List<ConsoleEngine> getEngines() {
        return engines;
    }

}

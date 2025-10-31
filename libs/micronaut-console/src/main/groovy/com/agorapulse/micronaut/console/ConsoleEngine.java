/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2025 Agorapulse.
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

import java.util.List;
import java.util.Map;

/**
 * The engine which can execute the script.
 *
 * Different engines exist for the different languages.
 */
public interface ConsoleEngine {

    /**
     * Executes the script.
     *
     * @param code the body of the script
     * @param bindings the map of objects available for the script
     *
     * @return the execution result
     */
    ExecutionResult execute(String code, Map<String, Object> bindings) throws Throwable;

    /**
     * @return the language supported by this engine
     */
    String getLanguage();

    /**
     * @return the supported mime types representing this language
     */
    List<String> getSupportedMimeTypes();

}

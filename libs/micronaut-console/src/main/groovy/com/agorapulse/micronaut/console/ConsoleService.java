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

/**
 * The script executor service.
 */
public interface ConsoleService {

    /**
     * Executes the script
     * @param script the script
     * @return the result of the script as String
     */
    @Nonnull ExecutionResult execute(Script script);

    /**
     * Returns the language based on the content type.
     * @param contentType the content type of the script
     * @return the language of the script
     */
    @Nullable String getLanguageForMimeType(@Nullable String contentType);
}

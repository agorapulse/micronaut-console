/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020 Vladimir Orany.
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

import java.util.Map;

/**
 * The service provides audit logging for the script execution.
 */
public interface AuditService {

    /**
     * Logs the execution of the script before it is executed.
     * @param script the script
     */
    default void beforeExecute(Script script, Map<String, Object> bindings) {
        // noop
    }

    /**
     * Logs the execution of the script after it is executed.
     * @param script the script
     * @param result the execution result
     */
    default void afterExecute(Script script, String result) {
        // noop
    }

    /**
     * Logs the error which happened during the execution.
     * @param script the script
     * @param throwable the executed error
     */
    default void onError(Script script, Throwable throwable) {
        // noop
    }

}

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

import java.util.Collections;
import java.util.Map;

/**
 * Provides additional binding values for the script.
 */
public interface BindingProvider {

    /**
     * @return the additional bindings for the script
     */
    Map<String, ?> getBinding();

    static String purifyClassName(String className) {
        if (!className.contains("$")) {
            return className;
        }

        if (className.contains(".$") && className.contains("Definition$Intercepted")) {
            return className
                // Micronaut 3.x
                .replace("$Definition$Intercepted", "")
                // Micronaut 1.x and 2.x
                .replace("Definition$Intercepted", "")
                .replace(".$", ".");
        }

        return className
            .replace("$Intercepted", "")
            .replace(".$", ".")
            .replace('$', '.');
    }

    static Map<String, ?> getDefaultBindingsStubs() {
        return Collections.singletonMap("user", User.anonymous());
    }

}

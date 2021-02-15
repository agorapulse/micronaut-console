/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 Agorapulse.
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
package com.agorapulse.micronaut.console.util;

import javax.inject.Singleton;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Singleton
public class ExceptionSanitizer {

    private static final List<String> IGNORED_PACKAGES = Arrays.asList(
        "org.codehaus.groovy",
        "groovy.lang",
        "sun",
        "java.lang.reflect",
        "com.agorapulse.micronaut.console"
    );

    private static final List<String> STOPPERS = Arrays.asList(
        "com.agorapulse.micronaut.console.http.ConsoleController",
        "com.agorapulse.micronaut.console.function.ConsoleHandler"
    );

    public String extractMessage(Throwable exception) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        sanitizeStackTrace(exception);
        exception.printStackTrace(pw);
        return sw.toString();
    }

    public void sanitizeStackTrace(Throwable exception) {
        StackTraceElement[] stackTrace = exception.getStackTrace();
        List<StackTraceElement> filtered = new ArrayList<>();
        for (StackTraceElement e : stackTrace) {
            if (STOPPERS.contains(e.getClassName())) {
                break;
            }
            if (IGNORED_PACKAGES.stream().anyMatch(pkg -> e.getClassName().startsWith(pkg))) {
                continue;
            }
            filtered.add(e);
        }
        exception.setStackTrace(filtered.toArray(new StackTraceElement[0]));

        if (exception.getCause() != null) {
            sanitizeStackTrace(exception.getCause());
        }
    }

}

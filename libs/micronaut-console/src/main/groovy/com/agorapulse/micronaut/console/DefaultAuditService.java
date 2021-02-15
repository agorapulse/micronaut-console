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
package com.agorapulse.micronaut.console;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

@Singleton
public class DefaultAuditService implements AuditService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultAuditService.class);

    @Override
    public void beforeExecute(Script script, Map<String, Object> bindings) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Before execution:\n" + script + "\n\nBindings: " + bindings);
        }
    }

    @Override
    public void afterExecute(Script script, ExecutionResult result) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("After execution:\n" + script + "\n\nResult:\n" + result);
        }
    }

    @Override
    public void onError(Script script, Throwable throwable) {
        if (LOGGER.isInfoEnabled()) {
            StringWriter sw = new StringWriter();
            PrintWriter writer = new PrintWriter(sw);
            throwable.printStackTrace(writer);
            LOGGER.debug("Execution error:\n" + script + "\n\n" + sw.toString());
        }
    }

}

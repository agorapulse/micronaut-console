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
package com.agorapulse.micronaut.console.function;

import com.agorapulse.micronaut.console.*;
import io.micronaut.context.ApplicationContext;
import io.micronaut.function.FunctionBean;
import io.micronaut.function.executor.FunctionInitializer;

import javax.inject.Inject;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.function.UnaryOperator;

@FunctionBean(value = "console", method = "apply")
public class ConsoleHandler extends FunctionInitializer implements UnaryOperator<String> {

    @Inject private ConsoleService service;
    @Inject private ConsoleConfiguration configuration;

    @Override
    public String apply(String scriptBody) {
        Script script = new Script(configuration.getLanguage(), scriptBody, User.anonymous());
        try {
            return service.execute(script).toString();
        } catch (Throwable th) {
            return extractMessage(th) + "\n\nScript:\n" + script;
        }
    }

    public ConsoleHandler() { }

    public ConsoleHandler(ApplicationContext applicationContext) {
        super(applicationContext);
    }

    private String extractMessage(Throwable exception) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exception.printStackTrace(pw);
        return sw.toString();
    }
}

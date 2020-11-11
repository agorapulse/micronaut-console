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

import com.agorapulse.micronaut.console.ConsoleEngine;
import com.agorapulse.micronaut.console.ConsoleService;
import com.agorapulse.micronaut.console.Script;
import com.agorapulse.micronaut.console.User;
import io.micronaut.context.ApplicationContext;
import io.micronaut.function.FunctionBean;
import io.micronaut.function.executor.FunctionInitializer;

import javax.inject.Inject;
import java.util.Optional;
import java.util.function.UnaryOperator;

@FunctionBean(value = "${console.function:console}\"", method = "apply")
public class ConsoleHandler extends FunctionInitializer implements UnaryOperator<String> {

    @Inject private ConsoleService consoleService;
    @Inject private ConsoleEngine engine;

    @Override
    public String apply(String event) {
        String logGroupName = Optional.ofNullable(System.getenv("AWS_LAMBDA_LOG_GROUP_NAME")).orElse("unknown");
        String logStreamName = Optional.ofNullable(System.getenv("AWS_LAMBDA_LOG_STREAM_NAME")).orElse("unknown");

        Script script = new Script(engine.getLanguage(), event, new User(
            null,
            null,
               logGroupName + "/" + logStreamName
            ));

        return consoleService.execute(script);
    }

    public ConsoleHandler() { }

    public ConsoleHandler(ApplicationContext applicationContext) {
        super(applicationContext);
    }

}

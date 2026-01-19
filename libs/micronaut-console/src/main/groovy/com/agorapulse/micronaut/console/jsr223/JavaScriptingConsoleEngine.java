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
package com.agorapulse.micronaut.console.jsr223;

import com.agorapulse.micronaut.console.ConsoleEngine;
import com.agorapulse.micronaut.console.ExecutionResult;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.SimpleBindings;
import javax.script.SimpleScriptContext;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

/**
 * Console wrapper around Java Scripting JSR-223 API.
 */
public class JavaScriptingConsoleEngine implements ConsoleEngine {

    private final ScriptEngine engine;
    private final String language;
    private final List<String> supportedMimeTypes;

    public JavaScriptingConsoleEngine(ScriptEngine engine, String language, List<String> supportedMimeTypes) {
        this.engine = engine;
        this.language = language;
        this.supportedMimeTypes = supportedMimeTypes;
    }

    @Override
    public ExecutionResult execute(String code, Map<String, Object> bindings) throws Throwable {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        ScriptContext context = new SimpleScriptContext();
        context.setBindings(new SimpleBindings(bindings), ScriptContext.ENGINE_SCOPE);
        context.setWriter(pw);

        // evaluate code
        return new ExecutionResult(engine.eval(code, context), sw.toString());
    }

    @Override
    public String getLanguage() {
        return language;
    }

    @Override
    public List<String> getSupportedMimeTypes() {
        return supportedMimeTypes;
    }

}

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
package com.agorapulse.micronaut.console.http;

import com.agorapulse.micronaut.console.ConsoleEngine;
import com.agorapulse.micronaut.console.ConsoleException;
import com.agorapulse.micronaut.console.ConsoleService;
import com.agorapulse.micronaut.console.Script;
import com.agorapulse.micronaut.console.User;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Error;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.hateoas.JsonError;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller("${console.path:/console}")
public class ConsoleController {

    private final Map<String, String> languages;
    private final ConsoleService service;

    public ConsoleController(List<ConsoleEngine> engines, ConsoleService service) {
        this.service = service;

        Map<String, String> languages = new LinkedHashMap<>();
        engines.forEach(e -> e.getSupportedMimeTypes().forEach(m -> languages.put(m, e.getLanguage())));
        this.languages = languages;
    }

    @Post("/execute")
    public ExecutionResult execute(@Body String body, @Header("Content-Type") String contentType, User user) {
        String language = languages.get(contentType);

        return new ExecutionResult(service.execute(new Script(language, body, user)));
    }

    @Error(ConsoleException.class)
    public HttpResponse<JsonError> consoleException(ConsoleException exception) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exception.printStackTrace(pw);
        return HttpResponse.badRequest(new JsonError(sw.toString()));
    }

}

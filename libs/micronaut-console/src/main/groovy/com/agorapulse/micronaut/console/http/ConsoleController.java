/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2024 Agorapulse.
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

import com.agorapulse.micronaut.console.ConsoleException;
import com.agorapulse.micronaut.console.ConsoleSecurityException;
import com.agorapulse.micronaut.console.ConsoleService;
import com.agorapulse.micronaut.console.ExecutionResult;
import com.agorapulse.micronaut.console.Script;
import com.agorapulse.micronaut.console.User;
import com.agorapulse.micronaut.console.ide.DslGenerator;
import com.agorapulse.micronaut.console.util.ExceptionSanitizer;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Consumes;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Error;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Produces;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@ExecuteOn(TaskExecutors.BLOCKING)
@Controller("${console.path:/console}")
public class ConsoleController {

    private final ConsoleService service;
    private final ExceptionSanitizer sanitizer;
    private final Map<String, DslGenerator> generators;

    public ConsoleController(ConsoleService service, ExceptionSanitizer sanitizer, List<DslGenerator> generatorList) {
        this.service = service;
        this.sanitizer = sanitizer;
        this.generators = generatorList.stream().collect(Collectors.toMap(DslGenerator::getScriptType, Function.identity()));
    }

    @Post("/execute")
    @Consumes(MediaType.ALL)
    @Produces(MediaType.APPLICATION_JSON)
    public ExecutionResult execute(@Body String body, @Nullable @Header("Content-Type") String contentType, User user) {
        return service.execute(new Script(service.getLanguageForMimeType(contentType), body, user));
    }

    @Post("/execute/result")
    @Consumes(MediaType.ALL)
    @Produces(MediaType.TEXT_PLAIN)
    public HttpResponse<String> executeAndReturnText(@Body String body, @Nullable @Header("Content-Type") String contentType, User user) {
        try {
            return HttpResponse.ok(service.execute(new Script(service.getLanguageForMimeType(contentType), body, user)).toString());
        } catch (ConsoleException e) {
            return HttpResponse.badRequest(sanitizer.extractMessage(e) + "\n\n" + e.getScript());
        }
    }

    @Get("/dsl/{type}")
    @Produces("text/plain")
    public HttpResponse<String> generateDslFile(@PathVariable("type") String type) {
        DslGenerator generator = generators.get(type);

        if (generator == null) {
            return HttpResponse.notFound("Generator for " + type + " does not exist! Available " + String.join(", ", generators.keySet()));
        }

        return HttpResponse.ok(generator.generateScript());
    }

    @Error(ConsoleException.class)
    public HttpResponse<ScriptJsonError> consoleException(ConsoleException exception) {
        ScriptJsonError error = new ScriptJsonError(exception.getScript(), sanitizer.extractMessage(exception));
        if (exception instanceof ConsoleSecurityException) {
            return HttpResponse.unauthorized();
        }
        return HttpResponse.badRequest(error);
    }

}

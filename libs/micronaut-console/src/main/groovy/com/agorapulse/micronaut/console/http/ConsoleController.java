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
package com.agorapulse.micronaut.console.http;

import com.agorapulse.micronaut.console.ConsoleException;
import com.agorapulse.micronaut.console.ConsoleService;
import com.agorapulse.micronaut.console.ExecutionResult;
import com.agorapulse.micronaut.console.Script;
import com.agorapulse.micronaut.console.ConsoleSecurityException;
import com.agorapulse.micronaut.console.User;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Consumes;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Error;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.hateoas.JsonError;

import javax.annotation.Nullable;
import java.io.PrintWriter;
import java.io.StringWriter;

@Controller("${console.path:/console}")
public class ConsoleController {

    private final ConsoleService service;

    public ConsoleController(ConsoleService service) {
        this.service = service;
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
            return HttpResponse.badRequest(extractMessage(e) + "\n\n" + e.getScript());
        }
    }

    @Error(ConsoleException.class)
    public HttpResponse<JsonError> consoleException(ConsoleException exception) {
        ScriptJsonError error = new ScriptJsonError(exception.getScript(), extractMessage(exception));
        if (exception instanceof ConsoleSecurityException) {
            return HttpResponse.unauthorized();
        }
        return HttpResponse.badRequest(error);
    }

    private String extractMessage(ConsoleException exception) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exception.printStackTrace(pw);
        return sw.toString();
    }

}

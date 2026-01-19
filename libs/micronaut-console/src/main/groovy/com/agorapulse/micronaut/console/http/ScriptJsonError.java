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
package com.agorapulse.micronaut.console.http;

import com.agorapulse.micronaut.console.Script;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.hateoas.JsonError;

@Introspected
@Produces(MediaType.APPLICATION_JSON)
public class ScriptJsonError extends JsonError {

    private final Script script;

    public ScriptJsonError(Script script, String message) {
        super(message);
        this.script = script;
    }

    public Script getScript() {
        return script;
    }

}

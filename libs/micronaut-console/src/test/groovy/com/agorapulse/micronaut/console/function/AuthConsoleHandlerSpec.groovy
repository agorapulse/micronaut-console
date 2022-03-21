/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2022 Agorapulse.
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
package com.agorapulse.micronaut.console.function

import com.agorapulse.testing.fixt.Fixt
import groovy.transform.CompileDynamic
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

@CompileDynamic
class AuthConsoleHandlerSpec extends Specification {

    @Shared @AutoCleanup AuthConsoleHandler handler = new AuthConsoleHandler()

    Fixt fixt = Fixt.create(ConsoleHandlerSpec)

    void 'execute simple groovy script'() {
        expect:
            handler.apply(script('"Hello World"')) == 'Hello World'
    }

    void 'execute script with prints'() {
        expect:
            handler.apply(script(fixt.readText('prints.groovy'))) == fixt.readText('prints.txt')
    }

    void 'execute script with exception'() {
        given:
            String errorResult = handler.apply(script(fixt.readText('error.groovy')))
        expect:
            errorResult.startsWith('com.agorapulse.micronaut.console.ConsoleException: Exception during script execution')
    }

    private AuthorizedScript script(String body) {
        return new AuthorizedScript(
            body: body,
            user: new AuthorizedScript.Executor(id: 'executor', name: 'Script Executor', address: '/10.0.0.1')
        )
    }

}

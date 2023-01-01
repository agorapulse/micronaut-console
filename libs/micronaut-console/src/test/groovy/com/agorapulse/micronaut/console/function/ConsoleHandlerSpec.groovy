/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2023 Agorapulse.
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
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

class ConsoleHandlerSpec extends Specification {

    @Shared @AutoCleanup ConsoleHandler handler = new ConsoleHandler()

    Fixt fixt = Fixt.create(ConsoleHandlerSpec)

    void 'execute simple groovy script'() {
        expect:
            handler.apply('"Hello World"') == 'Hello World'
    }

    void 'execute script with prints'() {
        expect:
            handler.apply(fixt.readText('prints.groovy')) == fixt.readText('prints.txt')
    }

    void 'execute script with exception'() {
        given:
            String errorResult = handler.apply(fixt.readText('error.groovy'))
        expect:
            errorResult.startsWith('com.agorapulse.micronaut.console.ConsoleException: Exception during script execution')
    }

}

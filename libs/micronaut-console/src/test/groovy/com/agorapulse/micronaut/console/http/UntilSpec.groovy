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
package com.agorapulse.micronaut.console.http

import com.agorapulse.gru.Gru
import com.agorapulse.gru.http.Http
import io.micronaut.context.ApplicationContext
import io.micronaut.runtime.server.EmbeddedServer
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

import java.time.Instant

class UntilSpec extends Specification {

    @Shared @AutoCleanup ApplicationContext context
    @Shared @AutoCleanup EmbeddedServer server

    @AutoCleanup Gru gru = Gru.equip(Http.steal(this))

    void setupSpec() {
        context = ApplicationContext.builder(
            'console.until': Instant.now().minusSeconds(3600).toString(),
        ).build()

        context.start()

        server = context.getBean(EmbeddedServer)
        server.start()
    }

    void setup() {
        gru.prepare(server.URL.toString())
    }

    void 'execute a simple groovy script'() {
        expect:
            gru.test {
                post('/console/execute') {
                    content inline('"Hello world!"'), 'text/groovy'
                }
                expect {
                    status UNAUTHORIZED
                }
            }
    }

}

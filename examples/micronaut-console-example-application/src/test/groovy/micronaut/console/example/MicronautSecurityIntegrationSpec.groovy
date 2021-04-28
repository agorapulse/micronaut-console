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
package micronaut.console.example

import com.agorapulse.gru.Gru
import com.agorapulse.gru.http.Http
import io.micronaut.context.ApplicationContext
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.security.authentication.UserDetails
import io.micronaut.security.token.jwt.generator.AccessRefreshTokenGenerator
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

class MicronautSecurityIntegrationSpec extends Specification {

    @Shared @AutoCleanup ApplicationContext context
    @Shared @AutoCleanup EmbeddedServer server
    @Shared AccessRefreshTokenGenerator generator

    @AutoCleanup Gru gru = Gru.equip(Http.steal(this))

    void setupSpec() {
        context = ApplicationContext.build().build()

        context.start()

        generator = context.getBean(AccessRefreshTokenGenerator)

        server = context.getBean(EmbeddedServer)
        server.start()
    }

    void setup() {
        gru.prepare(server.URL.toString())
    }

    void 'cannot access the console without authentication'() {
        expect:
            gru.test {
                post '/console/execute/result', {
                    content inline('"Hello World"'), 'text/groovy'
                }
                expect {
                    status UNAUTHORIZED
                }
            }
    }

    void 'can access the console with the token'() {
        given:
            String token = generator.generate(new UserDetails('sherlock', [])).get().accessToken
        expect:
            gru.test {
                post '/console/execute/result', {
                    headers Authorization: "Bearer $token"
                    content inline('"Hello World"'), 'text/groovy'
                }
                expect {
                    text inline('Hello World')
                }
            }
    }

}

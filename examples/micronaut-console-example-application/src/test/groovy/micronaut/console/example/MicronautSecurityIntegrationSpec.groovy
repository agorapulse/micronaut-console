/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2025 Agorapulse.
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
import groovy.transform.CompileDynamic
import io.micronaut.context.annotation.Property
import io.micronaut.security.token.generator.TokenGenerator
import io.micronaut.security.token.claims.ClaimsGenerator
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

/**
 * The tests for the Micronaut Security integration.
 */
@CompileDynamic
@MicronautTest(environments = 'secured')
@Property(name = 'gru.http.client', value = 'jdk')
class MicronautSecurityIntegrationSpec extends Specification {

    @Inject Gru gru
    @Inject TokenGenerator tokenGenerator
    @Inject ClaimsGenerator claimsGenerator

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
            String token = tokenGenerator.generateToken(claimsGenerator.generateClaimsSet([sub: 'sherlock'], 3600)).get()
        expect:
            gru.test {
                post '/console/execute/result', {
                    headers Authorization: "Bearer $token".toString()
                    content inline('"Hello World"'), 'text/groovy'
                }
                expect {
                    text inline('Hello World')
                }
            }
    }

}

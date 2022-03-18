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
package com.agorapulse.micronaut.console.http

import com.agorapulse.gru.Gru
import com.agorapulse.gru.http.Http
import io.micronaut.context.ApplicationContext
import io.micronaut.runtime.server.EmbeddedServer
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

class ConsoleControllerSpec extends Specification {

    private static final String USER = 'someuser'

    @Shared @AutoCleanup ApplicationContext context
    @Shared @AutoCleanup EmbeddedServer server

    @AutoCleanup Gru gru = Gru.create(Http.create(this))

    void setupSpec() {
        context = ApplicationContext.builder(
            'console.addresses': '/127.0.0.1',
            'console.users': USER
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
                    headers 'X-Console-User': USER
                    content inline('"Hello world!"'), 'text/groovy'
                }
                expect {
                    json result: 'Hello world!'
                }
            }
    }

    void 'execute a simple groovy script without valid user'() {
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

    void 'execute groovy script which uses context'() {
        expect:
            gru.test {
                post('/console/execute') {
                    headers 'X-Console-User': USER
                    content 'inception.groovy', 'text/groovy'
                }
                expect {
                    json 'inception.json'
                }
            }
    }

    void 'execute groovy script which throws exception'() {
        expect:
            gru.test {
                post('/console/execute') {
                    headers 'X-Console-User': USER
                    content 'exceptional.groovy', 'text/groovy'
                }
                expect {
                    status BAD_REQUEST
                    json 'exceptional.json'
                }
            }
    }

    void 'execute groovy script which throws exception and see the result'() {
        expect:
            gru.test {
                post('/console/execute/result') {
                    headers 'X-Console-User': USER
                    content 'exceptional.groovy', 'text/groovy'
                }
                expect {
                    status BAD_REQUEST
                    text 'exceptional.txt'
                }
            }
    }

    void 'execute groovy script which prints to the console'() {
        expect:
            gru.test {
                post('/console/execute') {
                    headers 'X-Console-User': USER
                    content 'printer.groovy', 'text/groovy'
                }
                expect {
                    json 'printer.json'
                }
            }
    }

    void 'execute js script which prints to the console'() {
        expect:
            gru.test {
                post('/console/execute') {
                    headers 'X-Console-User': USER
                    content 'jsr223.js', 'text/ecmascript'
                }
                expect {
                    json 'jsr223.json'
                }
            }
    }

    void 'execute groovy script which prints to the console and return text'() {
        expect:
            gru.test {
                post('/console/execute/result') {
                    headers 'X-Console-User': USER
                    content 'printer.groovy', 'text/groovy'
                }
                expect {
                    text 'printer.txt'
                }
            }
    }

    void 'execute unauthorized script'() {
        expect:
            gru.test {
                post('/console/execute') {
                    headers 'X-Console-User': USER
                    content inline('"Hello Voldemort"'), 'text/spellwords'
                }
                expect {
                    status UNAUTHORIZED
                }
            }
    }

    void 'execute script with unknown language'() {
        expect:
            gru.test {
                post('/console/execute') {
                    headers 'X-Console-User': USER
                    content inline('abrakadabra'), 'text/spell'
                }
                expect {
                    status BAD_REQUEST
                }
            }
    }

    void 'get gdsl script'() {
        expect:
            gru.test {
                get('/console/dsl/gdsl')
                expect {
                    text 'generated.gdsl'
                }
            }
    }

    void 'get dsld script'() {
        expect:
            gru.test {
                get('/console/dsl/dsld')
                expect {
                    text 'generated.dsld'
                }
            }
    }

    void 'get text binding summary'() {
        expect:
            gru.test {
                get('/console/dsl/text')
                expect {
                    text 'generated.txt'
                }
            }
    }

}

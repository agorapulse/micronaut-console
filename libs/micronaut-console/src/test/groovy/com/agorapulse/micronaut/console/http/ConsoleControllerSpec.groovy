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
package com.agorapulse.micronaut.console.http

import com.agorapulse.gru.Gru
import groovy.transform.CompileDynamic
import io.micronaut.context.annotation.Property
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.AutoCleanup
import spock.lang.Requires
import spock.lang.Specification

@MicronautTest
@CompileDynamic
@Property(name = 'console.enabled', value = 'true')
@Property(name = 'console.addresses', value =  '/127.0.0.1')
@Property(name = 'console.users', value =  USER)
@Property(name = 'micronaut.security.enabled', value =  'false')
class ConsoleControllerSpec extends Specification {

    private static final String USER = 'someuser'

    @AutoCleanup @Inject Gru gru

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

    @Requires({
        System.getProperty('java.version').startsWith('1.8')
    })
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

    void 'execute groovy script which blocks flux'() {
        expect:
        gru.test {
            post('/console/execute/result') {
                headers 'X-Console-User': USER
                content 'blocker.groovy', 'text/groovy'
            }
            expect {
                text 'blocker.txt'
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

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
import com.agorapulse.gru.micronaut.Micronaut
import spock.lang.AutoCleanup
import spock.lang.Specification

class ConsoleHeadersFilterSpec extends Specification {

    @AutoCleanup Gru gru

    void 'header missing'() {
        given:
            gru = Gru.create(Micronaut.build(this) {
                properties(
                    'console.enabled': true,
                    'console.header-name': 'X-Console-Verification'
                )
            }.start())
        expect:
            gru.test {
                post('/console/execute') {
                    content inline('"Hello world!"'), 'text/groovy'
                }
                expect {
                    status FORBIDDEN
                }
            }
    }

    void 'header present but the header value not set in the configuration'() {
        given:
            gru = Gru.create(Micronaut.build(this) {
                properties(
                    'console.enabled': true,
                    'console.header-name': 'X-Console-Verification'
                )
            }.start())
        expect:
            gru.test {
                post('/console/execute') {
                    headers 'X-Console-Verification': 'Hello'
                    content inline('"Hello world!"'), 'text/groovy'
                }
                expect {
                    status FORBIDDEN
                }
            }
    }

    void 'appropriate header value present'() {
        given:
            gru = Gru.create(Micronaut.build(this) {
                properties(
                    'console.enabled': true,
                    'console.header-name': 'X-Console-Verification',
                    'console.header-value': 'Paul is King'
                )
            }.start())
        expect:
            gru.test {
                post('/console/execute') {
                    headers 'X-Console-Verification': 'Paul is King'
                    content inline('"Hello world!"'), 'text/groovy'
                }
            }
    }

    void 'inappropriate header value present'() {
        given:
            gru = Gru.create(Micronaut.build(this) {
                properties(
                    'console.enabled': true,
                    'console.header-name': 'X-Console-Verification',
                    'console.header-value': 'Paul is King'
                )
            }.start())
        expect:
            gru.test {
                post('/console/execute') {
                    headers 'X-Console-Verification': 'Vlad is King'
                    content inline('"Hello world!"'), 'text/groovy'
                }
                expect {
                    status FORBIDDEN
                }
            }
    }

}

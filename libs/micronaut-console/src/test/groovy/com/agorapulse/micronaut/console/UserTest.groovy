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
package com.agorapulse.micronaut.console

import groovy.transform.CompileDynamic
import spock.lang.Specification
import spock.lang.Unroll

@CompileDynamic
class UserTest extends Specification {

    @Unroll
    @SuppressWarnings('MethodName')
    void 'user "#user" is printed correctly as "#expected"'() {
        expect:
            expected == user.toString()
        where:
            expected                                | user
            'First Last (first_last) @ 127.0.0.1'   | new User('first_last', 'First Last', '127.0.0.1')
            'first_last @ 127.0.0.1'                | new User('first_last', null, '127.0.0.1')
            'first_last'                            | new User('first_last', null, null)
            'Anonymous'                             | new User(null, null, null)
            'First Last'                            | new User(null, 'First Last', null)
            'Anonymous @ 127.0.0.1'                 | new User(null, null, '127.0.0.1')
            'First Last @ 127.0.0.1'                | new User(null, 'First Last', '127.0.0.1')
    }

}

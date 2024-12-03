/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2024 Agorapulse.
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

import spock.lang.Specification
import spock.lang.Unroll

class BindingProviderSpec extends Specification {

    @Unroll
    void 'class #type is purified as #purified'() {
        expect:
            BindingProvider.purifyClassName(type) == purified

        where:
            type                                                    | purified
            // normal class or interface
            'com.example.Foo'                                       | 'com.example.Foo'
            // clients, repositories, etc.
            'com.example.Foo$Intercepted'                           | 'com.example.Foo'
            'com.example.$Foo$Definition$Intercepted'               | 'com.example.Foo'
            // intercepted classes or interfaces e.g. Validable
            'com.example.$FooDefinition$Intercepted'                | 'com.example.Foo'
            'com.example.FooDefinition$Intercepted'                 | 'com.example.FooDefinition'
            'com.example.$FooDefinition$Definition$Intercepted'     | 'com.example.FooDefinition'
            // nested classes
            'com.example.Foo$Bar'                                   | 'com.example.Foo.Bar'
    }

}

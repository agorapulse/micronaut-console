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
package com.agorapulse.micronaut.console.http

import com.agorapulse.micronaut.console.BindingProvider
import com.agorapulse.micronaut.console.http.ParentClass.ChildService
import groovy.transform.CompileStatic

import jakarta.inject.Singleton

@Singleton
@CompileStatic
class DemoBindingProvider implements BindingProvider {

    private final InterceptedInterface service
    private final ChildService childService

    DemoBindingProvider(InterceptedInterface service, ChildService childService) {
        this.service = service
        this.childService = childService
    }

    @Override
    Map<String, ?> getBinding() {
        return [
            service: service,
            child: childService,
        ]
    }

}

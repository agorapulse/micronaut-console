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
package com.agorapulse.micronaut.console.http;

import io.micronaut.http.HttpRequest;
import io.micronaut.runtime.http.scope.RequestAware;
import io.micronaut.runtime.http.scope.RequestScope;

@RequestScope
class RequestHolder implements RequestAware {

    private HttpRequest<?> request;

    public HttpRequest<?> getRequest() {
        return request;
    }

    @Override
    public void setRequest(HttpRequest<?> request) {
        this.request = request;
    }

}

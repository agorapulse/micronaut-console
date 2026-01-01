/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2026 Agorapulse.
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

import com.agorapulse.micronaut.console.BindingProvider;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.env.Environment;
import io.micronaut.http.HttpRequest;
import io.micronaut.runtime.server.EmbeddedServer;

import jakarta.inject.Provider;
import jakarta.inject.Singleton;
import java.util.Collections;
import java.util.Map;

@Singleton
@Requires(beans = EmbeddedServer.class, notEnv = Environment.FUNCTION)
public class RequestBindingProvider implements BindingProvider {

    private final Provider<RequestHolder> holderProvider;

    public RequestBindingProvider(Provider<RequestHolder> holderProvider) {
        this.holderProvider = holderProvider;
    }

    @Override
    public Map<String, ?> getBinding() {
        RequestHolder holder = holderProvider.get();
        if (holder != null) {
            HttpRequest<?> request = holder.getRequest();
            if (request != null) {
                return Collections.singletonMap("request", request);
            }
        }
        return Collections.emptyMap();
    }
}

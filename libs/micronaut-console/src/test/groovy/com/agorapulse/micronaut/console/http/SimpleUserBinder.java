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

import com.agorapulse.micronaut.console.User;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.bind.binders.TypedRequestArgumentBinder;

import javax.inject.Singleton;

@Singleton
@Replaces(AnonymousUserArgumentBinder.class)
public class SimpleUserBinder implements TypedRequestArgumentBinder<User> {

    private final AnonymousUserArgumentBinder delegate = new AnonymousUserArgumentBinder();

    @Override
    public Argument<User> argumentType() {
        return delegate.argumentType();
    }

    @Override
    public BindingResult<User> bind(ArgumentConversionContext<User> context, HttpRequest<?> source) {
        return () -> delegate.bind(context, source).getValue().map(u -> new User(
            source.getHeaders().get("X-Console-User"),
            source.getHeaders().get("X-Console-Name"),
            u.getAddress()
        ));
    }
}

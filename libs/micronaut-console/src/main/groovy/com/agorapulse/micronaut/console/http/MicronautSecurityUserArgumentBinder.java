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
package com.agorapulse.micronaut.console.http;

import com.agorapulse.micronaut.console.User;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.bind.ArgumentBinder;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.bind.binders.TypedRequestArgumentBinder;

import javax.inject.Singleton;
import java.security.Principal;
import java.util.Optional;

@Singleton
@Replaces(AnonymousUserArgumentBinder.class)
@Requires(property = "micronaut.security.enabled", value = "true")
public class MicronautSecurityUserArgumentBinder implements TypedRequestArgumentBinder<User> {

    private final AnonymousUserArgumentBinder anonymousUserArgumentBinder;

    public MicronautSecurityUserArgumentBinder(AnonymousUserArgumentBinder anonymousUserArgumentBinder) {
        this.anonymousUserArgumentBinder = anonymousUserArgumentBinder;
    }

    @Override
    public Argument<User> argumentType() {
        return Argument.of(User.class);
    }

    @Override
    public BindingResult<User> bind(ArgumentConversionContext<User> context, HttpRequest<?> source) {
        if (source.getAttributes().contains("micronaut.once.SecurityFilter")) {
            final Optional<Principal> existing = source.getUserPrincipal();
            if (existing.isPresent()) {
                return () -> Optional.of(
                    new User(
                        existing.get().getName(),
                        null,
                        source.getRemoteAddress().getAddress().toString()
                    )
                );
            }
            return anonymousUserArgumentBinder.bind(context, source);
        }

        return ArgumentBinder.BindingResult.EMPTY;
    }

}

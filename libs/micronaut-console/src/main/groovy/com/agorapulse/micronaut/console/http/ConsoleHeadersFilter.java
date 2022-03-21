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
package com.agorapulse.micronaut.console.http;

import com.agorapulse.micronaut.console.ConsoleConfiguration;
import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpMethod;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.filter.FilterChain;
import io.micronaut.http.filter.HttpFilter;
import io.reactivex.Flowable;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Filter("${console.path:/console}/**")
@Requires(property = "console.header-name")
public class ConsoleHeadersFilter implements HttpFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsoleHeadersFilter.class);

    private final ConsoleConfiguration configuration;

    public ConsoleHeadersFilter(ConsoleConfiguration configuration) {
        this.configuration = configuration;

        if (configuration.getHeaderValue() == null) {
            LOGGER.error("Missing 'console.header-value' configuration value. All header checks will fail!");
        }
    }

    @Override
    public Publisher<? extends HttpResponse<?>> doFilter(HttpRequest<?> request, FilterChain chain) {
        return Flowable.just(request)
            .switchMap(req -> {
                if (HttpMethod.POST.equals(req.getMethod())) {
                    String headerValue = req.getHeaders().get(configuration.getHeaderName());
                    if (headerValue == null) {
                        return Flowable.just(HttpResponse.status(HttpStatus.FORBIDDEN, "Missing verification header"));
                    }

                    if (!headerValue.equals(configuration.getHeaderValue())) {
                        return Flowable.just(HttpResponse.status(HttpStatus.FORBIDDEN, "Wrong value of the verification header"));
                    }
                }

                return chain.proceed(req);
            });
    }

}

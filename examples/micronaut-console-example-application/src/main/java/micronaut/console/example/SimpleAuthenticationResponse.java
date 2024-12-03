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
package micronaut.console.example;

import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.authentication.AuthenticationResponse;

import java.util.Optional;

public class SimpleAuthenticationResponse implements AuthenticationResponse {

    private final Authentication authentication;

    public SimpleAuthenticationResponse(Authentication authentication) {
        this.authentication = authentication;
    }

    // @Override Micronaut 3 compatibility
    public Optional<Authentication> getAuthentication() {
        return Optional.of(authentication);
    }

    @Override
    public boolean isAuthenticated() {
        return true;
    }

    @Override
    public Optional<String> getMessage() {
        return Optional.empty();
    }

}

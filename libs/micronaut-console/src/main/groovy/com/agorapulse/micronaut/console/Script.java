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
package com.agorapulse.micronaut.console;

/**
 * The representation of the script.
 */
public class Script {

    private final String language;
    private final String body;
    private final User user;

    public Script(String language, String body, User user) {
        this.language = language;
        this.body = body;
        this.user = user;
    }

    /**
     * @return the language of the script
     */
    public String getLanguage() {
        return language;
    }

    /**
     * @return the body of the script
     */
    public String getBody() {
        return body;
    }

    /**
     * @return the user asking for the script execution
     */
    public User getUser() {
        return user;
    }

    @Override
    public String toString() {
        return "Language: " + language + "\nUser: " + user + "\n\n" + body;
    }
}

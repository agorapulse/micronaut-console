/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020 Agorapulse.
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

import io.micronaut.context.annotation.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties("console")
public class ConsoleConfiguration {

    private String language = "groovy";
    private List<String> addresses = new ArrayList<>();
    private List<String> users = new ArrayList<>();

    /**
     * @return the default language to be used if unspecified
     */
    public String getLanguage() {
        return language;
    }

    /**
     * @param language the default language to be used if unspecified
     */
    public void setLanguage(String language) {
        this.language = language;
    }

    /**
     * @return the list of allowed addresses
     */
    public List<String> getAddresses() {
        return addresses;
    }

    /**
     * @param addresses the list of allowed addresses
     */
    public void setAddresses(List<String> addresses) {
        this.addresses = addresses;
    }

    /**
     * @return the list of allowed user ids
     */
    public List<String> getUsers() {
        return users;
    }

    /**
     * @param users the list of allowed user ids
     */
    public void setUsers(List<String> users) {
        this.users = users;
    }

}

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
package com.agorapulse.micronaut.console;

public class User {

    public static User anonymous() {
        return new User(null, null, null);
    }

    private final String id;
    private final String name;
    private final String address;

    public User(String id, String name, String address) {
        this.id = id;
        this.name = name;
        this.address = address;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        if (name != null) {
            builder.append(name);
            if (id != null) {
                builder.append(" (").append(id).append(')');
            }
        } else if (id != null) {
            builder.append(id);
        } else {
            builder.append("Anonymous");
        }

        if (address != null) {
            builder.append(" @ ").append(address);
        }

        return builder.toString();
    }
}

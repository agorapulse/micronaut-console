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

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.core.convert.ConversionService;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@ConfigurationProperties("console")
public class ConsoleConfiguration {

    private boolean enabled;
    private String language = "groovy";
    private List<String> addresses = new ArrayList<>();
    private List<String> users = new ArrayList<>();
    private Object until;
    private String headerName;
    private String headerValue;

    /**
     * @return true if the console should be enabled in the <code>cloud</code> environment
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * @param enabled true if the console should be enabled in the <code>cloud</code> environment
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

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

    /**
     * @return the date and time until the console is enabled which can be used for temporary access
     */
    public Object getUntil() {
        return until;
    }

    /**
     * @param until the date and time until the console is enabled which can be used for temporary access
     */
    public void setUntil(Object until) {
        this.until = until;
    }

    public Instant convertUntil() {
        if (until == null) {
            return null;
        }
        if (until instanceof CharSequence) {
            return Instant.parse((CharSequence) until);
        }
        if (until instanceof Date) {
            return ((Date)until).toInstant();
        }
        if (until instanceof OffsetDateTime) {
            return ((OffsetDateTime) until).toInstant();
        }
        return ConversionService.SHARED.convert(until, Instant.class).orElseThrow(() ->
            new IllegalArgumentException("Cannot convert " + until + " (" + until.getClass() + ") to Instant")
        );
    }

    /**
     * @return the name of the header which must be present for any POST calls
     */
    public String getHeaderName() {
        return headerName;
    }

    /**
     * @param headerName the name of the header which must be present for any POST calls
     */
    public void setHeaderName(String headerName) {
        this.headerName = headerName;
    }

    /**
     * @return the required value of the header if header check is configured, only presence of the header is checked if <code>null</code>
     */
    public String getHeaderValue() {
        return headerValue;
    }

    /**
     * @param headerValue the required value of the header if header check is configured, only presence of the header is checked if <code>null</code>
     */
    public void setHeaderValue(String headerValue) {
        this.headerValue = headerValue;
    }
}

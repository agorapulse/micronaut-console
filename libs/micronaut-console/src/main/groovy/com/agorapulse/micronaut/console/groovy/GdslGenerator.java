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
package com.agorapulse.micronaut.console.groovy;

import com.agorapulse.micronaut.console.BindingProvider;
import com.agorapulse.micronaut.console.ide.DslGenerator;

import javax.inject.Singleton;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

@Singleton
public class GdslGenerator implements DslGenerator {

    private final List<BindingProvider> bindingProviders;

    public GdslGenerator(List<BindingProvider> bindingProviders) {
        this.bindingProviders = bindingProviders;
    }

    @Override
    public String generateScript() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        pw.println("def ctx = context(scriptScope())");
        pw.println();
        pw.println("contribute(ctx) {");
        bindingProviders.forEach(p -> p.getBinding().forEach((key, value) -> {
                if (value != null) {
                    pw.println("    property(name: '" + key + "', type: '" + value.getClass().getName() + "')");
                }
            }));
        pw.println("}");

        return sw.toString();
    }

    @Override
    public String getScriptType() {
        return "gdsl";
    }
}

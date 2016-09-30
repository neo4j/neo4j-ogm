/*
 * Copyright (c) 2002-2016 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 *  conditions of the subcomponent's license, as noted in the LICENSE file.
 */

package org.neo4j.ogm.config;

/**
 * @author vince
 */
public class CompilerConfiguration {

    public static final String[] COMPILER = {"neo4j.ogm.compiler", "compiler"};
    private static final String DEFAULT_COMPILER_CLASS_NAME = "org.neo4j.ogm.compiler.MultiStatementCypherCompiler";

    private final Configuration configuration;

    public CompilerConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    public CompilerConfiguration setCompilerClassName(String compilerClassName) {
        configuration.set(COMPILER[0], compilerClassName);
        return this;
    }

    public String getCompilerClassName() {
        if (configuration.get(COMPILER) == null) {
            return DEFAULT_COMPILER_CLASS_NAME;
        }
        return (String) configuration.get(COMPILER);
    }
}

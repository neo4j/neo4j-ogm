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

package org.neo4j.ogm.service;

import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

import org.neo4j.ogm.compiler.Compiler;
import org.neo4j.ogm.config.CompilerConfiguration;
import org.neo4j.ogm.exception.ServiceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loads a Cypher Compiler for use by the OGM to construct Cypher statements when mapping entities
 * to the graph.
 *
 * Different versions of Neo4j can take advantage of different cypher constructs. This service
 * allows the OGM to load the compiler service appropriate for the database version being used / under test
 *
 * The DefaultCypherCompiler is guaranteed to generate cypher that will work with all versions of Neo4j
 * post 2.0, and is automatically registered with the name "default".
 *
 * In the event that a requested compiler cannot be found, the default one will be selected
 *
 * @author vince
 */
abstract class CompilerService {

    private static final Logger logger = LoggerFactory.getLogger(CompilerService.class);

    /**
     * Using this method, you can load a Driver as a service provider by specifying its
     * fully qualified class name.
     *
     * @param className the fully qualified class name of the required Driver
     * @return the named Driver if found, otherwise throws a ServiceNotFoundException
     */
    private static org.neo4j.ogm.compiler.Compiler load(String className) {

        for (Compiler compiler : ServiceLoader.load(Compiler.class)) {
            try {
                if (compiler.getClass().getName().equals(className)) {
                    return compiler;
                }
            } catch (ServiceConfigurationError sce) {
                logger.warn("{}, reason: {}", sce.getLocalizedMessage(), sce.getCause());
            }
        }

        throw new ServiceNotFoundException("Compiler: " + className);

    }

    /**
     * Loads and initialises a Cypher Compiler using the specified CompilerConfiguration
     *
     * @param configuration an instance of {@link CompilerConfiguration} with which to configure the driver
     * @return the named {@link Compiler} if found, otherwise throws a ServiceNotFoundException
     */
    public static Compiler load(CompilerConfiguration configuration) {
        String compilerClassName = configuration.getCompilerClassName();
        return load(compilerClassName);
    }

}

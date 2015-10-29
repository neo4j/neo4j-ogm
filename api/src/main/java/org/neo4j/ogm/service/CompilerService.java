package org.neo4j.ogm.service;

import org.neo4j.ogm.compiler.Compiler;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.exception.ServiceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

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

        Iterator<Compiler> iterator = ServiceLoader.load(Compiler.class).iterator();

        while (iterator.hasNext()) {
            try {
                Compiler compiler = iterator.next();
                if (compiler.getClass().getName().equals(className)) {
                    return compiler;
                }
            } catch (ServiceConfigurationError sce) {
                logger.warn(sce.getLocalizedMessage() + ", reason: " + sce.getCause());
            }
        }

        throw new ServiceNotFoundException(className);

    }

    /**
     * Using this method you can load a Driver using its registered name.
     * The registered name for a driver is specified by the "class" property in the Driver's config file,
     * and the value should be the fully qualified class name of the implementing Driver class.
     *
     * @param registeredName the Driver's registered name
     * @return the required Driver if found, otherwise throws a ServiceNotFoundException
     */
    public static Compiler lookup(String registeredName) {

        Configuration config = new Configuration(registeredName + ".compiler.properties");
        String fqn = (String) config.getConfig("class");
        if (fqn != null) {
            return load(fqn);
        }

        throw new ServiceNotFoundException(registeredName);
    }
}

package org.neo4j.ogm.testutil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Supplier;

import org.neo4j.harness.Neo4jBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wraps the instantiation of either community or enterprise test harness.
 *
 * @author Michael J. Simons
 */
public final class TestHarnessSupplier implements Supplier<Neo4jBuilder> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestHarnessSupplier.class);

    /**
     * @return the TestServerBuilder based on what is present on classpath
     */
    public Neo4jBuilder get() {

        // Use reflection here so there is no compile time dependency on neo4j-harness-enterprise
        Neo4jBuilder builder;
        builder = instantiate("com.neo4j.harness.EnterpriseNeo4jBuilders");
        if (builder == null) {
            builder = instantiate("org.neo4j.harness.Neo4jBuilders");
        }

        if (builder == null) {
            throw new RuntimeException("Could not get one of Neo4jBuilders or EnterpriseNeo4jBuilders!");
        }

        LOGGER.info("Using {} for providing a Neo4j test harness", builder.getClass());
        return builder;
    }

    private static Neo4jBuilder instantiate(String className) {
        try {
            Class<?> builderClass = Class.forName(className);
            Method newInProcessBuilder = builderClass.getMethod("newInProcessBuilder");
            return (Neo4jBuilder) newInProcessBuilder.invoke(null);
        } catch (IllegalAccessException | ClassNotFoundException | NoSuchMethodException | InvocationTargetException e) {
            LOGGER.trace("Could not load {}", className, e);
        }
        return null;
    }
}

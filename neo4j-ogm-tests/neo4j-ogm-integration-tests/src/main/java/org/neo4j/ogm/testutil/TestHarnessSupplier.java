package org.neo4j.ogm.testutil;

import java.util.function.Supplier;

import org.neo4j.harness.TestServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wraps the instantiation of either community or enterprise test harness.
 *
 * @author Michael J. Simons
 */
public final class TestHarnessSupplier implements Supplier<TestServerBuilder> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestHarnessSupplier.class);

    /**
     * @Return the TestServerBuilder based on what is present on classpath
     */
    public TestServerBuilder get() {

        // Use reflection here so there is no compile time dependency on neo4j-harness-enterprise

        TestServerBuilder builder;
        builder = instantiate("org.neo4j.harness.internal.EnterpriseInProcessServerBuilder");
        if (builder == null) {
            // class name for Neo4j 3.1
            builder = instantiate("org.neo4j.harness.EnterpriseInProcessServerBuilder");
        }
        if (builder == null) {
            builder = instantiate("org.neo4j.harness.internal.InProcessServerBuilder");
        }
        /*
         The property "unsupported.dbms.jmx_module.enabled=false" disables JMX monitoring
         We may start multiple instances of the server and without disabling this the 2nd instance would not start.
         */
        builder = builder.withConfig("unsupported.dbms.jmx_module.enabled", "false");
        LOGGER.info("Creating new instance of {}", builder.getClass());
        return builder;
    }

    private static TestServerBuilder instantiate(String className) {
        TestServerBuilder builder = null;
        try {
            builder = ((TestServerBuilder) Class.forName(className)
                .newInstance());
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            LOGGER.trace("Could not load {}", className, e);
        }
        return builder;
    }
}

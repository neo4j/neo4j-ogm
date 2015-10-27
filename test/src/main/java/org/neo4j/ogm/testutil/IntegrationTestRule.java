/*
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 * conditions of the subcomponent's license, as noted in the LICENSE file.
 *
 */

package org.neo4j.ogm.testutil;

import org.junit.Assert;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.ogm.api.driver.Driver;
import org.neo4j.ogm.api.service.Components;

/**
 * JUnit {@link TestRule} that provides a {@link GraphDatabaseService} to its enclosing test harness via both the object itself
 * and a remote server running on a local port.  The port is configurable via this rule's constructor.
 * <p>
 * You can use this as a normal rule or as a class-level rule depending on your needs. Class-level use means you'll get one
 * database for the whole test class and is therefore normally faster.
 * <pre>
 * public class MyJUnitTest {
 *
 *     &#064;ClassRule
 *     public static IntegrationTestRule testServer = new IntegrationTestRule(Components.driver());
 *
 * }
 * </pre>
 * If you need a clean database for each individual test method then you can simply include this as a non-static rule instead:
 * <pre>
 * public class MyOtherJUnitTest {
 *
 *     &#064;Rule
 *     public IntegrationTestRule testServer = new IntegrationTestRule(Components.driver());
 *
 * }
 * </pre>
 * You can call methods on this rule from within your test methods to facilitate writing integration tests.
 *
 * @author Adam George
 */
public class IntegrationTestRule extends TestServer implements TestRule {

    public IntegrationTestRule() {
        super(Components.driver());
    }

    public IntegrationTestRule(Driver driver) {
        super(driver);
    }

    public IntegrationTestRule(Driver driver, int portNumber) {
        super(driver, portNumber);
    }

    @Override
    public Statement apply(final Statement baseStatement, Description description) {

        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                try {
                    if (isRunning(1000)) {
                        baseStatement.evaluate();
                    } else {
                        Assert.fail("Database was shut down or didn't become available within 1s");
                    }
                } finally {
                    shutdown();
                }
            }
        };
    }

}

/*
 * Copyright (c) 2002-2018 "Neo Technology,"
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

package org.neo4j.ogm.context;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.driver.v1.Config;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.harness.ServerControls;
import org.neo4j.harness.TestServerBuilders;
import org.neo4j.ogm.drivers.bolt.driver.BoltDriver;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.procedure.Mode;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.Procedure;

/**
 * @author Michael J. Simons
 */
public class ResponseMapperTest {
    private static final Config driverConfig = Config.build().withoutEncryption().toConfig();

    private static ServerControls serverControls;
    private static URI boltURI;

    @BeforeClass
    public static void initializeNeo4j() throws IOException {

        serverControls = TestServerBuilders.newInProcessBuilder().withProcedure(ApocLovesSwitch.class)
            .newServer();
        boltURI = serverControls.boltURI();
    }

    @Test // GH-479
    public void shouldIgnoreResultsIfQueryMapsToVoid() {
        SessionFactory sessionFactory = null;
        try (Driver driver = GraphDatabase.driver(boltURI, driverConfig)) {
            sessionFactory = new SessionFactory(new BoltDriver(driver), ResponseMapperTest.class.getName());
            Session session = sessionFactory.openSession();
            // The call would have been successfull if the procedure would have returned a stream of
            // things with at max 1 attribute, but in the end, that would be what was requested for.
            session.query(void.class, "CALL apoc.periodic.iterate('MATCH (d:Document) RETURN d', "
                + " 'SET d.thisIsAProperty = 0'"
                + " ,{batchSize:200, parallel:false, iterateList:true}) ", new HashMap<>());
        } finally {
            if (sessionFactory != null) {
                sessionFactory.close();
            }
        }
    }

    @AfterClass
    public static void tearDownNeo4j() {
        serverControls.close();
    }

    public static class ApocLovesSwitch {
        @Procedure(name = "apoc.periodic.iterate", mode = Mode.WRITE)
        public Stream<BatchAndTotalResult> iterate(
            @Name("cypherIterate") String cypherIterate,
            @Name("cypherAction") String cypherAction,
            @Name("config") Map<String, Object> config) {

            return Stream.of(new BatchAndTotalResult());
        }
    }

    public static class BatchAndTotalResult {
        public final long batches;
        public final long total;
        public final long timeTaken;

        public BatchAndTotalResult() {

            ThreadLocalRandom random = ThreadLocalRandom.current();
            this.batches = random.nextLong();
            this.total = random.nextLong();
            this.timeTaken = random.nextLong();
        }
    }
}

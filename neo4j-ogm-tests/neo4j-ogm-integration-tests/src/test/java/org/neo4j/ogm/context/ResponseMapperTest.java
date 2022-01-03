/*
 * Copyright (c) 2002-2022 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neo4j.ogm.context;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.driver.Config;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;
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
    private static final Config driverConfig = Config.builder().withoutEncryption().build();

    private static Neo4j serverControls;
    private static URI boltURI;

    @BeforeClass
    public static void initializeNeo4j() {

        serverControls = Neo4jBuilders.newInProcessBuilder().withProcedure(ApocLovesSwitch.class)
            .build();
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
            session.query("CALL apoc.periodic.iterate('MATCH (d:Document) RETURN d', "
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

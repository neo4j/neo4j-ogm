/*
 * Copyright (c) 2002-2021 "Neo4j,"
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
package org.neo4j.ogm.persistence.types.nativetypes;

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assume.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.drivers.embedded.driver.EmbeddedDriver;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.values.storable.DurationValue;

/**
 * @author Gerrit Meier
 * @author Michael J. Simons
 */
public class DatesEmbeddedTest extends DatesTestBase {

    @BeforeClass
    public static void init() {

        assumeTrue(isEmbeddedDriver());

        Configuration ogmConfiguration = getBaseConfigurationBuilder()
            .useNativeTypes()
            .build();

        EmbeddedDriver embeddedOgmDriver = new EmbeddedDriver();
        embeddedOgmDriver.configure(ogmConfiguration);
        sessionFactory = new SessionFactory(embeddedOgmDriver, SpatialEmbeddedTest.class.getPackage().getName());
    }

    @Override
    public void convertPersistAndLoadTemporalAmounts() {
        Session session = sessionFactory.openSession();

        long id = session.queryForObject(
            Long.class,
            "CREATE (s:`DatesTestBase$Sometime` {temporalAmount: duration('P13Y370M45DT25H120M')}) RETURN id(s)",
            Collections.emptyMap());
        session.clear();
        Sometime loaded = session.load(Sometime.class, id);

        assertThat(loaded.getTemporalAmount()).isEqualTo(DurationValue.parse("P13Y370M45DT25H120M"));
    }

    @Override
    public void shouldUseNativeDateTimeTypesInParameterMaps() {
        Session session = sessionFactory.openSession();

        LocalDate localDate = LocalDate.of(2018, 11, 14);
        LocalDateTime localDateTime = LocalDateTime.of(2018, 10, 11, 15, 24);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("a", localDate);
        parameters.put("b", localDateTime);
        session.query("CREATE (n:Test {a: $a, b: $b})", parameters);

        Map<String, Object> result = sessionFactory.unwrap(GraphDatabaseService.class)
            .executeTransactionally("MATCH (n:Test) RETURN n.a, n.b", Map.of(), r -> r.next());

        Object a = result.get("n.a");
        assertThat(a).isInstanceOf(LocalDate.class)
            .isEqualTo(localDate);

        Object b = result.get("n.b");
        assertThat(b).isInstanceOf(LocalDateTime.class)
            .isEqualTo(localDateTime);
    }

    @Override
    public void shouldObeyExplicitConversionOfNativeTypes() {

        Map<String, Object> params = createSometimeWithConvertedLocalDate();
        Map<String, Object> result = sessionFactory.unwrap(GraphDatabaseService.class)
            .executeTransactionally(
                "MATCH (n:`DatesTestBase$Sometime`) WHERE id(n) = $id RETURN n.convertedLocalDate AS convertedLocalDate",
                params, r -> r.next());

        Object a = result.get("convertedLocalDate");
        assertThat(a).isInstanceOf(String.class)
            .isEqualTo("2018-11-21");
    }
}

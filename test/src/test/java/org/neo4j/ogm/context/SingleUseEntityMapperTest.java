/*
 * Copyright (c) 2002-2019 "Neo4j,"
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

import static java.util.Collections.*;
import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.domain.gh551.ThingResult;
import org.neo4j.ogm.domain.gh552.Thing;
import org.neo4j.ogm.metadata.MetaData;
import org.neo4j.ogm.metadata.reflect.ReflectionEntityInstantiator;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.MultiDriverTestClass;

/**
 * @author Vince Bickers
 * @author Michael J. Simons
 */
public class SingleUseEntityMapperTest extends MultiDriverTestClass {

    private static SessionFactory sessionFactory;

    @BeforeClass
    public static void oneTimeSetUp() {
        sessionFactory = new SessionFactory(driver, "org.neo4j.ogm.domain.gh551");

        // Prepare test data
        sessionFactory.openSession()
            .query("unwind range(1,10) as x with x create (n:ThingEntity {name: 'Thing ' + x}) return n", EMPTY_MAP);
    }

    @Test // GH-551
    public void singleUseEntityMapperShouldWorkWithNestedObjects() {

        SingleUseEntityMapper entityMapper =
            new SingleUseEntityMapper(sessionFactory.metaData(),
                new ReflectionEntityInstantiator(sessionFactory.metaData()));

        Iterable<Map<String, Object>> results = sessionFactory.openSession()
            .query("MATCH (t:ThingEntity) RETURN 'a name' as something, collect({name: t.name}) as things", EMPTY_MAP)
            .queryResults();

        assertThat(results).hasSize(1);

        ThingResult thingResult = entityMapper.map(ThingResult.class, results.iterator().next());
        assertThat(thingResult.getSomething()).isEqualTo("a name");
        assertThat(thingResult.getThings().size()).isEqualTo(10);
        assertThat(thingResult.getThings())
            .extracting("name")
            .allSatisfy(s -> ((String) s).startsWith("Thing"));
    }

    @Test // GH-552
    public void shouldLookupCorrectRootClass() {
        MetaData metaData = new MetaData("org.neo4j.ogm.domain.gh552");
        String propertyKey = "notAName";
        Map<String, Object> properties = Collections.singletonMap(propertyKey, "NOT A NAME!!!");

        SingleUseEntityMapper entityMapper = new SingleUseEntityMapper(metaData, new ReflectionEntityInstantiator(metaData));
        Thing thing = entityMapper.map(Thing.class, properties);
        assertThat(thing.getNotAName()).isEqualTo(properties.get(propertyKey));
    }

    @Test
    public void shouldMapFromMap() {

        MetaData metaData = new MetaData("org.neo4j.ogm.context");
        SingleUseEntityMapper entityMapper = new SingleUseEntityMapper(metaData,
            new ReflectionEntityInstantiator(metaData));

        Collection<Object> toReturn = new ArrayList<>();

        Iterable<Map<String, Object>> results = getQueryResults();
        for (Map<String, Object> result : results) {
            toReturn.add(entityMapper.map(UserResult.class, result));
        }

        assertThat(toReturn).hasSize(1);
        assertThat(toReturn).first().isInstanceOf(UserResult.class);
        UserResult userResult = (UserResult) toReturn.iterator().next();
        assertThat(userResult.getProfile()).containsAllEntriesOf(
            (Map<? extends String, ?>) results.iterator().next().get("profile"));
    }

    private Iterable<Map<String, Object>> getQueryResults() {

        Map<String, Object> profile = new HashMap<>();
        profile.put("enterpriseId", "p.enterpriseId");
        profile.put("clidUuid", "u.clidUuid");
        profile.put("profileId", "p.clidUuid");
        profile.put("firstName", "p.firstName");
        profile.put("lastName", "p.lastName");
        profile.put("email", "u.email");
        profile.put("roles", "roles");
        profile.put("connectionType", "connectionType");

        return Collections.singletonList(Collections.singletonMap("profile", profile));
    }
}

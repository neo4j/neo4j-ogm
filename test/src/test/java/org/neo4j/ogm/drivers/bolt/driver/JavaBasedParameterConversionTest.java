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
package org.neo4j.ogm.drivers.bolt.driver;

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assume.*;
import static org.neo4j.ogm.driver.ParameterConversionMode.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.neo4j.driver.internal.value.DateTimeValue;
import org.neo4j.driver.internal.value.FloatValue;
import org.neo4j.driver.internal.value.ListValue;
import org.neo4j.driver.internal.value.MapValue;
import org.neo4j.driver.internal.value.StringValue;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Value;
import org.neo4j.driver.v1.types.TypeSystem;
import org.neo4j.ogm.driver.ParameterConversionMode;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.testutil.SingleDriverTestClass;

/**
 * @author Michael J. Simons
 */
public class JavaBasedParameterConversionTest extends SingleDriverTestClass {

    @Test
    public void shouldUseNativeTypesWhenNonNativeTypesOnlyIsActive() {

        assumeTrue(driverSupportsLocalDate());
        assumeTrue(databaseSupportJava8TimeTypes());

        Map<String, Object> customConfiguration = new HashMap<>();
        customConfiguration.put(ParameterConversionMode.CONFIG_PARAMETER_CONVERSION_MODE, CONVERT_NON_NATIVE_ONLY);

        try (Driver driver = getDriver()) {

            BoltDriver boltOgmDriver = new BoltDriver(driver, () -> customConfiguration);

            doWithSessionFactoryOf(boltOgmDriver, new Class[] { JavaBasedParameterConversionTest.class },
                sessionFactory -> {
                    Session session = sessionFactory.openSession();

                    LocalDateTime originalDateTime = LocalDateTime.of(2018, 10, 11, 15, 24);

                    Map<String, Object> parameters = new HashMap<>();
                    parameters.put("createdAt", originalDateTime);
                    session.query("CREATE (n:Test {createdAt: $createdAt})", parameters);

                    Record record = driver.session().run("MATCH (n:Test) RETURN n.createdAt as createdAt").single();
                    Object createdAt = record.get("createdAt").asObject();
                    assertThat(createdAt).isInstanceOf(LocalDateTime.class)
                        .isEqualTo(originalDateTime);
                });
        }
    }

    private static boolean driverSupportsLocalDate() {

        Class<TypeSystem> t = TypeSystem.class;
        try {
            return t.getDeclaredMethod("LOCAL_DATE_TIME") != null;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    @Test
    public void nestedConversions() {

        assumeTrue(driverSupportsLocalDate());
        assumeTrue(databaseSupportJava8TimeTypes());

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("aDate", ZonedDateTime.now());
        parameters.put("somethingElse", "Foobar");
        parameters.put("aDouble", 47.11);
        parameters.put("aNumber", new BigDecimal("42.23"));
        parameters.put("listOfDates", Arrays.asList(ZonedDateTime.now()));
        parameters.put("mapOfDates", Collections.singletonMap("aDate", ZonedDateTime.now()));
        parameters.put("arrayOfDates", new ZonedDateTime[] { ZonedDateTime.now() });

        Map<String, Object> convertedParameters = JavaDriverBasedParameterConversion.INSTANCE
            .convertParameters(parameters);

        assertThat(convertedParameters.keySet())
            .containsAll(parameters.keySet());
        assertThat(convertedParameters.get("aDate")).isInstanceOf(DateTimeValue.class);
        assertThat(convertedParameters.get("somethingElse"))
            .isInstanceOf(StringValue.class)
            .satisfies(v -> assertThat(((Value) v).asString()).isEqualTo("Foobar"));
        assertThat(convertedParameters.get("aDouble"))
            .isInstanceOf(FloatValue.class)
            .satisfies(v -> assertThat(((Value) v).asDouble()).isEqualTo(47.11));
        assertThat(convertedParameters.get("aNumber"))
            .isEqualTo(new BigDecimal("42.23"));
        assertThat(convertedParameters.get("listOfDates")).isInstanceOf(ListValue.class)
            .satisfies(v -> assertThat(((Value) v).asList(li -> li).get(0)).isInstanceOf(DateTimeValue.class));
        assertThat(convertedParameters.get("mapOfDates")).isInstanceOf(MapValue.class)
            .satisfies(v -> assertThat(((Value) v).asMap(li -> li).get("aDate")).isInstanceOf(DateTimeValue.class));
        assertThat(convertedParameters.get("arrayOfDates")).isInstanceOf(ListValue.class)
            .satisfies(v -> assertThat(((Value) v).asList(li -> li).get(0)).isInstanceOf(DateTimeValue.class));
    }
}

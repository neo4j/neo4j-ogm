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
package org.neo4j.ogm.dto;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.Driver;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.domain.dto.SomeDto;
import org.neo4j.ogm.domain.dto.SomeDtoRecord;
import org.neo4j.ogm.drivers.bolt.driver.BoltDriver;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.TestContainersTestBase;

/**
 * @author Gerrit Meier
 */
public class DtoMappingTest extends TestContainersTestBase {

    private static SessionFactory sessionFactory;
    private static Driver javaDriver;

    @BeforeAll
    public static void setupConnectionAndDatabase() {
        Configuration ogmConfiguration = getBaseConfigurationBuilder()
            .useNativeTypes()
            .build();

        BoltDriver boltOgmDriver = new BoltDriver();
        boltOgmDriver.configure(ogmConfiguration);
        sessionFactory = new SessionFactory(boltOgmDriver, "org.neo4j.ogm.domain.dto");
        javaDriver = boltOgmDriver.unwrap(Driver.class);
    }

    @Test
    void shouldMapSingleDto() {

        Session session = sessionFactory.openSession();
        List<SomeDto> dtos = session.queryDto("RETURN 'Hello' as valueA, 123 as valueB, date() as valueC", Map.of(), SomeDto.class);

        assertThat(dtos).hasSize(1);
        SomeDto dto = dtos.get(0);
        assertThat(dto.valueA).isEqualTo("Hello");
        assertThat(dto.valueB).isEqualTo(123);
        assertThat(dto.valueC).isInstanceOf(LocalDate.class);
    }

    @Test
    void shouldMapSingleDtoWithArrays() {

        Session session = sessionFactory.openSession();
        List<SomeDto> dtos = session.queryDto("RETURN ['a','b','c'] as valueD", Map.of(), SomeDto.class);

        assertThat(dtos).hasSize(1);
        SomeDto dto = dtos.get(0);
        assertThat(dto.valueD).containsExactly("a", "b", "c");
    }

    @Test
    void shouldMapCollectionOfDto() {
        try (var session = javaDriver.session()) {
            session.run("MATCH (n) detach delete n").consume();
            session.run("CREATE (m:Object{value1:'Hello', value2:123, value3:date()})").consume();
            session.run("CREATE (m:Object{value1:'Hello2', value2:1234, value3:date()})").consume();
        }

        Session session = sessionFactory.openSession();
        Iterable<SomeDto> dtos = session.queryDto("MATCH (o:Object) return o.value1 as valueA, o.value2 as valueB, o.value3 as valueC", Map.of(), SomeDto.class);

        assertThat(dtos).hasSize(2);
        assertThat(dtos).extracting("valueA").containsExactlyInAnyOrder("Hello", "Hello2");
        assertThat(dtos).extracting("valueB").containsExactlyInAnyOrder(123, 1234);
        assertThat(dtos).extracting("valueC").allSatisfy(o -> {
            assertThat(o).isNotNull();
            assertThat(o).isInstanceOf(LocalDate.class);
        });
    }

    @Test
    void shouldMapRecord() {

        Session session = sessionFactory.openSession();
        List<SomeDtoRecord> dtos = session.queryDto("RETURN 'Hello' as valueA, 123 as valueB, date() as valueC, ['a','b','c'] as valueD", Map.of(), SomeDtoRecord.class);

        assertThat(dtos).hasSize(1);
        SomeDtoRecord dto = dtos.get(0);
        assertThat(dto.valueA()).isEqualTo("Hello");
        assertThat(dto.valueB()).isEqualTo(123);
        assertThat(dto.valueC()).isInstanceOf(LocalDate.class);
    }
}

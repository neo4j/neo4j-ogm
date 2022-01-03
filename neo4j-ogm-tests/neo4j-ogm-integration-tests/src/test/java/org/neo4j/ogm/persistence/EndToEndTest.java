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
package org.neo4j.ogm.persistence;

import static org.assertj.core.api.Assertions.*;

import java.util.Arrays;
import java.util.HashMap;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.domain.bike.Bike;
import org.neo4j.ogm.domain.bike.Frame;
import org.neo4j.ogm.domain.bike.Saddle;
import org.neo4j.ogm.domain.bike.Wheel;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.TestContainersTestBase;

/**
 * @author Michal Bachman
 * @author Vince Bickers
 */
public class EndToEndTest extends TestContainersTestBase {

    private static SessionFactory sessionFactory;
    private Session session;

    @BeforeClass
    public static void oneTimeSetUp() {
        sessionFactory = new SessionFactory(getDriver(), "org.neo4j.ogm.domain.bike");
    }

    @Before
    public void init() {
        session = sessionFactory.openSession();
        session.purgeDatabase();
    }

    @Test
    public void canSimpleQueryDatabase() {
        Saddle expected = new Saddle();
        expected.setPrice(29.95);
        expected.setMaterial("Leather");
        Wheel frontWheel = new Wheel();
        Wheel backWheel = new Wheel();
        Bike bike = new Bike();
        bike.setBrand("Huffy");
        bike.setWheels(Arrays.asList(frontWheel, backWheel));
        bike.setSaddle(expected);
        session.save(bike);

        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("material", "Leather");
        Saddle actual = session
            .queryForObject(Saddle.class, "MATCH (saddle:Saddle{material: $material}) RETURN saddle", parameters);

        assertThat(actual.getId()).isEqualTo(expected.getId());
        assertThat(actual.getMaterial()).isEqualTo(expected.getMaterial());

        HashMap<String, Object> parameters2 = new HashMap<>();
        parameters2.put("brand", "Huffy");
        Bike actual2 = session.queryForObject(Bike.class, "MATCH (bike:Bike{brand: $brand}) RETURN bike", parameters2);

        assertThat(actual2.getId()).isEqualTo(bike.getId());
        assertThat(actual2.getBrand()).isEqualTo(bike.getBrand());
    }

    @Test
    public void canSimpleScalarQueryDatabase() {
        Saddle expected = new Saddle();
        expected.setPrice(29.95);
        expected.setMaterial("Leather");
        session.save(expected);

        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("material", "Leather");

        long actual = session
            .queryForObject(Long.class, "MATCH (saddle:Saddle{material: $material}) RETURN COUNT(saddle)", parameters);

        assertThat(actual).isEqualTo(1);
    }

    @Test
    public void canComplexQueryDatabase() {
        Saddle saddle = new Saddle();
        saddle.setPrice(29.95);
        saddle.setMaterial("Leather");
        Wheel frontWheel = new Wheel();
        Wheel backWheel = new Wheel();
        Bike bike = new Bike();
        bike.setBrand("Huffy");
        bike.setWheels(Arrays.asList(frontWheel, backWheel));
        bike.setSaddle(saddle);

        session.save(bike);

        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("brand", "Huffy");

        Bike actual = session.queryForObject(Bike.class,
            "MATCH (bike:Bike{brand:$brand})-[rels]-() RETURN bike, COLLECT(DISTINCT rels) as rels", parameters);

        assertThat(actual.getId()).isEqualTo(bike.getId());
        assertThat(actual.getBrand()).isEqualTo(bike.getBrand());
        assertThat(actual.getWheels().size()).isEqualTo(bike.getWheels().size());
        assertThat(actual.getSaddle()).isNotNull();
    }

    @Test
    public void canComplexExecute() {

        Saddle saddle = new Saddle();
        saddle.setPrice(29.95);
        saddle.setMaterial("Leather");

        Wheel frontWheel = new Wheel();
        Wheel backWheel = new Wheel();

        Bike bike = new Bike();
        bike.setBrand("Huffy");
        bike.setWheels(Arrays.asList(frontWheel, backWheel));
        bike.setSaddle(saddle);

        session.save(bike);

        Saddle newSaddle = new Saddle();
        newSaddle.setPrice(19.95);
        newSaddle.setMaterial("Vinyl");

        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("brand", "Huffy");
        parameters.put("saddle", newSaddle);

        session.query("MATCH (bike:Bike{brand:$brand})-[r]-(s:Saddle) SET s = $saddle", parameters);

        HashMap<String, Object> parameters2 = new HashMap<>();
        parameters2.put("brand", "Huffy");
        Bike actual = session.queryForObject(Bike.class,
            "MATCH (bike:Bike{brand: $brand})-[rels]-(o) RETURN *", parameters2);

        assertThat(actual.getId()).isEqualTo(bike.getId());
        assertThat(actual.getBrand()).isEqualTo(bike.getBrand());
        assertThat(actual.getWheels().size()).isEqualTo(bike.getWheels().size());
    }

    @Test
    public void canSaveNewObjectTreeToDatabase() {

        Wheel frontWheel = new Wheel();
        Wheel backWheel = new Wheel();
        Bike bike = new Bike();

        bike.setFrame(new Frame());
        bike.setSaddle(new Saddle());
        bike.setWheels(Arrays.asList(frontWheel, backWheel));

        assertThat(frontWheel.getId()).isNull();
        assertThat(backWheel.getId()).isNull();
        assertThat(bike.getId()).isNull();
        assertThat(bike.getFrame().getId()).isNull();
        assertThat(bike.getSaddle().getId()).isNull();

        session.save(bike);

        assertThat(frontWheel.getId()).isNotNull();
        assertThat(backWheel.getId()).isNotNull();
        assertThat(bike.getId()).isNotNull();
        assertThat(bike.getFrame().getId()).isNotNull();
        assertThat(bike.getSaddle().getId()).isNotNull();
    }
}

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
package org.neo4j.ogm.persistence.types.nativetypes;

import static org.assertj.core.api.Assertions.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Properties;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;
import org.neo4j.ogm.annotation.typeconversion.Convert;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.TestContainersTestBase;
import org.neo4j.ogm.typeconversion.LocalDateStringConverter;

/**
 * @author Gerrit Meier
 * @author Michael J. Simons
 * @author Matt Harrison
 */
public abstract class DatesTestBase extends TestContainersTestBase {

    static SessionFactory sessionFactory;

    @Before
    public void clearDatabase() {
        sessionFactory.openSession().purgeDatabase();
    }

    @Test
    public void convertPersistAndLoadLocalDate() {
        Session session = sessionFactory.openSession();
        Sometime sometime = new Sometime();
        LocalDate localDate = LocalDate.of(2018, 11, 15);
        sometime.setLocalDate(localDate);
        session.save(sometime);

        session.clear();
        Sometime loaded = session.load(Sometime.class, sometime.id);
        assertThat(loaded.localDate).isEqualTo(localDate);
    }

    @Test
    public void convertPersistAndLoadLocalDateTime() {
        Session session = sessionFactory.openSession();
        Sometime sometime = new Sometime();
        LocalDateTime localDateTime = LocalDateTime.of(2018, 11, 15, 8, 36);
        sometime.setLocalDateTime(localDateTime);
        session.save(sometime);

        session.clear();
        Sometime loaded = session.load(Sometime.class, sometime.id);
        assertThat(loaded.localDateTime).isEqualTo(localDateTime);
    }

    @Test
    public void convertPersistAndLoadDate() {
        Session session = sessionFactory.openSession();
        Sometime sometime = new Sometime();
        LocalDateTime localDateTime = LocalDateTime.of(2018, 11, 15, 8, 36);
        Date date = Date.from(localDateTime.toInstant(ZoneOffset.UTC));
        sometime.setDate(date);
        session.save(sometime);

        session.clear();
        Sometime loaded = session.load(Sometime.class, sometime.id);
        assertThat(loaded.date).isEqualTo(date);
    }

    @Test
    public void convertPersistAndLoadDuration() {
        Session session = sessionFactory.openSession();
        Sometime sometime = new Sometime();
        Duration duration = Duration.ofDays(32).plusHours(25).plusMinutes(61).plusSeconds(61).plusMillis(2123);
        sometime.setDuration(duration);
        session.save(sometime);

        session.clear();
        Sometime loaded = session.load(Sometime.class, sometime.id);
        assertThat(loaded.duration).isEqualTo(duration);
    }

    @Test
    public void convertPersistAndLoadPeriods() {
        Session session = sessionFactory.openSession();
        Sometime sometime = new Sometime();
        Period period = Period.of(13, 13, 400);
        sometime.setPeriod(period);
        session.save(sometime);

        session.clear();
        Sometime loaded = session.load(Sometime.class, sometime.id);
        assertThat(loaded.period).isEqualTo(period.normalized());
    }

    @Test
    public void convertPersistAndLoadZonedDateTimeInMap() {
        Session session = sessionFactory.openSession();
        Sometime sometime = new Sometime();
        ZonedDateTime dateTime = ZonedDateTime.of(2019,6,17,13,31,0,0,ZoneId.systemDefault());
        LocalDate localDate = LocalDate.of(2018, 11, 14);
        LocalDateTime localDateTime = LocalDateTime.of(2018, 10, 11, 15, 24);

        sometime.addMapProperty("zoned_datetime", dateTime);
        sometime.addMapProperty("local_date", localDate);
        sometime.addMapProperty("local_date_time", localDateTime);

        session.save(sometime);

        session.clear();
        Sometime loaded = session.load(Sometime.class, sometime.id);
        assertThat(loaded.getMapProperties()).containsEntry("zoned_datetime", dateTime);
        assertThat(loaded.getMapProperties()).containsEntry("local_date", localDate);
        assertThat(loaded.getMapProperties()).containsEntry("local_date_time", localDateTime);
    }

    @Test
    public void convertPersistAndLoadLocalDateForRelationship() {
        Session session = sessionFactory.openSession();
        SometimeRelationship sometime = new SometimeRelationship();
        LocalDate localDate = LocalDate.of(2018, 11, 15);
        sometime.setLocalDate(localDate);
        session.save(sometime);

        session.clear();
        SometimeRelationship loaded = session.load(SometimeRelationship.class, sometime.id);
        assertThat(loaded.localDate).isEqualTo(localDate);
    }

    @Test
    public void convertPersistAndLoadLocalDateTimeForRelationship() {
        Session session = sessionFactory.openSession();
        SometimeRelationship sometime = new SometimeRelationship();
        LocalDateTime localDateTime = LocalDateTime.of(2018, 11, 15, 8, 36);
        sometime.setLocalDateTime(localDateTime);
        session.save(sometime);

        session.clear();
        SometimeRelationship loaded = session.load(SometimeRelationship.class, sometime.id);
        assertThat(loaded.localDateTime).isEqualTo(localDateTime);
    }

    @Test
    public void convertPersistAndLoadDateForRelationship() {
        Session session = sessionFactory.openSession();
        SometimeRelationship sometimeRelationship = new SometimeRelationship();
        LocalDateTime localDateTime = LocalDateTime.of(2018, 11, 15, 8, 36);
        Date date = Date.from(localDateTime.toInstant(ZoneOffset.UTC));
        sometimeRelationship.setDate(date);
        session.save(sometimeRelationship);

        session.clear();
        SometimeRelationship loaded = session.load(SometimeRelationship.class, sometimeRelationship.id);
        assertThat(loaded.date).isEqualTo(date);
    }

    @Test
    public void convertPersistAndLoadDurationForRelationship() {
        Session session = sessionFactory.openSession();
        SometimeRelationship sometime = new SometimeRelationship();
        Duration duration = Duration.ofDays(32).plusHours(25).plusMinutes(61).plusSeconds(61).plusMillis(2123);
        sometime.setDuration(duration);
        session.save(sometime);

        session.clear();
        SometimeRelationship loaded = session.load(SometimeRelationship.class, sometime.id);
        assertThat(loaded.duration).isEqualTo(duration);
    }

    @Test
    public void convertPersistAndLoadPeriodsForRelationship() {
        Session session = sessionFactory.openSession();
        SometimeRelationship sometime = new SometimeRelationship();
        Period period = Period.of(13, 13, 400);
        sometime.setPeriod(period);
        session.save(sometime);

        session.clear();
        SometimeRelationship loaded = session.load(SometimeRelationship.class, sometime.id);
        assertThat(loaded.period).isEqualTo(period.normalized());
    }

    @Test
    public abstract void shouldObeyExplicitConversionOfNativeTypes();

    @Test
    public abstract void convertPersistAndLoadTemporalAmounts();

    Map<String, Object> createSometimeWithConvertedLocalDate() {
        Session session = sessionFactory.openSession();
        Sometime sometime = new Sometime();
        LocalDate convertedLocalDate = LocalDate.of(2018, 11, 21);
        sometime.setConvertedLocalDate(convertedLocalDate);
        session.save(sometime);

        session.clear();
        Map<String, Object> params = new HashMap<>();
        params.put("id", sometime.getId());
        return Collections.unmodifiableMap(params);
    }

    @Test
    public void collectionsOfDatesShouldWork() {
        Session session = sessionFactory.openSession();
        Sometime sometime = new Sometime();
        Duration d1 = Duration.ofMinutes(1);
        Duration d2 = Duration.ofMinutes(2);

        sometime.setArrayOfDates(new Duration[] { d1, d2 });
        sometime.setListOfDates(Arrays.asList(d1, d2));

        session.save(sometime);
        session.clear();

        Sometime loaded = session.load(Sometime.class, sometime.id);
        assertThat(loaded.getArrayOfDates()).hasSize(2).containsExactlyInAnyOrder(d1, d2);
        assertThat(loaded.getListOfDates()).hasSize(2).containsExactlyInAnyOrder(d1, d2);
    }

    @Test
    public abstract void shouldUseNativeDateTimeTypesInParameterMaps();

    @NodeEntity
    static class Sometime {

        LocalDate localDate;
        LocalDateTime localDateTime;
        Date date;
        Duration duration;
        Period period;
        TemporalAmount temporalAmount;
        private Long id;

        @Properties
        private Map<String, Object> mapProperties = new HashMap<>();

        @Convert(LocalDateStringConverter.class)
        LocalDate convertedLocalDate;

        @Relationship(value = "REL")
        private Collection<SometimeRelationship> rels = new ArrayList<>();

        private List<Duration> listOfDates;

        private Duration[] arrayOfDates;

        public Long getId() {
            return id;
        }

        public void setLocalDate(LocalDate localDate) {
            this.localDate = localDate;
        }

        public void setLocalDateTime(LocalDateTime localDateTime) {
            this.localDateTime = localDateTime;
        }

        public void setDate(Date date) {
            this.date = date;
        }

        public void setDuration(Duration duration) {
            this.duration = duration;
        }

        public void setPeriod(Period period) {
            this.period = period;
        }

        public TemporalAmount getTemporalAmount() {
            return temporalAmount;
        }

        public void setConvertedLocalDate(LocalDate convertedLocalDate) {
            this.convertedLocalDate = convertedLocalDate;
        }

        public List<Duration> getListOfDates() {
            return listOfDates;
        }

        public void setListOfDates(List<Duration> listOfDates) {
            this.listOfDates = listOfDates;
        }

        public Duration[] getArrayOfDates() {
            return arrayOfDates;
        }

        public void setArrayOfDates(Duration[] arrayOfDates) {
            this.arrayOfDates = arrayOfDates;
        }

        public void addMapProperty(String key, Object value) {
        	this.mapProperties.put(key, value);
        }

        public Map<String, Object> getMapProperties() {
        	return mapProperties;
        }
    }

    @NodeEntity
    static class Some {

        LocalDate localDate;
        private Long id;

        @Relationship(value = "REL", direction = Relationship.Direction.INCOMING)
        private Collection<SometimeRelationship> rels = new ArrayList<>();

    }

    @RelationshipEntity(type = "REL")
    static class SometimeRelationship {

        Long id;
        LocalDate localDate;
        LocalDateTime localDateTime;
        Date date;
        Duration duration;
        Period period;
        TemporalAmount temporalAmount;

        @StartNode
        private Sometime sometimeStart = new Sometime();

        @EndNode
        private Some someEnd = new Some();

        SometimeRelationship() {
            this.sometimeStart.rels.add(this);
            this.someEnd.rels.add(this);
        }

        public void setLocalDate(LocalDate localDate) {
            this.localDate = localDate;
        }

        public void setLocalDateTime(LocalDateTime localDateTime) {
            this.localDateTime = localDateTime;
        }

        public void setDate(Date date) {
            this.date = date;
        }

        public void setDuration(Duration duration) {
            this.duration = duration;
        }

        public void setPeriod(Period period) {
            this.period = period;
        }

        public TemporalAmount getTemporalAmount() {
            return temporalAmount;
        }
    }
}

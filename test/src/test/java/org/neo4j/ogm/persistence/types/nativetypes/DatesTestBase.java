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
package org.neo4j.ogm.persistence.types.nativetypes;

import static org.assertj.core.api.Assertions.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.junit.Test;
import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;

/**
 * @author Gerrit Meier
 * @author Michael J. Simons
 */
public abstract class DatesTestBase {

    protected static SessionFactory sessionFactory;

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
    public abstract void convertPersistAndLoadTemporalAmounts();

    @NodeEntity
    static class Sometime {

        LocalDate localDate;
        LocalDateTime localDateTime;
        Date date;
        Duration duration;
        Period period;
        TemporalAmount temporalAmount;
        private Long id;

        @Relationship(value = "REL")
        private Collection<SometimeRelationship> rels = new ArrayList<>();

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

    @NodeEntity
    static class Some {

        LocalDate localDate;
        private Long id;

        @Relationship(value = "REL", direction = Relationship.INCOMING)
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

        public SometimeRelationship() {
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

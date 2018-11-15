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
import java.util.Date;

import org.junit.Test;
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
        LocalDate localDate =  LocalDate.of(2018, 11, 15);
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
        LocalDateTime localDateTime =  LocalDateTime.of(2018, 11, 15, 8, 36);
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
        LocalDateTime localDateTime =  LocalDateTime.of(2018, 11, 15, 8, 36);
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
    public abstract void convertPersistAndLoadTemporalAmounts();

    static class Sometime {

        private Long id;

        private LocalDate localDate;
        private LocalDateTime localDateTime;
        private Date date;
        private Duration duration;
        private Period period;
        private TemporalAmount temporalAmount;

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

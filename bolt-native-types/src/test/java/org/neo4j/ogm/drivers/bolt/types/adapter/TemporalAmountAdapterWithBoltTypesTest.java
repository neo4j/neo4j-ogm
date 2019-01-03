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
package org.neo4j.ogm.drivers.bolt.types.adapter;

import static java.time.temporal.ChronoUnit.*;
import static org.assertj.core.api.Assertions.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.Period;

import org.junit.Test;
import org.neo4j.driver.v1.Values;
import org.neo4j.ogm.types.adapter.TemporalAmountAdapter;

/**
 * @author Michael J. Simons
 */
public class TemporalAmountAdapterWithBoltTypesTest {

    @Test
    public void internallyCreatedTypesShouldBeConvertedCorrect() {
        final TemporalAmountAdapter adapter = new TemporalAmountAdapter();

        assertThat(adapter.apply(Values.isoDuration(1, 0, 0, 0).asIsoDuration())).isEqualTo(Period.ofMonths(1));
        assertThat(adapter.apply(Values.isoDuration(1, 1, 0, 0).asIsoDuration()))
            .isEqualTo(Period.ofMonths(1).plusDays(1));
        assertThat(adapter.apply(Values.isoDuration(1, 1, 1, 0).asIsoDuration()))
            .isEqualTo(Values.isoDuration(1, 1, 1, 0).asIsoDuration());
        assertThat(adapter.apply(Values.isoDuration(0, 0, 120, 1).asIsoDuration()))
            .isEqualTo(Duration.ofMinutes(2).plusNanos(1));
    }

    @Test
    public void durationsShouldStayDurations() {
        final TemporalAmountAdapter adapter = new TemporalAmountAdapter();

        Duration duration =
            MONTHS.getDuration().multipliedBy(13).plus(DAYS.getDuration().multipliedBy(32)).plusHours(25)
                .plusMinutes(120);

        assertThat(adapter.apply(Values.value(duration).asIsoDuration())).isEqualTo(duration);
    }

    @Test
    public void periodsShouldStayPeriods() {
        final TemporalAmountAdapter adapter = new TemporalAmountAdapter();

        Period period = Period.between(LocalDate.of(2018, 11, 15), LocalDate.of(2020, 12, 24));

        assertThat(adapter.apply(Values.value(period).asIsoDuration())).isEqualTo(period.normalized());
    }
}

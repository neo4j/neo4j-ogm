/*
 * Copyright (c) 2002-2025 "Neo4j,"
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
package org.neo4j.ogm.drivers.bolt.types.adapter;

import static java.time.temporal.ChronoUnit.*;
import static org.assertj.core.api.Assertions.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.Period;

import org.junit.jupiter.api.Test;
import org.neo4j.driver.Values;
import org.neo4j.ogm.types.adapter.TemporalAmountAdapter;

/**
 * @author Michael J. Simons
 */
public class TemporalAmountAdapterWithBoltTypesTest {

    @Test
    void internallyCreatedTypesShouldBeConvertedCorrect() {
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
    void durationsShouldStayDurations() {
        final TemporalAmountAdapter adapter = new TemporalAmountAdapter();

        Duration duration =
            MONTHS.getDuration().multipliedBy(13).plus(DAYS.getDuration().multipliedBy(32)).plusHours(25)
                .plusMinutes(120);

        assertThat(adapter.apply(Values.value(duration).asIsoDuration())).isEqualTo(duration);
    }

    @Test
    void periodsShouldStayPeriods() {
        final TemporalAmountAdapter adapter = new TemporalAmountAdapter();

        Period period = Period.between(LocalDate.of(2018, 11, 15), LocalDate.of(2020, 12, 24));

        assertThat(adapter.apply(Values.value(period).asIsoDuration())).isEqualTo(period.normalized());
    }
}

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
package org.neo4j.ogm.drivers.bolt.types;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetTime;
import java.time.Period;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAmount;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.neo4j.driver.internal.value.DateTimeValue;
import org.neo4j.driver.internal.value.DateValue;
import org.neo4j.driver.internal.value.DurationValue;
import org.neo4j.driver.internal.value.LocalDateTimeValue;
import org.neo4j.driver.internal.value.LocalTimeValue;
import org.neo4j.driver.internal.value.PointValue;
import org.neo4j.driver.internal.value.TimeValue;
import org.neo4j.driver.Values;
import org.neo4j.driver.types.IsoDuration;
import org.neo4j.driver.types.Point;
import org.neo4j.ogm.driver.TypeAdapterLookupDelegate;
import org.neo4j.ogm.driver.TypeSystem;
import org.neo4j.ogm.drivers.bolt.types.adapter.BoltValueToPointAdapter;
import org.neo4j.ogm.drivers.bolt.types.adapter.PointToBoltValueAdapter;
import org.neo4j.ogm.types.adapter.TemporalAmountAdapter;
import org.neo4j.ogm.types.spatial.CartesianPoint2d;
import org.neo4j.ogm.types.spatial.CartesianPoint3d;
import org.neo4j.ogm.types.spatial.GeographicPoint2d;
import org.neo4j.ogm.types.spatial.GeographicPoint3d;

/**
 * @author Michael J. Simons
 */
class BoltNativeTypes implements TypeSystem {

    private final TypeAdapterLookupDelegate nativeToMappedAdapter;
    private final TypeAdapterLookupDelegate mappedToNativeAdapter;

    BoltNativeTypes() {

        Map<Class<?>, Function> mapOfNativeToMappedAdapter = new HashMap<>();
        Map<Class<?>, Function> mapOfMappedToNativeAdapter = new HashMap<>();

        addSpatialFeatures(mapOfNativeToMappedAdapter, mapOfMappedToNativeAdapter);
        addJavaTimeFeature(mapOfNativeToMappedAdapter, mapOfMappedToNativeAdapter);
        addPassthroughForBuildInTypes(mapOfMappedToNativeAdapter);

        this.nativeToMappedAdapter = new TypeAdapterLookupDelegate(mapOfNativeToMappedAdapter);
        this.mappedToNativeAdapter = new TypeAdapterLookupDelegate(mapOfMappedToNativeAdapter);
    }

    private static void addSpatialFeatures(Map<Class<?>, Function> nativeToMappedAdapter,
        Map<Class<?>, Function> mappedToNativeAdapter) {

        nativeToMappedAdapter.put(Point.class, new BoltValueToPointAdapter());

        PointToBoltValueAdapter pointToBoltValueAdapter = new PointToBoltValueAdapter();
        mappedToNativeAdapter.put(CartesianPoint2d.class, pointToBoltValueAdapter);
        mappedToNativeAdapter.put(CartesianPoint3d.class, pointToBoltValueAdapter);
        mappedToNativeAdapter.put(GeographicPoint2d.class, pointToBoltValueAdapter);
        mappedToNativeAdapter.put(GeographicPoint3d.class, pointToBoltValueAdapter);
    }

    private static void addJavaTimeFeature(Map<Class<?>, Function> nativeToMappedAdapter,
        Map<Class<?>, Function> mappedToNativeAdapter) {

        nativeToMappedAdapter.put(DateValue.class, Values.ofLocalDate());
        nativeToMappedAdapter.put(TimeValue.class, Values.ofOffsetTime());
        nativeToMappedAdapter.put(LocalTimeValue.class, Values.ofLocalTime());
        nativeToMappedAdapter.put(DateTimeValue.class, Values.ofZonedDateTime());
        nativeToMappedAdapter.put(LocalDateTimeValue.class, Values.ofLocalDateTime());

        nativeToMappedAdapter.put(IsoDuration.class, new TemporalAmountAdapter());

        mappedToNativeAdapter.put(LocalDate.class, Values::value);
        mappedToNativeAdapter.put(OffsetTime.class, Values::value);
        mappedToNativeAdapter.put(LocalTime.class, Values::value);
        mappedToNativeAdapter.put(ZonedDateTime.class, Values::value);
        mappedToNativeAdapter.put(LocalDateTime.class, Values::value);

        mappedToNativeAdapter.put(Duration.class, Values::value);
        mappedToNativeAdapter.put(Period.class, Values::value);
        mappedToNativeAdapter.put(TemporalAmount.class, Values::value);
    }

    // Those look the same as in embbedded native types, but the packages are different
    @SuppressWarnings("Duplicates")
    private static void addPassthroughForBuildInTypes(Map<Class<?>, Function> mappedToNativeAdapter) {
        /*
            // This allows passing in native parameters like this
            Map<String, Object> params  =new HashMap<>();
            params.put("x", Values.isoDuration(526, 45, 97200, 0).asIsoDuration());
            session.queryForObject(Long.class, "CREATE (s:`DatesTestBase$Sometime` {temporalAmount: $x}) RETURN id(s)",params);
         */
        mappedToNativeAdapter.put(PointValue.class, Function.identity());

        mappedToNativeAdapter.put(DateValue.class, Function.identity());
        mappedToNativeAdapter.put(TimeValue.class, Function.identity());
        mappedToNativeAdapter.put(LocalTimeValue.class, Function.identity());
        mappedToNativeAdapter.put(DateTimeValue.class, Function.identity());
        mappedToNativeAdapter.put(LocalDateTimeValue.class, Function.identity());

        mappedToNativeAdapter.put(DurationValue.class, Function.identity());
    }

    public boolean supportsAsNativeType(Class<?> clazz) {
        return TypeSystem.super.supportsAsNativeType(clazz) || mappedToNativeAdapter.hasAdapterFor(clazz);
    }

    @Override
    public Function<Object, Object> getNativeToMappedTypeAdapter(Class<?> clazz) {
        return nativeToMappedAdapter.getAdapterFor(clazz);
    }

    @Override
    public Function<Object, Object> getMappedToNativeTypeAdapter(Class<?> clazz) {
        return mappedToNativeAdapter.getAdapterFor(clazz);
    }
}

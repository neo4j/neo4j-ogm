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
package org.neo4j.ogm.drivers.embedded.types;

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

import org.neo4j.ogm.driver.TypeAdapterLookupDelegate;
import org.neo4j.ogm.driver.TypeSystem;
import org.neo4j.ogm.drivers.embedded.types.adapter.EmbeddedValueToPointAdapter;
import org.neo4j.ogm.drivers.embedded.types.adapter.PointToEmbeddedValueAdapter;
import org.neo4j.ogm.types.adapter.TemporalAmountAdapter;
import org.neo4j.ogm.types.spatial.CartesianPoint2d;
import org.neo4j.ogm.types.spatial.CartesianPoint3d;
import org.neo4j.ogm.types.spatial.GeographicPoint2d;
import org.neo4j.ogm.types.spatial.GeographicPoint3d;
import org.neo4j.values.storable.DateTimeValue;
import org.neo4j.values.storable.DateValue;
import org.neo4j.values.storable.DurationValue;
import org.neo4j.values.storable.LocalDateTimeValue;
import org.neo4j.values.storable.LocalTimeValue;
import org.neo4j.values.storable.PointValue;
import org.neo4j.values.storable.TimeValue;
import org.neo4j.values.storable.Values;

/**
 * @author Michael J. Simons
 */
class EmbeddedNativeTypes implements TypeSystem {

    private final TypeAdapterLookupDelegate nativeToMappedAdapter;
    private final TypeAdapterLookupDelegate mappedToNativeAdapter;

    EmbeddedNativeTypes() {

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

        nativeToMappedAdapter.put(PointValue.class, new EmbeddedValueToPointAdapter());

        PointToEmbeddedValueAdapter pointToEmbeddedValueAdapter = new PointToEmbeddedValueAdapter();
        mappedToNativeAdapter.put(CartesianPoint2d.class, pointToEmbeddedValueAdapter);
        mappedToNativeAdapter.put(CartesianPoint3d.class, pointToEmbeddedValueAdapter);
        mappedToNativeAdapter.put(GeographicPoint2d.class, pointToEmbeddedValueAdapter);
        mappedToNativeAdapter.put(GeographicPoint3d.class, pointToEmbeddedValueAdapter);
    }

    private static void addJavaTimeFeature(Map<Class<?>, Function> nativeToMappedAdapter,
        Map<Class<?>, Function> mappedToNativeAdapter) {

        nativeToMappedAdapter.put(DateValue.class, (Function<DateValue, LocalDate>) v -> v.asObjectCopy());
        nativeToMappedAdapter.put(TimeValue.class, (Function<TimeValue, OffsetTime>) v -> v.asObjectCopy());
        nativeToMappedAdapter.put(LocalTimeValue.class, (Function<LocalTimeValue, LocalTime>) v -> v.asObjectCopy());
        nativeToMappedAdapter.put(DateTimeValue.class, (Function<DateTimeValue, ZonedDateTime>) v -> v.asObjectCopy());
        nativeToMappedAdapter.put(LocalDateTimeValue.class, (Function<LocalDateTimeValue, LocalDateTime>) v -> v.asObjectCopy());

        nativeToMappedAdapter.put(DurationValue.class, new TemporalAmountAdapter());

        mappedToNativeAdapter.put(LocalDate.class, Values::of);
        mappedToNativeAdapter.put(OffsetTime.class, Values::of);
        mappedToNativeAdapter.put(LocalTime.class, Values::of);
        mappedToNativeAdapter.put(ZonedDateTime.class, Values::of);
        mappedToNativeAdapter.put(LocalDateTime.class, Values::of);

        mappedToNativeAdapter.put(Duration.class, Values::of);
        mappedToNativeAdapter.put(Period.class, Values::of);
        mappedToNativeAdapter.put(TemporalAmount.class, Values::of);
    }

    // Those look the same as in bolt native types, but the packages are different
    @SuppressWarnings("Duplicates")
    private static void addPassthroughForBuildInTypes(Map<Class<?>, Function> mappedToNativeAdapter) {
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

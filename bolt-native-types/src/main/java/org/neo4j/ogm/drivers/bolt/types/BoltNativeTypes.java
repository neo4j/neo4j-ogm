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
package org.neo4j.ogm.drivers.bolt.types;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.temporal.TemporalAmount;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.neo4j.driver.internal.value.DateValue;
import org.neo4j.driver.internal.value.DurationValue;
import org.neo4j.driver.internal.value.LocalDateTimeValue;
import org.neo4j.driver.internal.value.PointValue;
import org.neo4j.driver.v1.Value;
import org.neo4j.driver.v1.Values;
import org.neo4j.driver.v1.types.IsoDuration;
import org.neo4j.driver.v1.types.Point;
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

        Map<Class<?>, Function> nativeToMappedAdapter = new HashMap<>();
        Map<Class<?>, Function> mappedToNativeAdapter = new HashMap<>();

        addSpatialFeatures(nativeToMappedAdapter, mappedToNativeAdapter);
        addJavaTimeFeature(nativeToMappedAdapter, mappedToNativeAdapter);

        this.nativeToMappedAdapter = new TypeAdapterLookupDelegate(nativeToMappedAdapter);
        this.mappedToNativeAdapter = new TypeAdapterLookupDelegate(mappedToNativeAdapter);
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

        nativeToMappedAdapter.put(DateValue.class, new DriverFunctionWrapper<>(Values.ofLocalDate()));
        nativeToMappedAdapter.put(LocalDateTimeValue.class, new DriverFunctionWrapper<>(Values.ofLocalDateTime()));
        nativeToMappedAdapter.put(IsoDuration.class, new TemporalAmountAdapter());

        mappedToNativeAdapter.put(LocalDate.class, Values::value);
        mappedToNativeAdapter.put(LocalDateTime.class, Values::value);

        mappedToNativeAdapter.put(Duration.class, Values::value);
        mappedToNativeAdapter.put(Period.class, Values::value);
        mappedToNativeAdapter.put(TemporalAmount.class, Values::value);
    }

    public boolean supportsAsNativeType(Class<?> clazz) {
        return mappedToNativeAdapter.hasAdapterFor(clazz);
    }

    @Override
    public Function<Object, Object> getNativeToMappedTypeAdapter(Class<?> clazz) {
        return nativeToMappedAdapter.getAdapterFor(clazz);
    }

    @Override
    public Function<Object, Object> getMappedToNativeTypeAdapter(Class<?> clazz) {
        return mappedToNativeAdapter.getAdapterFor(clazz);
    }

    static class DriverFunctionWrapper<R> implements Function<Value, R> {

        private final org.neo4j.driver.v1.util.Function<Value, R> delegate;

        DriverFunctionWrapper(org.neo4j.driver.v1.util.Function<Value, R> delegate) {
            this.delegate = delegate;
        }

        @Override
        public R apply(Value t) {
            return delegate.apply(t);
        }
    }

}

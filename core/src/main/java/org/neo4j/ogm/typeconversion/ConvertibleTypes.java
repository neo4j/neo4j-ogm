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
package org.neo4j.ogm.typeconversion;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.typeconversion.DateString;
import org.neo4j.ogm.config.Configuration;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 * @author Michael J. Simons
 */
public abstract class ConvertibleTypes {

    private static final String DATE_SIGNATURE = "java.util.Date";
    private static final String BIG_DECIMAL_SIGNATURE = "java.math.BigDecimal";
    private static final String BIG_INTEGER_SIGNATURE = "java.math.BigInteger";
    private static final String BYTE_ARRAY_SIGNATURE = "byte[]";
    private static final String BYTE_ARRAY_WRAPPER_SIGNATURE = "java.lang.Byte[]";
    private static final String INSTANT_SIGNATURE = "java.time.Instant";
    private static final String LOCAL_DATE_SIGNATURE = "java.time.LocalDate";
    private static final String LOCAL_DATE_TIME_SIGNATURE = "java.time.LocalDateTime";
    private static final String OFFSET_DATE_TIME_SIGNATURE = "java.time.OffsetDateTime";

    /**
     * A unmodifiable map containing known attribute converters.
     */
    public static final Map<String, AttributeConverters> REGISTRY = buildRegistry();

    public static AttributeConverter<?, ?> getDateConverter() {
        return new DateStringConverter(DateString.ISO_8601);
    }

    public static AttributeConverter<?, ?> getDateArrayConverter() {
        return new DateArrayStringConverter(DateString.ISO_8601);
    }

    public static AttributeConverter<?, ?> getDateCollectionConverter(String collectionType) {
        try {
            Class collectionClazz = Class.forName(collectionType, false, Configuration.getDefaultClassLoader());
            return new DateCollectionStringConverter(DateString.ISO_8601, collectionClazz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static AttributeConverter<?, ?> getEnumConverter(Class enumClass) {
        return new EnumStringConverter(enumClass);
    }

    public static AttributeConverter<?, ?> getEnumArrayConverter(Class enumClass) {
        return new EnumArrayStringConverter(enumClass);
    }

    public static AttributeConverter<?, ?> getEnumCollectionConverter(Class enumClass, String collectionType) {
        try {
            Class collectionClazz = Class.forName(collectionType, false, Configuration.getDefaultClassLoader());
            return new EnumCollectionStringConverter(enumClass, collectionClazz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static AttributeConverter<?, ?> getBigIntegerConverter() {
        return new NumberStringConverter(BigInteger.class);
    }

    public static AttributeConverter<?, ?> getBigIntegerArrayConverter() {
        return new NumberArrayStringConverter(BigInteger.class);
    }

    public static AttributeConverter<?, ?> getBigIntegerCollectionConverter(String collectionType) {
        try {
            Class collectionClazz = Class.forName(collectionType, false, Configuration.getDefaultClassLoader());
            return new NumberCollectionStringConverter(BigInteger.class, collectionClazz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static AttributeConverter<?, ?> getBigDecimalConverter() {
        return new NumberStringConverter(BigDecimal.class);
    }

    public static AttributeConverter<?, ?> getBigDecimalArrayConverter() {
        return new NumberArrayStringConverter(BigDecimal.class);
    }

    public static AttributeConverter<?, ?> getBigDecimalCollectionConverter(String collectionType) {
        try {
            Class collectionClazz = Class.forName(collectionType, false, Configuration.getDefaultClassLoader());
            return new NumberCollectionStringConverter(BigDecimal.class, collectionClazz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static AttributeConverter<?, ?> getByteArrayBase64Converter() {
        return new ByteArrayBase64Converter();
    }

    public static AttributeConverter<?, ?> getByteArrayWrapperBase64Converter() {
        return new ByteArrayWrapperBase64Converter();
    }

    public static AttributeConverter<?, ?> getInstantConverter() {
        return new InstantStringConverter();
    }

    public static AttributeConverter<?, ?> getLocalDateConverter() {
        return new LocalDateStringConverter();
    }

    public static AttributeConverter<?, ?> getLocalDateTimeConverter() {
        return new LocalDateTimeStringConverter();
    }

    public static AttributeConverter<?, ?> getOffsetDateTimeConverter() {
        return new OffsettDateTimeStringConverter();
    }

    public static AttributeConverter<?, ?> getConverterBasedCollectionConverter(AttributeConverter<?, ?> converter,
        String collectionType) {
        try {
            Class collectionClazz = Class.forName(collectionType, false, Configuration.getDefaultClassLoader());
            return new ConverterBasedCollectionConverter<>(collectionClazz, converter);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static Map<String, AttributeConverters> buildRegistry() {
        final Map<String, AttributeConverters> registry = new HashMap<>();

        registry.put(DATE_SIGNATURE, AttributeConverters.Builder
            .forScalar(getDateConverter())
            .array(getDateArrayConverter())
            .andIterable(ct -> getDateCollectionConverter(ct)));

        registry.put(BIG_INTEGER_SIGNATURE, AttributeConverters.Builder
            .forScalar(getBigIntegerConverter()).array(getBigIntegerArrayConverter())
            .andIterable(ct -> getBigIntegerCollectionConverter(ct)));

        registry.put(BIG_DECIMAL_SIGNATURE, AttributeConverters.Builder
            .forScalar(getBigDecimalConverter()).array(getBigDecimalArrayConverter())
            .andIterable(ct -> getBigDecimalCollectionConverter(ct)));

        registry.put(INSTANT_SIGNATURE, AttributeConverters.Builder
            .onlyScalar(getInstantConverter()));

        registry.put(OFFSET_DATE_TIME_SIGNATURE, AttributeConverters.Builder
            .forScalar(getOffsetDateTimeConverter())
            .andIterable(ct -> getConverterBasedCollectionConverter(getOffsetDateTimeConverter(), ct)));

        registry.put(LOCAL_DATE_TIME_SIGNATURE, AttributeConverters.Builder
            .forScalar(getLocalDateTimeConverter())
            .andIterable(ct -> getConverterBasedCollectionConverter(getLocalDateTimeConverter(), ct)));

        registry.put(LOCAL_DATE_SIGNATURE, AttributeConverters.Builder
            .forScalar(getLocalDateConverter())
            .andIterable(ct -> getConverterBasedCollectionConverter(getLocalDateConverter(), ct)));

        registry.put(BYTE_ARRAY_SIGNATURE, AttributeConverters.Builder
            .onlyArray(getByteArrayBase64Converter()));

        registry.put(BYTE_ARRAY_WRAPPER_SIGNATURE, AttributeConverters.Builder
            .onlyArray(getByteArrayWrapperBase64Converter()));

        return Collections.unmodifiableMap(registry);
    }
}

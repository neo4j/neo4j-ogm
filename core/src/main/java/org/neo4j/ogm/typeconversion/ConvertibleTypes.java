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

package org.neo4j.ogm.typeconversion;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.neo4j.ogm.annotation.typeconversion.DateString;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 */
public abstract class ConvertibleTypes {

    public static AttributeConverter<?, ?> getDateConverter() {
        return new DateStringConverter(DateString.ISO_8601);
    }

    public static AttributeConverter<?, ?> getDateArrayConverter() {
        return new DateArrayStringConverter(DateString.ISO_8601);
    }

    public static AttributeConverter<?, ?> getDateCollectionConverter(String collectionType) {
        try {
            Class collectionClazz = Class
                .forName(collectionType, false, Thread.currentThread().getContextClassLoader());
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
            Class collectionClazz = Class
                .forName(collectionType, false, Thread.currentThread().getContextClassLoader());
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
            Class collectionClazz = Class
                .forName(collectionType, false, Thread.currentThread().getContextClassLoader());
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
            Class collectionClazz = Class
                .forName(collectionType, false, Thread.currentThread().getContextClassLoader());
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
            Class collectionClazz = Class
                .forName(collectionType, false, Thread.currentThread().getContextClassLoader());
            return new ConverterBasedCollectionConverter<>(collectionClazz, converter);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}

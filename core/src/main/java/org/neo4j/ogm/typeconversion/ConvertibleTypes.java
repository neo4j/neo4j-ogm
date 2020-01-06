/*
 * Copyright (c) 2002-2020 "Neo4j,"
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

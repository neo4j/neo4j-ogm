package org.neo4j.ogm.metadata.info;

import org.neo4j.ogm.annotation.typeconversion.DateString;
import org.neo4j.ogm.typeconversion.*;

import java.math.BigDecimal;
import java.math.BigInteger;

public abstract class ConvertibleTypes {

    public static AttributeConverter<?, ?> getDateConverter() {
        return new DateStringConverter(DateString.ISO_8601);
    }

    public static AttributeConverter<?, ?> getEnumConverter(String enumDescriptor) {
        String className = enumDescriptor.replace("/", ".");
        try {
            Class clazz = Class.forName(className);
            return new EnumStringConverter(clazz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static AttributeConverter<?,?> getBigIntegerConverter() {
        return new NumberStringConverter(BigInteger.class);
    }

    public static AttributeConverter<?, ?> getBigDecimalConverter() {
        return new NumberStringConverter(BigDecimal.class);
    }

    public static AttributeConverter<?, ?> getByteArrayBase64Converter() {
        return new ByteArrayBase64Converter();
    }

    public static AttributeConverter<?, ?> getByteArrayWrapperBase64Converter() {
        return new ByteArrayWrapperBase64Converter();
    }

}

package org.neo4j.ogm.metadata.info;

import org.neo4j.ogm.annotation.DateString;
import org.neo4j.ogm.typeconversion.AttributeConverter;
import org.neo4j.ogm.typeconversion.DateStringConverter;
import org.neo4j.ogm.typeconversion.EnumStringConverter;

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

}

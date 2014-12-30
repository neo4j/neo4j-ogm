package org.neo4j.ogm.metadata.info;

import org.neo4j.ogm.annotation.DateString;
import org.neo4j.ogm.typeconversion.AttributeConverter;
import org.neo4j.ogm.typeconversion.DateStringConverter;

public abstract class ConvertibleTypes {

    static String DATE = "Ljava/util/Date;";

    public static AttributeConverter<?, ?> getDefaultConverter(String typeDescriptor) {
        if (typeDescriptor.equals(DATE)) {
            return new DateStringConverter(DateString.ISO_8601);
        }
        throw new RuntimeException("Could not obtain type converter for convertible type: " + typeDescriptor);
    }
}

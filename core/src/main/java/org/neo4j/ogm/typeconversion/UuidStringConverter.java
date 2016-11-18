package org.neo4j.ogm.typeconversion;

import java.util.UUID;

/**
 * Converter to convert {@link java.util.UUID} to {@link java.lang.String}.
 *
 * This is a convenience class for those that use UUID to define node uniqueness.
 *
 * @author Mark Angrish
 */
public class UuidStringConverter implements AttributeConverter<UUID, String> {

    @Override
    public String toGraphProperty(UUID value) {
        if (value == null) {
            return null;
        }
        return value.toString();
    }

    @Override
    public UUID toEntityAttribute(String value) {
        if (value == null) {
            return null;
        }
        return UUID.fromString(value);
    }
}

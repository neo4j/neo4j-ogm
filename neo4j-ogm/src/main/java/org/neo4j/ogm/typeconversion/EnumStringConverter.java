package org.neo4j.ogm.typeconversion;

/**
 * By default the OGM will map date objects to UTC-based ISO8601 compliant
 * String values when being stored as a node / relationship property
 *
 * Users can override this behaviour for Date objects using
 * the appropriate annotations:
 *
 * @DateString("format") will convert between dates and strings
 * using a user defined date format, e.g. "yy-MM-dd"
 *
 * @DateLong will read and write dates as Long values in the database.
 */
public class EnumStringConverter implements AttributeConverter<Enum, String> {

    private final Class<? extends Enum> enumClass;

    public EnumStringConverter(Class<? extends Enum> enumClass) {
        this.enumClass = enumClass;
    }

    @Override
    public String toGraphProperty(Object value) {
        return ((Enum)value).name();
    }

    @Override
    public Enum toEntityAttribute(Object value) {
        return Enum.valueOf(enumClass, value.toString());
    }

}

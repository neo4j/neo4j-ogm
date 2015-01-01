package org.neo4j.ogm.typeconversion;

/**
 * By default the OGM will map enum objects to and from
 * the string value returned by enum.name()
 *
 * enum.name() is preferred to enum.ordinal() because it
 * is (slightly) safer: a persisted enum have to be renamed
 * to break its database mapping, whereas if its ordinal
 * was persisted instead, the mapping would be broken
 * simply by changing the declaration order in the enum set.
 */
public class EnumStringConverter implements AttributeConverter<Enum, String> {

    private final Class<? extends Enum> enumClass;

    public EnumStringConverter(Class<? extends Enum> enumClass) {
        this.enumClass = enumClass;
    }

    @Override
    public String toGraphProperty(Enum value) {
        return value.name();
    }

    @Override
    public Enum toEntityAttribute(String value) {
        return Enum.valueOf(enumClass, value.toString());
    }

}

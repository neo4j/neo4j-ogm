package org.neo4j.ogm.typeconversion;

/**
 * The NumberStringConverter can be used to convert any java object that extends
 * java.lang.Number to and from its String representation.
 *
 * By default, the OGM will automatically convert BigInteger and BigDecimal
 * entity attributes using this converter.
 *
 */
public class NumberStringConverter implements AttributeConverter<Number, String> {

    private final Class<? extends Number> numberClass;

    public NumberStringConverter(Class<? extends Number> numberClass) {
        this.numberClass = numberClass;
    }

    @Override
    public String toGraphProperty(Number value) {
        if (value == null) return null;
        return value.toString();
    }

    @Override
    public Number toEntityAttribute(String value) {
        if (value == null) return null;
        try {
            return numberClass.getDeclaredConstructor(String.class).newInstance(value);
        } catch (Exception e) {
            throw new RuntimeException("Conversion failed!", e);
        }
    }
}

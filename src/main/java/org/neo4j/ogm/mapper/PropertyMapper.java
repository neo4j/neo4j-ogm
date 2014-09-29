package org.neo4j.ogm.mapper;

/**
 * Used to map values between a property on a graph element and a particular field of a Java object.
 *
 * This could very well end up as an interface soon, if we want to have different implementations that work on, say,
 * Java-bean-style access methods as opposed to direct field access.
 */
public abstract class PropertyMapper {

    protected String fieldName;
    protected Object value;

    public PropertyMapper(String javaBeanFieldName, Object value) {
        this.fieldName = javaBeanFieldName;
        this.value = value;
    }

    public abstract void writeToObject(Object target);

    public String getFieldName() {
        return this.fieldName;
    }

}

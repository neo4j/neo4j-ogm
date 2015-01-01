package org.neo4j.ogm.typeconversion;

/**
 * based on JPA AttributeConverter, but with methods
 * appropriate for property graphs, rather than
 * column stores/RDBMS.
 *
 * @param <T> the class of the entity attribute
 * @param <F> the class of the associated graph property
 */
public interface AttributeConverter<T, F> {

    <F> F toGraphProperty(T value);
    <T> T toEntityAttribute(F value);

}

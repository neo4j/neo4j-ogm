package org.neo4j.ogm.metadata.dictionary;

public interface MappingResolver {

    String resolveGraphAttribute(String graphAttributeName);

    /**
     * Resolves the named type attribute to a graph entity property name or a relationship type depending on
     * the nature of the object to which this type refers.
     *
     * @param typeAttributeName The name of the type attribute
     * @param owningType The {@link Class} denoting the type on which the named attribute resides
     * @return The corresponding property name or relationship type or <code>null</code> if invoked with <code>null</code>
     */
    String resolveTypeAttribute(String typeAttributeName, Class<?> owningType);

}

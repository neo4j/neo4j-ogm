package org.neo4j.ogm.metadata;

/**
 * Encapsulates information about a field on a Java object that is persistent in a graph database.
 */
public interface PersistentField {

    // still WIP...

    String getJavaObjectFieldName();

    String getGraphElementPropertyName();

}

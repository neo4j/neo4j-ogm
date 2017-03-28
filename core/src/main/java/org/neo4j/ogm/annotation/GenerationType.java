package org.neo4j.ogm.annotation;

/**
 * Defines the types of primary key generation strategies.
 *
 * @see GeneratedValue
 *
 * @since 3.0
 */
public enum GenerationType {

    /**
	 * Indicates to use the Neo4J internal node id.
     * Beware that node ids may be recycled by the database and are not guaranteed to be unique
     * unless the database is explicitly configured for that.
     */
    NEO4J_INTERNAL_ID,

    /**
     * Indicates to generate a random UUID for this entity.
     */
    UUID
}
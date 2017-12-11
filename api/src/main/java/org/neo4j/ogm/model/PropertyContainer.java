package org.neo4j.ogm.model;

/**
 * Common interface for {@link Node} and {@link Edge} to allow common query generation
 *
 * @author Frantisek Hartman
 */
public interface PropertyContainer {

    /**
     * Return current version of the node, null if the relationship entity is new
     *
     * @return version property with current version
     */
    Property<String, Long> getVersion();
}

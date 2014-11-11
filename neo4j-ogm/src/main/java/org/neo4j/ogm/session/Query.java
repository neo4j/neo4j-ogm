package org.neo4j.ogm.session;

import org.graphaware.graphmodel.neo4j.Property;

import java.util.Collection;

public interface Query {

    /**
     * construct a query to fetch a single object with the specified id
     * @param id the id of the object to find
     * @return a Cypher expression
     */
    String findOne(Long id);

    /**
     * construct a query to fetch all objects with the specified ids
     * @param ids the ids of the objects to find
     * @return a Cypher expression
     */
    String findAll(Collection<Long> ids);

    /**
     * construct a query to fetch all objects
     * @return a Cypher expression
     */
    String findAll();

    /**
     * construct a query to fetch all objects with the specified label
     * @param label the labels attached to the objects
     * @return a Cypher expression
     */
    String findByLabel(String label);

    /**
     * construct a query to delete an object
     * @param id the id of the object to delete
     * @return a Cypher expression
     */
    String delete(Long id);

    /**
     * construct a query to delete all objects with the specified ids
     * @param ids the ids of the objects to delete
     * @return a Cypher expression
     */
    String deleteAll(Collection<Long> ids);

    /**
     * construct a query to purge the database
     * @return a Cypher expression
     */
    String purge();

    /**
     * construct a query to delete all objects having the supplied label
     * @param label the label for the objects to be deleted
     * @return a Cypher expression
     */
    String deleteByLabel(String label);

    /**
     * Construct an update statement to update the properties of
     * the node with the given identity
     *
     * @param identity
     * @param properties
     * @return
     */
    String updateProperties(Long identity, Collection<Property<String, Object>> properties);

    /**
     * Construct an update statement to update the properties of
     * the node with the given identity
     *
     * @param properties
     * @param labels
     * @return
     */
    String createNode(Collection<Property<String,Object>> properties, Collection<String> labels);

}

package org.neo4j.ogm.session.querystrategy;

import org.graphaware.graphmodel.neo4j.Property;
import org.neo4j.ogm.mapper.cypher.GraphModelQuery;
import org.neo4j.ogm.mapper.cypher.ParameterisedStatement;
import org.neo4j.ogm.mapper.cypher.RowModelQuery;

import java.util.Collection;

public interface QueryStrategy {

    /**
     * construct a query to fetch a single object with the specified id
     * @param id the id of the object to find
     * @return a Cypher expression
     */
     GraphModelQuery findOne(Long id);

    /**
     * construct a query to fetch all objects with the specified ids
     * @param ids the ids of the objects to find
     * @return a Cypher expression
     */
    GraphModelQuery findAll(Collection<Long> ids);

    /**
     * construct a query to fetch all objects
     * @return a Cypher expression
     */
    GraphModelQuery findAll();

    /**
     * construct a query to fetch all objects with the specified label
     * @param label the labels attached to the objects
     * @return a Cypher expression
     */
    GraphModelQuery findByLabel(String label);

    /**
     * construct a query to fetch all objects with the specified label and property
     * @param label the label value to filter on
     * @param property a property<K,V> value to filter on
     * @return a Cypher expression
     */
    GraphModelQuery findByProperty(String label, Property<String, Object> property);


    /**
     * construct a query to delete an object
     * @param id the id of the object to delete
     * @return a Cypher expression
     */
    ParameterisedStatement delete(Long id);

    /**
     * construct a query to delete all objects with the specified ids
     * @param ids the ids of the objects to delete
     * @return a Cypher expression
     */
    ParameterisedStatement deleteAll(Collection<Long> ids);

    /**
     * construct a query to purge the database
     * @return a Cypher expression
     */
    ParameterisedStatement purge();

    /**
     * construct a query to delete all objects having the supplied label
     * @param label the label for the objects to be deleted
     * @return a Cypher expression
     */
    ParameterisedStatement deleteByLabel(String label);

    /**
     * Construct an update statement to update the properties of
     * the node with the given identity
     *
     * @param identity
     * @param properties
     * @return
     */
    ParameterisedStatement updateProperties(Long identity, Collection<Property<String, Object>> properties);

    /**
     * Construct an update statement to update the properties of
     * the node with the given identity
     *
     * @param properties
     * @param labels
     * @return
     */
    RowModelQuery createNode(Collection<Property<String,Object>> properties, Collection<String> labels);


}

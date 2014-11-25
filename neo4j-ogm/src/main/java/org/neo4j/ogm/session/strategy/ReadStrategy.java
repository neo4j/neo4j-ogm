package org.neo4j.ogm.session.strategy;

import org.neo4j.graphmodel.Property;
import org.neo4j.ogm.mapper.cypher.statements.GraphModelQuery;

import java.util.Collection;

public interface ReadStrategy {

    /**
     * construct a query to fetch a single object with the specified id
     * @param id the id of the object to find
     * @param depth the depth to traverse for any related objects
     * @return a Cypher expression
     */
    GraphModelQuery findOne(Long id, int depth);

    /**
     * construct a query to fetch all objects with the specified ids
     * @param ids the ids of the objects to find
     * @param depth the depth to traverse for any related objects
     * @return a Cypher expression
     */
    GraphModelQuery findAll(Collection<Long> ids, int depth);

    /**
     * construct a query to fetch all objects
     * @return a Cypher expression
     */
    GraphModelQuery findAll();

    /**
     * construct a query to fetch all objects with the specified label
     * @param label the labels attached to the objects
     * @param depth the depth to traverse for related objects
     * @return a Cypher expression
     */
    GraphModelQuery findByLabel(String label, int depth);

    /**
     * construct a query to fetch all objects with the specified label and property
     * @param label the label value to filter on
     * @param property a property<K,V> value to filter on
     * @param depth the depth to traverse for related objects
     * @return a Cypher expression
     */
    GraphModelQuery findByProperty(String label, Property<String, Object> property, int depth);

}

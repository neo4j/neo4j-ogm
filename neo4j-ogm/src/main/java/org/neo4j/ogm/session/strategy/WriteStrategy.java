package org.neo4j.ogm.session.strategy;


import org.neo4j.graphmodel.Property;
import org.neo4j.ogm.mapper.cypher.statements.ParameterisedStatement;
import org.neo4j.ogm.mapper.cypher.statements.RowModelQuery;

import java.util.Collection;

public interface WriteStrategy {
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

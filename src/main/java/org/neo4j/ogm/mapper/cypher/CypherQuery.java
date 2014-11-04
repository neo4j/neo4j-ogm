package org.neo4j.ogm.mapper.cypher;

import org.graphaware.graphmodel.neo4j.GraphModel;
import org.graphaware.graphmodel.neo4j.Property;

import java.util.Collection;

public interface CypherQuery {

    public void setRequest(Request<GraphModel> request);

    /**
     * fetch one or more objects with the specified ids and their immediate neighbours
     * @param ids
     * @return MATCH p=(n)-[:r]->(m) where { id(n) in [....] | id(n) = ...}
     */
    ResponseStream<GraphModel> queryById(Long... ids);

    /**
     * fetch all objects with the specified label and their immediate neighbours
     * @param label
     * @return MATCH p=(n:{LABEL})-[:r]->(m)
     */
    ResponseStream<GraphModel> queryByLabel(Collection<String> label);

    /**
     * Fetch all objects with the specified ids also having the specified label
     *
     * @param label
     * @param ids
     * @return MATCH p=(n:{LABEL})-[:r]->(m) where { id(n) in [....] | id(n) = ...}
     */
    ResponseStream<GraphModel> queryByLabelAndId(Collection<String> label, Long... ids);

    /**
     * Fetch all objects with the specified labels also having the specified property
     *
     * @param labels
     * @param property
     */
    ResponseStream<GraphModel> queryByProperty(Collection<String> labels, Property property);

    /**
     * Fetch all objects with the specified labels also having the specified properties
     *
     * @param labels
     * @param properties
     */
    ResponseStream<GraphModel> queryByProperties(Collection<String> labels, Collection<Property> properties);

}

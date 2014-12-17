package org.neo4j.ogm.session.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.neo4j.ogm.cypher.compiler.CypherContext;
import org.neo4j.ogm.model.GraphModel;
import org.neo4j.ogm.model.Property;

import java.util.Collection;
import java.util.Set;

public interface ResponseHandler {

    <T> T loadById(Class<T> type, Neo4jResponse<GraphModel> stream, Long id);
    <T> Collection<T> loadAll(Class<T> type, Neo4jResponse<GraphModel> stream);
    <T> Set<T> loadByProperty(Class<T> type, Neo4jResponse<GraphModel> stream, Property<String, Object> filter);

    void updateObjects(CypherContext context, Neo4jResponse<String> response, ObjectMapper mapper);
}

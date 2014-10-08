package org.neo4j.ogm.strategy.annotated;

import org.graphaware.graphmodel.neo4j.GraphModel;
import org.neo4j.ogm.mapper.GraphModelToObjectMapper;

public class AnnotatedMethodMappingStrategy implements GraphModelToObjectMapper<GraphModel> {
    @Override
    public Object mapToObject(GraphModel graphModel) {
        return null;
    }
}

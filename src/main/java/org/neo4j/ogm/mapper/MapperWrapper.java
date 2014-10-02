package org.neo4j.ogm.mapper;

import org.graphaware.graphmodel.neo4j.GraphModel;

// or template. but I like mapper wrapper
public class MapperWrapper {

    private GraphModelToObjectMapper mapper;
    private GraphModel model;

    public MapperWrapper(GraphModelToObjectMapper mapper) {
        this.mapper = mapper;
    }

    public void load() {
        mapper.mapToObject(model);
    }



}

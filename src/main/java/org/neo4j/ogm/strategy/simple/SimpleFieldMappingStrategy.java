package org.neo4j.ogm.strategy.simple;

import org.graphaware.graphmodel.neo4j.GraphModel;
import org.neo4j.ogm.entityaccess.FieldEntityAccessFactory;
import org.neo4j.ogm.mapper.GraphModelToObjectMapper;
import org.neo4j.ogm.mapper.ObjectGraphMapper;
import org.neo4j.ogm.metadata.factory.DefaultConstructorObjectFactory;
import org.neo4j.ogm.metadata.info.DomainInfo;

public class SimpleFieldMappingStrategy implements GraphModelToObjectMapper<GraphModel> {

    private final ObjectGraphMapper mapper;

    public SimpleFieldMappingStrategy(Class<?> rootObjectType, String... packages) {
        mapper = new ObjectGraphMapper(
                rootObjectType,
                new DefaultConstructorObjectFactory(new SimpleClassDictionary(new DomainInfo(packages))),
                new FieldEntityAccessFactory(),
                new SimpleFieldDictionary());
    }

    public void reset() {
        mapper.reset();
    }

    @Override
    public Object mapToObject(GraphModel graphModel) {
        return mapper.mapToObject(graphModel);
    }
}

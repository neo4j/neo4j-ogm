package org.neo4j.ogm.strategy.simple;

import org.graphaware.graphmodel.neo4j.GraphModel;
import org.neo4j.ogm.entityaccess.MethodEntityAccessFactory;
import org.neo4j.ogm.mapper.GraphModelToObjectMapper;
import org.neo4j.ogm.mapper.ObjectGraphMapper;
import org.neo4j.ogm.metadata.AutomappingPersistentFieldDictionary;
import org.neo4j.ogm.metadata.DefaultConstructorObjectFactory;

public class SimpleSetterMappingStrategy  implements GraphModelToObjectMapper<GraphModel> {

    private final ObjectGraphMapper mapper;

    public SimpleSetterMappingStrategy(Class clazz) {

        mapper = new ObjectGraphMapper(
                clazz,
                new DefaultConstructorObjectFactory(new SimpleClassDictionary()),
                new MethodEntityAccessFactory(),
                new AutomappingPersistentFieldDictionary());


    }

    public void reset() {
        mapper.reset();
    }

    @Override
    public Object mapToObject(GraphModel graphModel) {
        return mapper.mapToObject(graphModel);
    }
}

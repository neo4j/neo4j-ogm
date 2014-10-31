package org.neo4j.ogm.strategy.simple;

import org.graphaware.graphmodel.neo4j.GraphModel;
import org.neo4j.ogm.entityaccess.MethodEntityAccessFactory;
import org.neo4j.ogm.mapper.GraphModelToObjectMapper;
import org.neo4j.ogm.mapper.ObjectGraphMapper;
import org.neo4j.ogm.metadata.dictionary.ClassDictionary;
import org.neo4j.ogm.metadata.dictionary.MethodDictionary;
import org.neo4j.ogm.metadata.factory.DefaultConstructorObjectFactory;
import org.neo4j.ogm.metadata.info.DomainInfo;

public class SimpleSetterMappingStrategy  implements GraphModelToObjectMapper<GraphModel> {

    private final ObjectGraphMapper mapper;

    public SimpleSetterMappingStrategy(Class<?> rootObjectType, String... packages) {

        DomainInfo domainInfo = new DomainInfo(packages);
        ClassDictionary classDictionary = new SimpleClassDictionary(domainInfo);
        MethodDictionary methodDictionary = new SimpleMethodDictionary(domainInfo);

        mapper = new ObjectGraphMapper(
                rootObjectType,
                new DefaultConstructorObjectFactory(classDictionary),
                new MethodEntityAccessFactory(methodDictionary));
    }

    public void reset() {
        mapper.reset();
    }

    @Override
    public Object mapToObject(GraphModel graphModel) {
        return mapper.mapToObject(graphModel);
    }
}

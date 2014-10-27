package org.neo4j.ogm.strategy.simple;

import org.graphaware.graphmodel.neo4j.GraphModel;
import org.neo4j.ogm.entityaccess.FieldEntityAccessFactory;
import org.neo4j.ogm.mapper.GraphModelToObjectMapper;
import org.neo4j.ogm.mapper.ObjectGraphMapper;
import org.neo4j.ogm.metadata.dictionary.AttributeDictionary;
import org.neo4j.ogm.metadata.dictionary.ClassDictionary;
import org.neo4j.ogm.metadata.dictionary.FieldDictionary;
import org.neo4j.ogm.metadata.factory.DefaultConstructorObjectFactory;
import org.neo4j.ogm.metadata.info.DomainInfo;

public class SimpleFieldMappingStrategy implements GraphModelToObjectMapper<GraphModel> {

    private final ObjectGraphMapper mapper;


    public SimpleFieldMappingStrategy(Class<?> rootObjectType, String... packages) {

        DomainInfo domainInfo = new DomainInfo(packages);
        ClassDictionary classDictionary = new SimpleClassDictionary(domainInfo);
        FieldDictionary fieldDictionary = new SimpleFieldDictionary(domainInfo);

        mapper = new ObjectGraphMapper(
                rootObjectType,
                new DefaultConstructorObjectFactory(classDictionary),
                new FieldEntityAccessFactory(fieldDictionary),
                (AttributeDictionary) fieldDictionary);  // todo: seems like we have this information twice now.
    }

    public void reset() {
        mapper.reset();
    }

    @Override
    public Object mapToObject(GraphModel graphModel) {
        return mapper.mapToObject(graphModel);
    }
}

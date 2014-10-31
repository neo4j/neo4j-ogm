package org.neo4j.ogm.metadata;

import java.util.Arrays;

import org.junit.Test;
import org.neo4j.ogm.entityaccess.EntityAccessFactory;
import org.neo4j.ogm.entityaccess.FieldEntityAccessFactory;
import org.neo4j.ogm.mapper.domain.social.Individual;
import org.neo4j.ogm.metadata.dictionary.FieldDictionary;
import org.neo4j.ogm.metadata.factory.DefaultConstructorObjectFactory;
import org.neo4j.ogm.metadata.factory.ObjectFactory;
import org.neo4j.ogm.metadata.info.DomainInfo;
import org.neo4j.ogm.strategy.simple.SimpleClassDictionary;
import org.neo4j.ogm.strategy.simple.SimpleFieldDictionary;

public class MappingConfigurationPlayground {

    private FieldDictionary socialDictionary = new SimpleFieldDictionary(new DomainInfo("org.neo4j.ogm.mapper.domain.social"));

    @Test
    public void exploreHowWritingWillWorkWithSimpleGraphOfTwoNodesAndOneRelationship() {
        Individual jeff = new Individual();
        jeff.setAge(30);
        jeff.setName("Jeff");
        Individual gary = new Individual();
        gary.setAge(42);
        gary.setName("Gary");
        gary.setFriends(Arrays.asList(jeff));

        final Object root = gary;

        // So, we want to end up with something like:
        // CREATE (:Individual {age:30,name:'Gary'})-[:FRIEND_OF]->(:Individual {age:42,name:'Jeff'});

        MappingConfiguration mappingConfig = buildMappingConfiguration();
        EntityAccessFactory entityAccessFactory = new FieldEntityAccessFactory(socialDictionary);

        // We DEFINITELY need a DSL for this, it's very messy with invocation order being critical as with a StringBuilder
        // is jCypher appropriate or should we use a lightweight CypherQueryContext or something?
        // look at: http://docs.spring.io/spring-data/data-neo4j/docs/3.1.2.RELEASE/reference/html/programming-model.html#d0e2915
        StringBuilder cypherBuilder = new StringBuilder();
        StringBuilder relationshipBuilder = new StringBuilder();
        StringBuilder propertiesBuilder = new StringBuilder().append("{ ");
/*
        MetaData metaData = mappingConfig.retrieveMappingMetadata();
        for (FieldInfo field : metaData.classInfo(root.getClass().getName()).fieldsInfo().fields()) {
            if (field.isIdField()) {
                boolean isNewNode = entityAccessFactory.forProperty(field.getGraphElementPropertyName()).readValue(root) == null;
                cypherBuilder.append(isNewNode ? "CREATE " : "MERGE ");
            } else if (field.isScalarValue()) {
                // just write the field value to its property
                Object valueToSave = entityAccessFactory.forProperty(field.getJavaObjectFieldName()).readValue(root);
                // what about escaping if the value contains characters like an apostrophe?  Is this done in t'DSL?
                // indeed, should we just be making a PreparedStatement here?
                propertiesBuilder.append(field.getGraphElementPropertyName()).append(':').append(valueToSave).append(',');
            } else {
                // not an ID or property so create a relationship to another node

                // could be a collection or a single object
                Object otherObjectOrObjects = entityAccessFactory.forProperty(field.getJavaObjectFieldName()).readValue(root);

                if (otherObjectOrObjects != null) {
                    String relationshipType = field.getGraphElementPropertyName(); // what we want here is the relationship type
                    relationshipBuilder.append("-[:").append(relationshipType).append("]->");

                    // recurse onto the other node (or each of them if it's a collection)
//                    map(((List) otherObjectOrObjects).get(0), mappingConfig, entityAccessFactory, cypherBuilder);
                }
            }
        }
*/
        propertiesBuilder.setCharAt(propertiesBuilder.length() - 1, '}');

        // now, are we writing this object to a node or a relationship?
//        if (metaData.isNodeEntity(root.getClass())) {
            cypherBuilder.append('(');

            // would need to resolve and add potentially several labels here
            cypherBuilder.append(':').append(root.getClass().getSimpleName()).append(' ');

            cypherBuilder.append(propertiesBuilder);
            cypherBuilder.append(')');
            cypherBuilder.append(relationshipBuilder);
//        }

//        java.sql.Connection con = driver.connect(...)
//        con.createStatement().executeQuery(cypherBuilder.toString());
        System.out.println(cypherBuilder.toString());
    }

    private static MappingConfiguration buildMappingConfiguration() {
        return new MappingConfiguration() {

            @Override
            public ObjectFactory provideObjectFactory() {
                return new DefaultConstructorObjectFactory(new SimpleClassDictionary(new DomainInfo("org.neo4j.ogm.mapper.domain.social")));
            }

            @Override
            public MetaData retrieveMappingMetadata() {
                // just what we need for reading, let's say we've got a person...
                return new MetaData("org.neo4j.ogm.mapper.domain.social");
            }
        };
    }

}

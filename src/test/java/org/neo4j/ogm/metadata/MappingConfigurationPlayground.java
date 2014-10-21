package org.neo4j.ogm.metadata;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.graphaware.graphmodel.neo4j.NodeModel;
import org.graphaware.graphmodel.neo4j.Property;
import org.junit.Test;
import org.neo4j.ogm.entityaccess.EntityAccessFactory;
import org.neo4j.ogm.entityaccess.FieldEntityAccessFactory;
import org.neo4j.ogm.mapper.domain.social.Individual;
import org.neo4j.ogm.metadata.dictionary.DefaultPersistentFieldDictionary;

import org.neo4j.ogm.metadata.dictionary.PersistentFieldDictionary;
import org.neo4j.ogm.metadata.factory.DefaultConstructorObjectFactory;
import org.neo4j.ogm.metadata.factory.ObjectFactory;
import org.neo4j.ogm.strategy.simple.SimpleClassDictionary;

public class MappingConfigurationPlayground {

    @Test
    public void buildSomeMappingConfigurationAndMetadataAndTryToUseIt() {
        MappingConfiguration trivialMappingConfig = buildMappingConfiguration();

        NodeModel testNode = new NodeModel();
        testNode.setLabels(new String[] {"Individual"});
        testNode.setPropertyList(Arrays.<Property<String, Object>>asList(
                new Property<String, Object>("forename", "Dougal"),
                new Property<String, Object>("surname", "McAngus"),
                new Property<String, Object>("age", 32)));

        EntityAccessFactory entityAccessFactory = new FieldEntityAccessFactory();

        // do some test mapping now...

        Individual toHydrate = trivialMappingConfig.provideObjectFactory().instantiateObjectMappedTo(testNode);
        DefaultPersistentFieldDictionary personMetadata = (DefaultPersistentFieldDictionary) trivialMappingConfig.findMappingMetadataForType(toHydrate.getClass());
        for (Property<?, ?> attribute : testNode.getPropertyList()) {
            // now, not all of these attributes may be mapped and not all of the fields may be specified as attributes
            PersistentField pf = personMetadata.lookUpPersistentFieldForProperty((String)attribute.getKey());
            try {
                entityAccessFactory.forProperty(pf.getJavaObjectFieldName()).setValue(toHydrate, attribute.getValue());
                // FIXME: this is NPE-ing here, should we keep the no-op behaviour that PropertyMapper used to have?
            } catch (Exception e) {
                System.err.println("Couldn't map persistent field: " + pf + " to instance of " + toHydrate.getClass());
                e.printStackTrace(System.err);
            }
        }

        assertEquals("Dougal", toHydrate.getName());
        assertEquals(32, toHydrate.getAge());
    }

    /** This is the sort of thing I'd expect to see in ObjectGraphMapper when it implements the "write" interface */
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
        EntityAccessFactory entityAccessFactory = new FieldEntityAccessFactory();

        // We DEFINITELY need a DSL for this, it's very messy with invocation order being critical as with a StringBuilder
        // is jCypher appropriate or should we use a lightweight CypherQueryContext or something?
        // look at: http://docs.spring.io/spring-data/data-neo4j/docs/3.1.2.RELEASE/reference/html/programming-model.html#d0e2915
        StringBuilder cypherBuilder = new StringBuilder();
        StringBuilder relationshipBuilder = new StringBuilder();
        StringBuilder propertiesBuilder = new StringBuilder().append("{ ");

        PersistentFieldDictionary persistentFieldDictionary = mappingConfig.findMappingMetadataForType(root.getClass());
        for (PersistentField pf : persistentFieldDictionary.lookUpPersistentFieldsOfType(root.getClass())) {
            if (pf.isIdField()) {
                boolean isNewNode = entityAccessFactory.forProperty(pf.getGraphElementPropertyName()).readValue(root) == null;
                cypherBuilder.append(isNewNode ? "CREATE " : "MERGE ");
            } else if (pf.isScalarValue()) {
                // just write the field value to its property
                Object valueToSave = entityAccessFactory.forProperty(pf.getJavaObjectFieldName()).readValue(root);
                // what about escaping if the value contains characters like an apostrophe?  Is this done in t'DSL?
                // indeed, should we just be making a PreparedStatement here?
                propertiesBuilder.append(pf.getGraphElementPropertyName()).append(':').append(valueToSave).append(',');
            } else {
                // not an ID or property so create a relationship to another node

                // could be a collection or a single object
                Object otherObjectOrObjects = entityAccessFactory.forProperty(pf.getJavaObjectFieldName()).readValue(root);

                if (otherObjectOrObjects != null) {
                    String relationshipType = pf.getGraphElementPropertyName(); // what we want here is the relationship type
                    relationshipBuilder.append("-[:").append(relationshipType).append("]->");

                    // recurse onto the other node (or each of them if it's a collection)
//                    map(((List) otherObjectOrObjects).get(0), mappingConfig, entityAccessFactory, cypherBuilder);
                }
            }
        }
        propertiesBuilder.setCharAt(propertiesBuilder.length() - 1, '}');

        // now, are we writing this object to a node or a relationship?
        if (persistentFieldDictionary.isNodeEntity(root.getClass())) {
            cypherBuilder.append('(');

            // would need to resolve and add potentially several labels here
            cypherBuilder.append(':').append(root.getClass().getSimpleName()).append(' ');

            cypherBuilder.append(propertiesBuilder);
            cypherBuilder.append(')');
            cypherBuilder.append(relationshipBuilder);
        }

//        java.sql.Connection con = driver.connect(...)
//        con.createStatement().executeQuery(cypherBuilder.toString());
        System.out.println(cypherBuilder.toString());
    }

    private static MappingConfiguration buildMappingConfiguration() {
        return new MappingConfiguration() {

            @Override
            public DefaultPersistentFieldDictionary findMappingMetadataForType(Class<?> typeToMap) {

                // just what we need for reading, let's say we've got a person...
                return new DefaultPersistentFieldDictionary(Arrays.asList(
                        new RegularPersistentField("id"),
                        new RegularPersistentField("name", "forename"),
                        new RelationalPersistentField("friends"),
                        new RegularPersistentField("age")));
            }

            @Override
            public ObjectFactory provideObjectFactory() {
                return new DefaultConstructorObjectFactory(new SimpleClassDictionary("org.neo4j.ogm.mapper.domain.social"));
            }
        };
    }

}

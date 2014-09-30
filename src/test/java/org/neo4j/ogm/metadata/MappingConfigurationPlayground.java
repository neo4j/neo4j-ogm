package org.neo4j.ogm.metadata;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.graphaware.graphmodel.Property;
import org.graphaware.graphmodel.impl.StringProperty;
import org.graphaware.graphmodel.neo4j.NodeModel;
import org.junit.Test;
import org.neo4j.ogm.domain.social.Person;
import org.neo4j.ogm.strategy.EntityAccess;
import org.neo4j.ogm.strategy.EntityAccessFactory;

public class MappingConfigurationPlayground {

    @Test
    public void buildSomeMappingConfigurationAndMetadataAndTryToUseIt() {
        MappingConfiguration trivialMappingConfig = buildMappingConfiguration();

        NodeModel testNode = new NodeModel();
        testNode.setLabels(new String[] {"Person"});
        testNode.setAttributes(Arrays.<Property>asList(new StringProperty("forename", "Dougal"),
                new StringProperty("surname", "McAngus"), new StringProperty("age", 32)));

        EntityAccessFactory entityAccessFactory = new EntityAccessFactory() {
            @Override
            public EntityAccess forPersistentField(PersistentField pf) {
                return new FieldEntityAccess(pf.getJavaObjectFieldName());
            }
            @Override
            public EntityAccess forType(Class<?> type) {
                throw new UnsupportedOperationException("atg hasn't written this method");
            }};

        // do some test mapping now...

        Person toHydrate = trivialMappingConfig.provideObjectFactory().instantiateObjectMappedTo(testNode);
        SimplePersistentFieldDictionary personMetadata = (SimplePersistentFieldDictionary) trivialMappingConfig.findMappingMetadataForType(toHydrate.getClass());
        for (Property<?, ?> attribute : testNode.getAttributes()) {
            // now, not all of these attributes may be mapped and not all of the fields may be specified as attributes
            PersistentField pf = personMetadata.lookUpPersistentFieldForProperty(attribute);
            try {
                entityAccessFactory.forPersistentField(pf).setValue(toHydrate, attribute.getValue());
                // FIXME: this is NPE-ing here, should we keep the no-op behaviour that PropertyMapper used to have?
            } catch (Exception e) {
                System.err.println("Couldn't map persistent field: " + pf + " to instance of " + toHydrate.getClass());
                e.printStackTrace(System.err);
            }
        }

        assertEquals("Dougal", toHydrate.getName());
        assertEquals(32, toHydrate.getAge());
    }

    private static MappingConfiguration buildMappingConfiguration() {
        return new MappingConfiguration() {

            @Override
            public SimplePersistentFieldDictionary findMappingMetadataForType(Class<?> typeToMap) {
                // just what we need for reading, let's say we've got a person...
                return new SimplePersistentFieldDictionary(Arrays.asList(
                        new RegularPersistentField("id"),
                        new RegularPersistentField("name", "forename"),
                        new RegularPersistentField("age")));
            }

            @Override
            public ObjectFactory provideObjectFactory() {
                // how to look up the label from a node/relationship
                Map<String, String> classMap = new HashMap<>();
                classMap.put("Person", Person.class.getName());
                ClassDictionary classDictionary = new MapBasedClassDictionary(classMap);
                return new DefaultConstructorObjectFactory(classDictionary);
            }
        };
    }

}

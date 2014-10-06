package org.neo4j.ogm.metadata;

import org.graphaware.graphmodel.neo4j.NodeModel;
import org.graphaware.graphmodel.neo4j.Property;
import org.junit.Test;
import org.neo4j.ogm.entityaccess.EntityAccess;
import org.neo4j.ogm.entityaccess.EntityAccessFactory;
import org.neo4j.ogm.entityaccess.FieldEntityAccess;
import org.neo4j.ogm.mapper.domain.social.Individual;
import org.neo4j.ogm.metadata.dictionary.ClassDictionary;
import org.neo4j.ogm.metadata.dictionary.DefaultPersistentFieldDictionary;
import org.neo4j.ogm.metadata.dictionary.MapBasedClassDictionary;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

//
// TODO:
// this class is causing a few problems because we had overlapping domain objects under test.
// e.g. label "person" was mapped to social.person as well as rulers.person. social.person
// has been renamed to social.individual for now, but we need to get to the root of the problem and
// fix it.
public class MappingConfigurationPlayground {

    @Test
    public void buildSomeMappingConfigurationAndMetadataAndTryToUseIt() {
        MappingConfiguration trivialMappingConfig = buildMappingConfiguration();

        NodeModel testNode = new NodeModel();
        testNode.setLabels(new String[] {"Individual"});
        testNode.setPropertyList(Arrays.<Property<String, Object>>asList(new Property("forename", "Dougal"),
                new Property("surname", "McAngus"), new Property("age", 32)));

        EntityAccessFactory entityAccessFactory = new EntityAccessFactory() {
            @Override
            public EntityAccess forProperty(String property) {
                return FieldEntityAccess.forProperty(property);
            }
        };

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

    private static MappingConfiguration buildMappingConfiguration() {
        return new MappingConfiguration() {

            @Override
            public DefaultPersistentFieldDictionary findMappingMetadataForType(Class<?> typeToMap) {

                // just what we need for reading, let's say we've got a person...
                return new DefaultPersistentFieldDictionary(Arrays.asList(
                        new RegularPersistentField("id"),
                        new RegularPersistentField("name", "forename"),
                        new RegularPersistentField("age")));
            }

            @Override
            public ObjectFactory provideObjectFactory() {
                // how to look up the label from a node/relationship
                Map<String, String> classMap = new HashMap<>();
                classMap.put("Individual", Individual.class.getName());
                ClassDictionary classDictionary = new MapBasedClassDictionary(classMap);
                return new DefaultConstructorObjectFactory(classDictionary);
            }
        };
    }

}

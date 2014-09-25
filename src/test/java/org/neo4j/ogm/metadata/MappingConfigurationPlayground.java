package org.neo4j.ogm.metadata;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.graphaware.graphmodel.Property;
import org.graphaware.graphmodel.Taxon;
import org.graphaware.graphmodel.Vertex;
import org.graphaware.graphmodel.impl.StringProperty;
import org.graphaware.graphmodel.impl.StringTaxon;
import org.graphaware.graphmodel.neo4j.GraphModel;
import org.graphaware.graphmodel.neo4j.NodeModel;
import org.junit.Test;
import org.neo4j.ogm.mapper.GraphModelToObjectMapper;
import org.neo4j.ogm.testmodel.Person;

public class MappingConfigurationPlayground {

    @Test
    public void buildSomeMappingConfigurationAndMetadataAndTryToUseIt() {
        MappingConfiguration trivialMappingConfig = new MappingConfiguration() {

            @Override
            public MappingMetadata findMappingMetadataForType(Class<?> typeToMap) {
                // just what we need for reading, let's say we've got a person...
                return new MappingMetadata(Person.class, Arrays.<PersistentField> asList(
                        new RegularPersistentField("id"),
                        new RegularPersistentField("name", "forename"),
                        new RegularPersistentField("age")));
            }

            @Override
            public ObjectCreator provideObjectCreator() {
                // how to look up the label from a node/relationship
                Map<String, String> classMap = new HashMap<>();
                classMap.put("Person", Person.class.getName());
                ClassDictionary classDictionary = new MapBasedClassDictionary(classMap);
                return new DefaultConstructorObjectCreator(classDictionary);
            }
        };

        NodeModel testNode = new NodeModel();
        testNode.setTaxa(Arrays.<Taxon>asList(new StringTaxon("Person")));
        testNode.setAttributes(Arrays.<Property>asList(new StringProperty("forename", "Dougal"),
                new StringProperty("surname", "McAngus"), new StringProperty("age", 32)));

        // do some test mapping now...

        Person toHydrate = trivialMappingConfig.provideObjectCreator().instantiateObjectMappedTo(testNode);
        MappingMetadata personMetadata = trivialMappingConfig.findMappingMetadataForType(toHydrate.getClass());
        for (Property<?, ?> attribute : testNode.getAttributes()) {
            // now, not all of these attributes may be mapped and not all of the fields may be specified as attributes
            // that's why we return noop from getPropertyMapper if we don't find the attribute
            personMetadata.getPropertyMapper(attribute).writeToObject(toHydrate);
        }

        assertEquals("Dougal", toHydrate.getName());
        assertEquals(32, toHydrate.getAge());
    }

}

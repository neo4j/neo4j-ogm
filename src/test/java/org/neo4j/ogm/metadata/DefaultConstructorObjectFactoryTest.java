package org.neo4j.ogm.metadata;

import org.graphaware.graphmodel.neo4j.RelationshipModel;
import org.graphaware.graphmodel.neo4j.NodeModel;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.mapper.domain.social.Individual;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNotNull;

public class DefaultConstructorObjectFactoryTest {

    private DefaultConstructorObjectFactory objectCreator;

    @Before
    public void setUp() {
        Map<String, String> mappings = new HashMap<>();
        mappings.put("ClassWithPrivateConstructor", ClassWithPrivateConstructor.class.getName());
        mappings.put("ClassWithoutZeroArgumentConstructor", ClassWithoutZeroArgumentConstructor.class.getName());
        mappings.put("Person", Individual.class.getName());
        this.objectCreator = new DefaultConstructorObjectFactory(new MapBasedClassDictionary(mappings));
    }

    @Test
    public void shouldConstructObjectOfParticularTypeUsingItsDefaultZeroArgConstructor() {
        RelationshipModel personRelationshipModel = new RelationshipModel();
        personRelationshipModel.setType("Person");
        Individual gary = this.objectCreator.instantiateObjectMappedTo(personRelationshipModel);
        assertNotNull(gary);

        NodeModel personNodeModel = new NodeModel();
        personNodeModel.setLabels(new String[] {"Person"});
        Individual sheila = this.objectCreator.instantiateObjectMappedTo(personNodeModel);
        assertNotNull(sheila);
    }

    @Test
    public void shouldHandleMultipleLabelsSafely() {
        NodeModel personNodeModel = new NodeModel();
        personNodeModel.setLabels(new String[] {"Female", "Person", "Lass"});
        Individual ourLass = this.objectCreator.instantiateObjectMappedTo(personNodeModel);
        assertNotNull(ourLass);
    }

    @Test(expected = MappingException.class)
    public void shouldFailIfZeroArgConstructorIsNotPresent() {
        RelationshipModel edge = new RelationshipModel();
        edge.setId(49L);
        edge.setType("ClassWithoutZeroArgumentConstructor");
        this.objectCreator.instantiateObjectMappedTo(edge);
    }

    @Test(expected = MappingException.class)
    public void shouldFailIfZeroArgConstructorIsNotVisible() {
        NodeModel vertex = new NodeModel();
        vertex.setId(163L);
        vertex.setLabels(new String[] {"ClassWithPrivateConstructor"});
        this.objectCreator.instantiateObjectMappedTo(vertex);
    }

    @Test(expected = MappingException.class)
    public void shouldFailForGraphModelComponentWithNoTaxa() {
        NodeModel vertex = new NodeModel();
        vertex.setId(302L);
        vertex.setLabels(new String[0]);
        this.objectCreator.instantiateObjectMappedTo(vertex);
    }

    static class ClassWithPrivateConstructor {

        private ClassWithPrivateConstructor() {
            // can't instantiate me!
        }

    }

    static class ClassWithoutZeroArgumentConstructor {

        public ClassWithoutZeroArgumentConstructor(String parameter) {
            // do nothing
        }

        public ClassWithoutZeroArgumentConstructor(int parameter) {
            // do nothing
        }

    }

}

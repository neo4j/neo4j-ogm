package org.neo4j.ogm.metadata;

import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import org.graphaware.graphmodel.neo4j.EdgeModel;
import org.graphaware.graphmodel.neo4j.NodeModel;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.mapper.domain.social.Person;

public class DefaultConstructorObjectFactoryTest {

    private DefaultConstructorObjectFactory objectCreator;

    @Before
    public void setUp() {
        Map<String, String> mappings = new HashMap<>();
        mappings.put("ClassWithPrivateConstructor", ClassWithPrivateConstructor.class.getName());
        mappings.put("ClassWithoutZeroArgumentConstructor", ClassWithoutZeroArgumentConstructor.class.getName());
        mappings.put("Person", Person.class.getName());
        this.objectCreator = new DefaultConstructorObjectFactory(new MapBasedClassDictionary(mappings));
    }

    @Test
    public void shouldConstructObjectOfParticularTypeUsingItsDefaultZeroArgConstructor() {
        EdgeModel personEdgeModel = new EdgeModel();
        personEdgeModel.setType("Person");
        Person gary = this.objectCreator.instantiateObjectMappedTo(personEdgeModel);
        assertNotNull(gary);

        NodeModel personNodeModel = new NodeModel();
        personNodeModel.setLabels(new String[] {"Person"});
        Person sheila = this.objectCreator.instantiateObjectMappedTo(personNodeModel);
        assertNotNull(sheila);
    }

    @Test
    public void shouldHandleMultipleLabelsSafely() {
        NodeModel personNodeModel = new NodeModel();
        personNodeModel.setLabels(new String[] {"Female", "Person", "Lass"});
        Person ourLass = this.objectCreator.instantiateObjectMappedTo(personNodeModel);
        assertNotNull(ourLass);
    }

    @Test(expected = MappingException.class)
    public void shouldFailIfZeroArgConstructorIsNotPresent() {
        EdgeModel edge = new EdgeModel();
        edge.setId(49);
        edge.setType("ClassWithoutZeroArgumentConstructor");
        this.objectCreator.instantiateObjectMappedTo(edge);
    }

    @Test(expected = MappingException.class)
    public void shouldFailIfZeroArgConstructorIsNotVisible() {
        NodeModel vertex = new NodeModel();
        vertex.setId(163);
        vertex.setLabels(new String[] {"ClassWithPrivateConstructor"});
        this.objectCreator.instantiateObjectMappedTo(vertex);
    }

    @Test(expected = MappingException.class)
    public void shouldFailForGraphModelComponentWithNoTaxa() {
        NodeModel vertex = new NodeModel();
        vertex.setId(302);
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

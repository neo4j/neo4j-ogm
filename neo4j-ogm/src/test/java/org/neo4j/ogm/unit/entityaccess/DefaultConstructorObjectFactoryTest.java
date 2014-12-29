package org.neo4j.ogm.unit.entityaccess;

import org.neo4j.ogm.model.NodeModel;
import org.neo4j.ogm.model.RelationshipModel;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.domain.canonical.ClassWithPrivateConstructor;
import org.neo4j.ogm.domain.canonical.ClassWithoutZeroArgumentConstructor;
import org.neo4j.ogm.domain.social.Individual;
import org.neo4j.ogm.entityaccess.ObjectFactory;
import org.neo4j.ogm.metadata.MappingException;
import org.neo4j.ogm.metadata.MetaData;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNotNull;

public class DefaultConstructorObjectFactoryTest {

    private ObjectFactory objectCreator;

    @Before
    public void setUp() {
        Map<String, String> mappings = new HashMap<>();
        mappings.put("ClassWithPrivateConstructor", ClassWithPrivateConstructor.class.getName());
        mappings.put("ClassWithoutZeroArgumentConstructor", ClassWithoutZeroArgumentConstructor.class.getName());
        mappings.put("Person", Individual.class.getName());
        this.objectCreator = new ObjectFactory(new MetaData("org.neo4j.ogm.domain.social", "org.neo4j.ogm.domain.canonical"));
    }

    @Test
    public void shouldConstructObjectOfParticularTypeUsingItsDefaultZeroArgConstructor() {
        RelationshipModel personRelationshipModel = new RelationshipModel();
        personRelationshipModel.setType("Individual");
        Individual gary = this.objectCreator.newObject(personRelationshipModel);
        assertNotNull(gary);

        NodeModel personNodeModel = new NodeModel();
        personNodeModel.setLabels(new String[] {"Individual"});
        Individual sheila = this.objectCreator.newObject(personNodeModel);
        assertNotNull(sheila);
    }

    @Test
    public void shouldHandleMultipleLabelsSafely() {
        NodeModel personNodeModel = new NodeModel();
        personNodeModel.setLabels(new String[] {"Female", "Individual", "Lass"});
        Individual ourLass = this.objectCreator.newObject(personNodeModel);
        assertNotNull(ourLass);
    }

    @Test(expected = MappingException.class)
    public void shouldFailIfZeroArgConstructorIsNotPresent() {
        RelationshipModel edge = new RelationshipModel();
        edge.setId(49L);
        edge.setType("ClassWithoutZeroArgumentConstructor");
        this.objectCreator.newObject(edge);
    }

    @Test(expected = MappingException.class)
    public void shouldFailIfZeroArgConstructorIsNotVisible() {
        NodeModel vertex = new NodeModel();
        vertex.setId(163L);
        vertex.setLabels(new String[] {"ClassWithPrivateConstructor"});
        this.objectCreator.newObject(vertex);
    }

    @Test(expected = MappingException.class)
    public void shouldFailForGraphModelComponentWithNoTaxa() {
        NodeModel vertex = new NodeModel();
        vertex.setId(302L);
        vertex.setLabels(new String[0]);
        this.objectCreator.newObject(vertex);
    }

}

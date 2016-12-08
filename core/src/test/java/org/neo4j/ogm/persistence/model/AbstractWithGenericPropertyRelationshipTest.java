package org.neo4j.ogm.persistence.model;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.neo4j.ogm.annotation.*;
import org.neo4j.ogm.persistence.examples.stage.edges.LastDrama;
import org.neo4j.ogm.persistence.examples.stage.edges.PlayedInDrama;
import org.neo4j.ogm.persistence.examples.stage.nodes.Drama;
import org.neo4j.ogm.persistence.examples.stage.nodes.StageActor;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.MultiDriverTestClass;

/**
 * Issue #54, #66, #186 & Zendesk ticket XXX
 *
 * TODO: The problem here is that FieldInfo will not resolve the correct type for @RelationshipEntity's that have
 * parameterised types.
 *
 * The required behaviour is to delay binding any parameterised field types till a concrete class is defined in a
 * subtype (that is: not define a fieldInfo at the level the parameterised type appears, but remember it and define it
 * when subclasses that define the concrete type appear). At the moment is processes breadth first so it encounters the
 * parameterised type field and binds it to the generic superclass.
 *
 * @author Mihai Raulea
 * @author Mark Angrish
 */
@Ignore
public class AbstractWithGenericPropertyRelationshipTest extends MultiDriverTestClass {

	private static final SessionFactory sessionFactory = new SessionFactory("org.neo4j.ogm.persistence.model", "org.neo4j.ogm.persistence.examples.stage");

	private Session session;


	@Before
	public void init() throws IOException {
		session = sessionFactory.openSession();
		session.purgeDatabase();
	}

	@Test
	public void shouldReloadEntitiesJustSaved() {
		StageActor stageActor = new StageActor("first actor");
		Drama firstDrama = new Drama("malade imaginaire");
		Drama secondDrama = new Drama("Le cid");

		stageActor.dramas.add(new PlayedInDrama(stageActor, firstDrama, "rel1"));
		stageActor.dramas.add(new PlayedInDrama(stageActor, secondDrama, "rel2"));
		stageActor.lastDrama = new LastDrama(stageActor, secondDrama, "last");

		session.save(stageActor);

		session.clear();

		StageActor reloadedActor = session.load(StageActor.class, stageActor.id);

		Assert.assertEquals(2, reloadedActor.dramas.size());
		Assert.assertNotNull(reloadedActor.lastDrama);
	}

	@Test
	public void testSave() {
		SomeNode<Integer> someNode = new SomeNode<>();
		session.save(someNode);

		SomeOtherNode someOtherNode = new SomeOtherNode();
		session.save(someOtherNode);

		RelationshipImplementation<Integer> relationshipImplementation = new RelationshipImplementation<>();
		relationshipImplementation.a = someNode;
		relationshipImplementation.b = someOtherNode;
		session.save(relationshipImplementation);

		AnotherRelationshipImplementation<Integer> anotherRelationshipImplementation = new AnotherRelationshipImplementation<>();
		anotherRelationshipImplementation.a = someNode;
		anotherRelationshipImplementation.b = someOtherNode;
		session.save(anotherRelationshipImplementation);

		// we can not query by abstract class
		Collection<L> relationship = session.loadAll(L.class);
		Assert.assertEquals(relationship.size(), 1);
	}

	@Test
	public void testGenericTypeInConcreteRelationship() {
		SomeNode<Integer> someNode = new SomeNode<>();
		session.save(someNode);

		SomeOtherNode someOtherNode = new SomeOtherNode();
		session.save(someOtherNode);

		RelationshipImplementation<Integer> relationshipImplementation = new RelationshipImplementation<>();
		relationshipImplementation.a = someNode;
		relationshipImplementation.b = someOtherNode;
		session.save(relationshipImplementation);

		AnotherRelationshipImplementation<String> anotherRelationshipImplementation = new AnotherRelationshipImplementation<>();
		anotherRelationshipImplementation.a = someNode;
		anotherRelationshipImplementation.b = someOtherNode;
		session.save(anotherRelationshipImplementation);

		AnotherRelationshipImplementation<String> retrieved = session.load(AnotherRelationshipImplementation.class, anotherRelationshipImplementation.id);
		Assert.assertEquals(retrieved.a.id, someNode.id);
	}

	@RelationshipEntity(type = "otherRelDynamicProperty")
	public class AnotherRelationshipImplementation<T> extends L<T> {

	}


	@RelationshipEntity(type = "relDynamicProperty")
	public class RelationshipImplementation<T> extends L<T> {

	}

	@RelationshipEntity(type = "abstractDynamicProperty")
	public abstract class L<T> {

		Long id;

		private T property;

		@StartNode
		public SomeNode a;

		@EndNode
		public SomeOtherNode b;
	}

	@NodeEntity
	public final class SomeNode<T> {

		Long id;

		@Relationship
		private List<L<T>> listOfLs;
	}

	@NodeEntity
	public final class SomeOtherNode {

		Long id;
	}
}

package org.neo4j.ogm.persistence.model;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.domain.generic_hierarchy.relationship.GenericRelationship;
import org.neo4j.ogm.domain.generic_hierarchy.relationship.SourceEntityWithEntityInterface;
import org.neo4j.ogm.domain.generic_hierarchy.relationship.SourceEntityWithEntitySuperInterface;
import org.neo4j.ogm.domain.generic_hierarchy.relationship.TargetEntityWithEntityInterface;
import org.neo4j.ogm.domain.generic_hierarchy.relationship.TargetEntityWithEntitySuperInterface;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.MultiDriverTestClass;

public class GenericRelationshipEntityTest extends MultiDriverTestClass {
    private Session session;

    @Before
    public void init() throws IOException {
        session = new SessionFactory(driver, "org.neo4j.ogm.domain.generic_hierarchy.relationship").openSession();
        session.purgeDatabase();

    }

    @After
    public void cleanup() {
        session.purgeDatabase();
        session.clear();
    }

    // Test relationship loading with the base interface of all entity/domain classes

    @Test
    public void loadEntitySuperInterfaceToEntitySuperInterfaceRelationship() {
        createSuperInterfaceToSuperInterfaceRelationship();

        SourceEntityWithEntitySuperInterface source = session.loadAll(SourceEntityWithEntitySuperInterface.class)
            .iterator().next();

        assertThat(source.relationship).isNotNull();
        assertThat(source.relationship.target).isNotNull();
    }

    // Test relationship loading with the base interface in source and extending one in target class.
    @Test
    public void loadEntitySuperInterfaceToEntityInterfaceRelationship() {
        createSuperInterfaceToInterfaceRelationship();

        SourceEntityWithEntitySuperInterface source = session.loadAll(SourceEntityWithEntitySuperInterface.class)
            .iterator().next();

        assertThat(source.relationship).isNotNull();
        assertThat(source.relationship.target).isNotNull();
    }

    // Test relationship loading with the extending interface in source and base in target class.
    @Test
    public void loadEntityInterfaceToEntitySuperInterfaceRelationship() {
        createInterfaceToSuperInterfaceRelationship();

        SourceEntityWithEntityInterface source = session.loadAll(SourceEntityWithEntityInterface.class)
            .iterator().next();

        assertThat(source.relationship).isNotNull();
        assertThat(source.relationship.target).isNotNull();
    }

    // Test relationship loading with the extending interface in source and base in target class.
    @Test
    public void loadEntityInterfaceToEntityInterfaceRelationship() {
        createInterfaceToInterfaceRelationship();

        SourceEntityWithEntityInterface source = session.loadAll(SourceEntityWithEntityInterface.class)
            .iterator().next();

        assertThat(source.relationship).isNotNull();
        assertThat(source.relationship.target).isNotNull();
    }

    private void createSuperInterfaceToSuperInterfaceRelationship() {
        SourceEntityWithEntitySuperInterface source = new SourceEntityWithEntitySuperInterface();

        GenericRelationship<SourceEntityWithEntitySuperInterface, TargetEntityWithEntitySuperInterface> relationship = new GenericRelationship<>();
        relationship.source = source;
        relationship.target = new TargetEntityWithEntitySuperInterface();

        source.relationship = relationship;
        session.save(source);
        session.clear();
    }

    private void createSuperInterfaceToInterfaceRelationship() {
        SourceEntityWithEntitySuperInterface source = new SourceEntityWithEntitySuperInterface();

        GenericRelationship<SourceEntityWithEntitySuperInterface, TargetEntityWithEntityInterface> relationship = new GenericRelationship<>();
        relationship.source = source;
        relationship.target = new TargetEntityWithEntityInterface();

        source.relationship = relationship;
        session.save(source);
        session.clear();
    }

    private void createInterfaceToSuperInterfaceRelationship() {
        SourceEntityWithEntityInterface source = new SourceEntityWithEntityInterface();

        GenericRelationship<SourceEntityWithEntityInterface, TargetEntityWithEntitySuperInterface> relationship = new GenericRelationship<>();
        relationship.source = source;
        relationship.target = new TargetEntityWithEntitySuperInterface();

        source.relationship = relationship;
        session.save(source);
        session.clear();
    }

    private void createInterfaceToInterfaceRelationship() {
        SourceEntityWithEntityInterface source = new SourceEntityWithEntityInterface();

        GenericRelationship<SourceEntityWithEntityInterface, TargetEntityWithEntityInterface> relationship = new GenericRelationship<>();
        relationship.source = source;
        relationship.target = new TargetEntityWithEntityInterface();

        source.relationship = relationship;
        session.save(source);
        session.clear();
    }
}

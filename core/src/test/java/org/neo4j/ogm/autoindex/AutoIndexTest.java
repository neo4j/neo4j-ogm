package org.neo4j.ogm.autoindex;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test for parsing the index/constraint description to {@link org.neo4j.ogm.autoindex.AutoIndex}
 * @author Frantisek Hartman
 */
public class AutoIndexTest {

    @Test
    public void parseIndex() throws Exception {
        AutoIndex index = AutoIndex.parse("INDEX ON :Person(name)").get();
        assertThat(index.getOwningType()).isEqualTo("Person");
        assertThat(index.getProperties()).containsOnly("name");
        assertThat(index.getType()).isEqualTo(IndexType.SINGLE_INDEX);
    }

    @Test
    public void parseCompositeIndex() throws Exception {
        AutoIndex index = AutoIndex.parse("INDEX ON :Person(name,id)").get();
        assertThat(index.getOwningType()).isEqualTo("Person");
        assertThat(index.getProperties()).containsOnly("name", "id");
        assertThat(index.getType()).isEqualTo(IndexType.COMPOSITE_INDEX);
    }

    @Test
    public void parseUniqueConstraint() throws Exception {
        AutoIndex index = AutoIndex.parse("CONSTRAINT ON ( person:Person ) ASSERT person.name IS UNIQUE").get();
        assertThat(index.getOwningType()).isEqualTo("Person");
        assertThat(index.getProperties()).containsOnly("name");
        assertThat(index.getType()).isEqualTo(IndexType.UNIQUE_CONSTRAINT);
    }

    @Test
    public void parseNodeKeyConstraint() throws Exception {
        AutoIndex index = AutoIndex
            .parse("CONSTRAINT ON ( person:Person ) ASSERT (person.name, person.id) IS NODE KEY").get();
        assertThat(index.getOwningType()).isEqualTo("Person");
        assertThat(index.getProperties()).containsOnly("name", "id");
        assertThat(index.getType()).isEqualTo(IndexType.NODE_KEY_CONSTRAINT);

    }

    @Test
    public void parseNodePropertyExistenceConstraint() throws Exception {
        AutoIndex index = AutoIndex.parse("CONSTRAINT ON ( person:Person ) ASSERT exists(person.name)").get();
        assertThat(index.getOwningType()).isEqualTo("Person");
        assertThat(index.getProperties()).containsOnly("name");
        assertThat(index.getType()).isEqualTo(IndexType.NODE_PROP_EXISTENCE_CONSTRAINT);
    }

    @Test
    public void shouldRelationshipPropertyExistenceConstraint() throws Exception {
        AutoIndex index = AutoIndex.parse("CONSTRAINT ON ()-[like:LIKED]-() ASSERT exists(like.stars)").get();
        assertThat(index.getOwningType()).isEqualTo("LIKED");
        assertThat(index.getProperties()).containsOnly("stars");
        assertThat(index.getType()).isEqualTo(IndexType.REL_PROP_EXISTENCE_CONSTRAINT);
    }
}

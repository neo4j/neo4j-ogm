package org.neo4j.ogm.session.request.strategy.impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.metadata.DomainInfo;
import org.neo4j.ogm.metadata.schema.DomainInfoSchemaBuilder;
import org.neo4j.ogm.metadata.schema.Schema;

/**
 * @author Frantisek Hartman
 */
public class SchemaRelationshipLoadClauseBuilderTest {

    private SchemaRelationshipLoadClauseBuilder builder;

    @Before
    public void setUp() throws Exception {
        builder = createLoadClauseBuilder();
    }

    @Test(expected = IllegalArgumentException.class)
    public void buildClauseDepthZeroNotAllowed() {


        builder.build("r", "FRIEND_OF", 0);
    }

    /*@Test
    public void buildClauseWithDepthOne() {
        String clause = builder.build("r", "FOUNDED", 1);
        assertThat(clause).isEqualToIgnoringWhitespace(" RETURN r,n,m");
    }*/

    @Test
    public void buildClauseWithDepthTwo() {
        String clause = builder.build("r", "FOUNDED", 1);

        assertThat(clause).isEqualToIgnoringWhitespace(
            " RETURN r,n,"
                + "[ "
                + "[ (n)-[r_f1:`FOUNDED`]->(o1:`Organisation`) | [ r_f1, o1 ] ],"
                + "[ (n)-[r_e1:`EMPLOYED_BY`]->(o1:`Organisation`) | [ r_e1, o1 ] ],"
                + "[ (n)-[r_l1:`LIVES_AT`]->(l1:`Location`) | [ r_l1, l1 ] ] "
                + "],"
                + "m"
        );
    }

    private SchemaRelationshipLoadClauseBuilder createLoadClauseBuilder() {
        DomainInfo domainInfo = DomainInfo.create("org.neo4j.ogm.metadata.schema.simple");
        Schema schema = new DomainInfoSchemaBuilder(domainInfo).build();
        return new SchemaRelationshipLoadClauseBuilder(schema);
    }
}

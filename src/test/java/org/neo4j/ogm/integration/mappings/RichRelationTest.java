package org.neo4j.ogm.integration.mappings;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.neo4j.ogm.domain.mappings.Article;
import org.neo4j.ogm.domain.mappings.Person;
import org.neo4j.ogm.domain.mappings.RichRelation;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.Neo4jIntegrationTestRule;

import java.io.IOException;

/**
 * @author Nils Dröge
 */
public class RichRelationTest {
    @Rule
    public Neo4jIntegrationTestRule neo4jRule = new Neo4jIntegrationTestRule();

    private Session session;

    @Before
    public void init() throws IOException {
        session =  new SessionFactory("org.neo4j.ogm.domain.mappings").openSession(neo4jRule.url());
    }

    @Test
    public void shouldCreateARichRelation()
    {
        Person person = new Person();
        Article article = new Article();
        RichRelation relation = new RichRelation();
        person.addRelation(article, relation);

        session.save(person); // TODO: should not throw a RuntimeException
    }
}

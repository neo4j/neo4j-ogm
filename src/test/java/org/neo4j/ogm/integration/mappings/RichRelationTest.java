package org.neo4j.ogm.integration.mappings;

import java.io.IOException;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.neo4j.ogm.domain.mappings.Article;
import org.neo4j.ogm.domain.mappings.Person;
import org.neo4j.ogm.domain.mappings.RichRelation;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.Neo4jIntegrationTestRule;

/**
 * @author Nils Dröge
 */
@Ignore
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
        session.save(person);

        Article article1 = new Article();
        session.save(article1);
        Article article2 = new Article();
        session.save(article2);

        RichRelation relation1 = new RichRelation();
        person.addRelation(article1, relation1);
        session.save(person, 1);
        session.clear();

        RichRelation relation2 = new RichRelation();
        person.addRelation(article2, relation2);
        session.save(person, 1); // TODO: should not throw a RuntimeException("Couldn't get identity for _1")
    }
}

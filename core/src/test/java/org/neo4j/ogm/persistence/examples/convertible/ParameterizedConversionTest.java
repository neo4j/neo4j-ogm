package org.neo4j.ogm.persistence.examples.convertible;

import org.junit.Test;
import org.neo4j.ogm.domain.convertible.parametrized.JsonNode;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.session.Utils;
import org.neo4j.ogm.testutil.MultiDriverTestClass;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author vince
 */
public class ParameterizedConversionTest extends MultiDriverTestClass {

    @Test
    public void shouldConvertParametrizedMap() {

        SessionFactory sessionFactory = new SessionFactory("org.neo4j.ogm.domain.convertible.parametrized");
        Session session = sessionFactory.openSession();
        session.purgeDatabase();

        JsonNode jsonNode = new JsonNode();
        jsonNode.payload = Utils.map("key", "value");

        session.save(jsonNode);

        session.clear();

        JsonNode found = session.load(JsonNode.class, jsonNode.id);

        assertTrue(found.payload.containsKey("key"));
        assertEquals("value", found.payload.get("key"));

    }
}

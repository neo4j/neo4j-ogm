package org.neo4j.ogm.integration;

import org.junit.experimental.categories.Category;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;

import java.io.IOException;

@Category(IntegrationTest.class)

public class LocalhostServerTest {

    public Session session(String... packages) throws IOException {
        return new SessionFactory(packages).openSession("http://localhost:7474");
    }


}

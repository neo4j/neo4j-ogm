package org.neo4j.ogm.session;

import org.neo4j.ogm.mapper.cypher.CypherQuery;
import org.neo4j.ogm.metadata.MetaData;

public class SessionFactory {

    private final MetaData metaData;
    private final CypherQuery query;

    // todo: bind the query implementation via @inject.
    public SessionFactory(CypherQuery query, String... packages) {
        this.metaData = new MetaData(packages);
        this.query = query;
    }

    public Session openSession() {
        return new Session(metaData, query);
    }

    public void closeSession() {
        // what to do here? what does this mean in terms of the objects that have been created on the session?
    }
}

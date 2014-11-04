package org.neo4j.ogm.session;

import org.neo4j.ogm.metadata.MetaData;

public class SessionFactory {

    private final MetaData metaData;

    public SessionFactory(String... packages) {
        metaData = new MetaData(packages);
    }

    public Session openSession() {
        return new Session(metaData);
    }

    public void closeSession() {
        // what to do here? what does this mean in terms of the objects that have been created on the session?
    }
}

package org.neo4j.ogm.session;

import org.neo4j.ogm.metadata.MetaData;

public class SessionFactory {

    private final MetaData metaData;

    public SessionFactory(String... packages) {
        this.metaData = new MetaData(packages);
    }

    public Session openSession() {
        return new DefaultSessionImpl(metaData);
    }

}

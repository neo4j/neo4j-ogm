package org.neo4j.ogm.session;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.neo4j.ogm.metadata.MetaData;

public class SessionFactory {

    private final MetaData metaData;
    private final CloseableHttpClient httpClient = HttpClients.createDefault();

    public SessionFactory(String... packages) {
        this.metaData = new MetaData(packages);
    }

    public Session openSession(String url) {
        return new DefaultSessionImpl(metaData, url, httpClient);
    }

}

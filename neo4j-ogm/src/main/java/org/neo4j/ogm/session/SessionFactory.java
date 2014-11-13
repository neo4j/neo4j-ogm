package org.neo4j.ogm.session;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.neo4j.ogm.metadata.MetaData;

public class SessionFactory {

    // objectmapper is known to be thread safe. Since its
    // expensive to create them, we have a singleton here, and pass
    // it around to anybody who needs one.

    private static final ObjectMapper objectMapper = new ObjectMapper();

    // this is a threadsafe httpclient that can handle multiple connections simultaneously
    // it takes several seconds to initialise, and we must not have more than one of
    // them.
    private final CloseableHttpClient httpClient = HttpClients.createDefault();
    private final MetaData metaData;

    public SessionFactory(String... packages) {
        this.metaData = new MetaData(packages);
    }

    public Session openSession(String url) {
        return new DefaultSessionImpl(metaData, url, httpClient, objectMapper);
    }

}

package org.neo4j.ogm.session;

import java.util.Collection;

public interface Session {

    void setRequestHandler(Neo4jRequestHandler request);

    <T> T load(Class<T> type, Long id);

    <T> Collection<T> loadAll(Class<T> type, Collection<Long> ids);

    <T> Collection<T> loadAll(Class<T> type);

}

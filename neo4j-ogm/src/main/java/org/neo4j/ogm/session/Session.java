package org.neo4j.ogm.session;

import java.util.Collection;

public interface Session {

    void setRequestHandler(Neo4jRequestHandler request);

    <T> T load(Class<T> type, Long id);

    <T> Collection<T> loadAll(Class<T> type, Collection<Long> ids);

    <T> Collection<T> loadAll(Class<T> type);

    <T> Collection<T> loadAll(Collection<T> objects);

    <T> void deleteAll(Class<T> type);

    void execute(String... commands);

    void purge();

    <T> void save(T object);
}

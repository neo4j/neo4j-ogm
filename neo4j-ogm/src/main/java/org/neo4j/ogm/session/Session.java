package org.neo4j.ogm.session;

import java.util.Collection;

public interface Session {

    <T> T load(Class<T> type, Long id);

    <T> T load(Class<T> type, Long id, int depth);

    <T> Collection<T> loadAll(Class<T> type, Collection<Long> ids);

    <T> Collection<T> loadAll(Class<T> type, Collection<Long> ids, int depth);

    <T> Collection<T> loadAll(Class<T> type);

    <T> Collection<T> loadAll(Class<T> type, int depth);

    <T> Collection<T> loadAll(Collection<T> objects);

    <T> Collection<T> loadAll(Collection<T> objects, int depth);

    //
    <T> void deleteAll(Class<T> type);

    void execute(String jsonStatements);

    void purge();

    <T> void save(T object);

    <T> void save(T object, int depth);
}

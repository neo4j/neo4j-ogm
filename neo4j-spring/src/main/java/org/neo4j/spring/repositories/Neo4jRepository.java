package org.neo4j.spring.repositories;

import org.neo4j.ogm.session.transaction.Transaction;
import org.springframework.data.repository.Repository;

import java.util.Collection;

public interface Neo4jRepository<T> extends Repository<T, Long> {

    T load(Class<T> type, Long id);

    T load(Class<T> type, Long id, int depth);

    Collection<T> loadAll(Class<T> type, Collection<Long> ids);

    Collection<T> loadAll(Class<T> type, Collection<Long> ids, int depth);

    Collection<T> loadAll(Class<T> type);

    Collection<T> loadAll(Class<T> type, int depth);

    Collection<T> loadAll(Collection<T> objects);

    Collection<T> loadAll(Collection<T> objects, int depth);

    void execute(String jsonStatements);

    void purge();

    void save(T object);

    void save(T object, int depth);

    void delete(T object);

    void deleteAll(Class<T> type);

    Transaction beginTransaction();

    void close();

    void flush();


}

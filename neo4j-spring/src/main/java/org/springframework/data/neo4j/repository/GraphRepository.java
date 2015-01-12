package org.springframework.data.neo4j.repository;

import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;

@NoRepositoryBean
public interface GraphRepository<T> extends Repository<T, Long> {

    <S extends T> S save(S entity);
    <S extends T> S save(S s, int depth);

    <S extends T> Iterable<S> save(Iterable<S> entities);
    <S extends T> Iterable<S> save(Iterable<S> entities, int depth);

    T findOne(Long id);
    T findOne(Long id, int depth);

    Iterable<T> findAll();
    Iterable<T> findAll(int depth);

    Iterable<T> findAll(Iterable<Long> ids);
    Iterable<T> findAll(Iterable<Long> ids, int depth);

    Iterable<T> findByProperty(String propertyName, Object propertyValue);
    Iterable<T> findByProperty(String propertyName, Object propertyValue, int depth);

    void delete(Long id);
    void delete(T entity);
    void delete(Iterable<? extends T> entities);
    void deleteAll();

    long count();
    boolean exists(Long id);

}

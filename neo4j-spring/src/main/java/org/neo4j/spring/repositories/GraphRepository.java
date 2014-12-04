package org.neo4j.spring.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface GraphRepository<T> extends CrudRepository<T, Long> {

    <S extends T>  S save(S s, int depth);

    <S extends T>  java.lang.Iterable<S> save(java.lang.Iterable<S> ses, int depth);

    T findOne(Long id, int depth);

    Iterable<T> findAll(int depth);

    Iterable<T> findAll(java.lang.Iterable<Long> ids, int depth);

}

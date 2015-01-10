package org.neo4j.spring.integration.movies.repo;

import org.neo4j.spring.integration.movies.domain.AbstractAnnotatedEntity;
import org.neo4j.spring.integration.movies.domain.AbstractEntity;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AbstractEntityRepository extends GraphRepository<AbstractEntity> {
}

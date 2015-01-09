package org.neo4j.spring.integration.movies.repo;

import org.neo4j.spring.integration.movies.domain.Genre;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GenreRepository extends GraphRepository<Genre> {
}

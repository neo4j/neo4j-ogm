package org.neo4j.spring.integration.cineasts1.repo;

import org.neo4j.spring.integration.cineasts1.domain.Genre;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GenreRepository extends GraphRepository<Genre> {
}

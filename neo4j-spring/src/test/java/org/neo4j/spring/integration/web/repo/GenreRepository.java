package org.neo4j.spring.integration.web.repo;

import org.neo4j.spring.integration.web.domain.Genre;
import org.neo4j.spring.integration.web.domain.User;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GenreRepository extends GraphRepository<Genre> {
}

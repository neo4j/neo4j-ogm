package org.neo4j.spring.integration.cineasts1.repo;

import org.neo4j.spring.integration.cineasts1.domain.Cinema;
import org.neo4j.spring.integration.cineasts1.domain.Genre;
import org.springframework.data.neo4j.repository.GraphRepository;

public interface CinemaRepository extends GraphRepository<Cinema> {
}

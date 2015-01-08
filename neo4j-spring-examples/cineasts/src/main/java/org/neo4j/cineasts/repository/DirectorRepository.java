package org.neo4j.cineasts.repository;

import org.neo4j.cineasts.domain.Director;
import org.springframework.data.neo4j.repository.GraphRepository;

public interface DirectorRepository extends GraphRepository<Director> {
    Director findById(String id);
}

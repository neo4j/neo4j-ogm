package org.neo4j.spring.integration.movies.repo;

import org.neo4j.spring.integration.movies.domain.User;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface UserRepository extends GraphRepository<User> {

    Collection<User> findUsersByName(String name);
}

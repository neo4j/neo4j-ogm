package org.neo4j.cineasts.repository;

import org.neo4j.cineasts.domain.User;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.data.neo4j.repository.RelationshipOperationsRepository;

public interface UserRepository extends GraphRepository<User>,
        RelationshipOperationsRepository<User>,
        CineastsUserDetailsService {

    User findByLogin(String login);
}

package org.neo4j.cineasts.repository;

import org.neo4j.cineasts.domain.User;
import org.springframework.data.neo4j.repository.GraphRepository;

public interface UserRepository extends GraphRepository<User>, CineastsUserDetailsService {

    User findByLogin(String login);
}

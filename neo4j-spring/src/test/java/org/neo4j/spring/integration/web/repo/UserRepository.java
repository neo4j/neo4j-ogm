package org.neo4j.spring.integration.web.repo;

import org.neo4j.spring.integration.web.domain.User;
import org.springframework.context.annotation.Scope;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface UserRepository extends GraphRepository<User> {

//    Collection<User> findUserByName(String name);
}

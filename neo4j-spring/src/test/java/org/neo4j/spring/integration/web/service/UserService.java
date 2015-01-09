package org.neo4j.spring.integration.web.service;

import org.neo4j.spring.integration.web.domain.User;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

public interface UserService {

    User getUserByName(String name);

    Collection<User> getNetwork(User user);


}

package org.neo4j.spring.integration.movies.service;

import org.neo4j.spring.integration.movies.domain.User;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 */
public interface UserService {

    @Transactional
    void updateUser(User user, String newName);

    @Transactional
    void notInterestedIn(Long userId, Long genreId);
}

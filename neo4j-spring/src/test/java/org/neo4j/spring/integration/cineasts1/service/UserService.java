package org.neo4j.spring.integration.cineasts1.service;

import org.neo4j.spring.integration.cineasts1.domain.User;
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

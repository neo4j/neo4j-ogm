package org.neo4j.spring.integration.movies.service;

import org.neo4j.spring.integration.movies.domain.Genre;
import org.neo4j.spring.integration.movies.domain.User;
import org.neo4j.spring.integration.movies.repo.GenreRepository;
import org.neo4j.spring.integration.movies.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GenreRepository genreRepository;

    @Override
    public void updateUser(User user, String newName) {
        user.setName(newName);
    }

    @Override
    public void notInterestedIn(Long userId, Long genreId) {
        User user = userRepository.findOne(userId);
        Genre genre = genreRepository.findOne(genreId);

        user.notInterestedIn(genre);
        userRepository.save(user);
    }
}

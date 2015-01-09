package org.neo4j.spring.integration.web.service;

import org.neo4j.spring.integration.web.domain.User;
import org.neo4j.spring.integration.web.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Transactional
    @Override
    public User getUserByName(String name) {
        Iterable<User> users = userRepository.findByProperty("name", name);
        if (!users.iterator().hasNext()) {
            return null;
        }
        return users.iterator().next();
    }

    @Transactional
    @Override
    public Collection<User> getNetwork(User user) {
        Set<User> network = new HashSet<>();
        buildNetwork(user, network);
        network.remove(user);
        return network;
    }

    private void buildNetwork(User user, Set<User> network) {
        for (User friend : user.getFriends()) {
            if (!network.contains(friend)) {
                network.add(friend);
                buildNetwork(friend, network);
            }
        }
    }
}

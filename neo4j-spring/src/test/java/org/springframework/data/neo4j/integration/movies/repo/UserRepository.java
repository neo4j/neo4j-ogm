package org.springframework.data.neo4j.integration.movies.repo;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.integration.movies.domain.User;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Repository
public interface UserRepository extends GraphRepository<User> {

    Collection<User> findUsersByName(String name);

    @Query("MATCH (user:User) RETURN COUNT(user)")
    int findTotalUsers();

    @Query("MATCH (user:User) RETURN user.id")
    List<Integer> getUserIds();

    @Query("MATCH (user:User) RETURN user")
    List<User> getUsers();

    @Query("MATCH (user:User) RETURN user.name, user.id")
    Iterable<Map<String,Object>> getUsersAsProperties();
}

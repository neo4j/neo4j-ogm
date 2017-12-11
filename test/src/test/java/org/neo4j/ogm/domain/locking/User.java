package org.neo4j.ogm.domain.locking;

import static org.neo4j.ogm.annotation.Relationship.UNDIRECTED;

import java.util.HashSet;
import java.util.Set;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.Version;

/**
 * @author Frantisek Hartman
 */
@NodeEntity
public class User {

    private Long id;

    private String name;

    @Version
    private Long version;

    @Relationship(type = "FRIEND_OF", direction = UNDIRECTED)
    Set<FriendOf> friends = new HashSet<>();

    public User() {
    }

    public User(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    @Override public String toString() {
        return "User{" +
            "id=" + id +
            ", version=" + version +
            '}';
    }

    public FriendOf addFriend(User user) {
        FriendOf friendOf = new FriendOf(this, user);
        friends.add(friendOf);
        user.friends.add(friendOf);
        return friendOf;
    }

    public void clearFriends() {
        friends.clear();
    }
}

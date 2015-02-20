package org.neo4j.ogm.domain.cineasts.partial;

import org.neo4j.ogm.annotation.Relationship;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Actor {

    Long id;
    String name;

    @Relationship(type="ACTS_IN")
    List<Role> roles = new ArrayList<>();

    public Actor()  {}

    public Actor(String name) {
        this.name = name;
    }

    public void addRole(String character, Movie movie) {
        roles.add(new Role(character, this, movie));
    }

    public List<Role> roles() {
        return Collections.unmodifiableList(roles);
    }

    public void removeRole(String character) {

        Iterator<Role> iterator = roles.iterator();
        while (iterator.hasNext()) {
            Role role = iterator.next();
            if (role.played.equals(character)) {
                iterator.remove();
            }
        }
    }

}



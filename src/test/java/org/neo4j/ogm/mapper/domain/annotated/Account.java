package org.neo4j.ogm.mapper.domain.annotated;

import org.neo4j.ogm.annotation.Label;

import java.util.List;

@Label(name="Login")
public class Account {

    Long id;
    List<User> userList;
}

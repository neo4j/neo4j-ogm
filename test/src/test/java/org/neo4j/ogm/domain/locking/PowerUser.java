package org.neo4j.ogm.domain.locking;

import org.neo4j.ogm.annotation.NodeEntity;

/**
 * @author Frantisek Hartman
 */
@NodeEntity
public class PowerUser extends User {

    public PowerUser() {
    }

    public PowerUser(String name) {
        super(name);
    }

}

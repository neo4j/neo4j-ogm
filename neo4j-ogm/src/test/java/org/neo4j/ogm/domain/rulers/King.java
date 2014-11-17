package org.neo4j.ogm.domain.rulers;

public class King extends Monarch {

    @Override
    public String sex() {
        return "Male";
    }

}

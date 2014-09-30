package org.neo4j.ogm.mapper.domain.rulers;

public class King extends Monarch {

    @Override
    public String sex() {
        return "Male";
    }

}

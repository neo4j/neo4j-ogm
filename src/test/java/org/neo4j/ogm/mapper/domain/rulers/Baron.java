package org.neo4j.ogm.mapper.domain.rulers;

public class Baron extends Nobleman {
    @Override
    public String rulesOver() {
        return "Barony";
    }
}

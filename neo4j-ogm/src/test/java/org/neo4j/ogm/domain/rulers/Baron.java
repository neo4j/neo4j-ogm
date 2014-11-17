package org.neo4j.ogm.domain.rulers;

public class Baron extends Nobleman {
    @Override
    public String rulesOver() {
        return "Barony";
    }
}

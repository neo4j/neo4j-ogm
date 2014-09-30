package org.neo4j.ogm.mapper.domain.rulers;

public class Duke extends Nobleman {
    @Override
    public String rulesOver() {
        return "Duchy";
    }
}

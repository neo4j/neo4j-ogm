package org.neo4j.ogm.domain.rulers;

public class Duke extends Nobleman {
    @Override
    public String rulesOver() {
        return "Duchy";
    }
}

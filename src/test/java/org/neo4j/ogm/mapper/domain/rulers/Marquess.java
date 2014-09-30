package org.neo4j.ogm.mapper.domain.rulers;

public class Marquess extends Nobleman {
    @Override
    public String rulesOver() {
        return "March";
    }
}

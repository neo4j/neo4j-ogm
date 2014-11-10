package org.neo4j.ogm.mapper.domain.rulers;

public class Viscount extends Nobleman {
    @Override
    public String rulesOver() {
        return "District";
    }
}

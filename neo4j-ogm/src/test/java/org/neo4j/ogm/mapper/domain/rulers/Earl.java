package org.neo4j.ogm.mapper.domain.rulers;

public class Earl extends Nobleman {
    @Override
    public String rulesOver() {
        return "County";
    }
}

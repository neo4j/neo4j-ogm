package org.neo4j.ogm.mapper.domain.rulers;

public class Princess extends Daughter {

    @Override
    public boolean isCommoner() {
        return false;
    }
}

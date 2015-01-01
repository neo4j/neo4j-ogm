package org.neo4j.ogm.domain.convertible.enums;

import org.neo4j.ogm.annotation.CustomType;

public class Algebra {

    private Long id;

    private NumberSystem numberSystem;

    @CustomType(NumberSystemDomainConverter.class)
    public NumberSystem getNumberSystem() {
        return numberSystem;
    }

    @CustomType(NumberSystemDomainConverter.class)
    public void setNumberSystem(NumberSystem numberSystem) {
        this.numberSystem = numberSystem;
    }
}

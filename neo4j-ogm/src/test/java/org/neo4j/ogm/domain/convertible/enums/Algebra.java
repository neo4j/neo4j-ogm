package org.neo4j.ogm.domain.convertible.enums;

import org.neo4j.ogm.annotation.typeconversion.Convert;

public class Algebra {

    private Long id;

    private NumberSystem numberSystem;

    @Convert(NumberSystemDomainConverter.class)
    public NumberSystem getNumberSystem() {
        return numberSystem;
    }

    @Convert(NumberSystemDomainConverter.class)
    public void setNumberSystem(NumberSystem numberSystem) {
        this.numberSystem = numberSystem;
    }
}

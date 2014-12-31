package org.neo4j.ogm.domain.convertible.enums;

import org.neo4j.ogm.annotation.EnumString;

public class Algebra {

    private Long id;


    @EnumString(NumberSystem.class)
    private NumberSystem numberSystem;

    public NumberSystem getNumberSystem() {
        return numberSystem;
    }

    public void setNumberSystem(NumberSystem numberSystem) {
        this.numberSystem = numberSystem;
    }
}

package org.neo4j.ogm.domain.convertible.enums;

public enum NumberSystem {

    NATURAL("N"),
    INTEGER("Z"),
    RATIONAL("Q"),
    REAL("R"),
    COMPLEX("C");

    private final String domain;

    NumberSystem(String domain) {
        this.domain = domain;
        System.out.println(domain);
    }

    public String getDomain() {
        return domain;
    }

}

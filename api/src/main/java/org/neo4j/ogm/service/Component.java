package org.neo4j.ogm.service;

/**
 * @author vince
 */
public enum Component {

    DRIVER("driver"),
    COMPILER("compiler");

    private final String name;

    Component(String name) {
        this.name = name;
    }

    public String toString() {
        return name;
    }

}

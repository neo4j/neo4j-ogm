package org.neo4j.ogm.api.service;

/**
 * @author vince
 */
public enum Component {

    DRIVER("driver"),
    COMPILER("compiler");

    private String name;

    Component(String name) {
        this.name = name;
    }

    public String toString() {
        return name;
    }

}

package org.neo4j.ogm.metadata.info;

import java.util.HashSet;
import java.util.Set;

/**
 * Direct and ancestral interfaces of a given interface.
 */
public class InterfaceInfo {

    private String interfaceName;
    private Set<InterfaceInfo> superInterfaces = new HashSet<>();

    // what's this for?
    private Set<InterfaceInfo> allSuperInterfaces = new HashSet<>();

    public InterfaceInfo(String name) {
        this.interfaceName = name;
    }

    public Set<InterfaceInfo> superInterfaces() {
        return superInterfaces;
    }

    public Set<InterfaceInfo> allSuperInterfaces() {
        return allSuperInterfaces;
    }

    public String name() {
        return interfaceName;
    }

    public String toString() {
        return name();
    }

}

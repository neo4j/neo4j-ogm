package org.neo4j.ogm.metadata.info;

import java.util.HashSet;
import java.util.Set;

/**
 * Direct and ancestral interfaces of a given interface.
 */
class InterfaceInfo {

    private final String interfaceName;
    private final Set<InterfaceInfo> superInterfaces = new HashSet<>();

    // what's this for?
    private final Set<InterfaceInfo> allSuperInterfaces = new HashSet<>();

    public InterfaceInfo(String name) {
        this.interfaceName = name;
    }

    public Set<InterfaceInfo> superInterfaces() {
        return superInterfaces;
    }

    public Set<InterfaceInfo> allSuperInterfaces() {
        return allSuperInterfaces;
    }

    String name() {
        return interfaceName;
    }

    public String toString() {
        return name();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InterfaceInfo that = (InterfaceInfo) o;

        if (!interfaceName.equals(that.interfaceName)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return interfaceName.hashCode();
    }
}

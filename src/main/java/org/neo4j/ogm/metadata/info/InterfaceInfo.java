package org.neo4j.ogm.metadata.info;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Direct and ancestral interfaces of a given interface.
 */
public class InterfaceInfo {
    ArrayList<String> superInterfaces = new ArrayList<>();

    HashSet<String> allSuperInterfaces = new HashSet<>();

    public InterfaceInfo(ArrayList<String> superInterfaces) {
        this.superInterfaces.addAll(superInterfaces);
    }

}

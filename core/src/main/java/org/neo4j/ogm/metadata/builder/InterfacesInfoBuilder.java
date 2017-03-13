package org.neo4j.ogm.metadata.builder;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.metadata.InterfaceInfo;
import org.neo4j.ogm.metadata.InterfacesInfo;

/**
 * Created by markangrish on 07/03/2017.
 */
public class InterfacesInfoBuilder {

    public static InterfacesInfo create(Class<?> cls) {
        Map<String, InterfaceInfo> interfaceMap = new HashMap<>();
        for (Class iface: cls.getInterfaces()) {
            interfaceMap.put(iface.getName(), new InterfaceInfo(iface.getName()));
        }

        return new InterfacesInfo(interfaceMap);
    }
}

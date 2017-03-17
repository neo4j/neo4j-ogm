/*
 * Copyright (c) 2002-2016 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 *  conditions of the subcomponent's license, as noted in the LICENSE file.
 */

package org.neo4j.ogm.metadata;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Vince Bickers
 */
public class InterfacesInfo {

    private final Map<String, InterfaceInfo> interfaceMap;

    InterfacesInfo() {
        this.interfaceMap = new HashMap<>();
    }

    InterfacesInfo(Class<?> cls) {
        this.interfaceMap = new HashMap<>();
        for (Class iface : cls.getInterfaces()) {
            interfaceMap.put(iface.getName(), new InterfaceInfo(iface));
        }
    }

    public Collection<InterfaceInfo> list() {
        return interfaceMap.values();
    }

    public void append(InterfacesInfo interfacesInfo) {
        for (InterfaceInfo interfaceInfo : interfacesInfo.list()) {
            interfaceMap.put(interfaceInfo.name(), interfaceInfo);
        }
    }
}

/*
 * Copyright (c) 2002-2022 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

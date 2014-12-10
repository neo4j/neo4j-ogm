package org.neo4j.ogm.metadata.info;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

class InterfacesInfo {

    private final Map<String, InterfaceInfo> interfaceMap = new HashMap<>();

    InterfacesInfo() {}

    public InterfacesInfo(DataInputStream dataInputStream, ConstantPool constantPool) throws IOException {
        int interfaceCount = dataInputStream.readUnsignedShort();
        for (int i = 0; i < interfaceCount; i++) {
            String interfaceName = constantPool.lookup(dataInputStream.readUnsignedShort()).replace('/', '.');
            interfaceMap.put(interfaceName, new InterfaceInfo(interfaceName));
        }
    }

    public Collection<InterfaceInfo> list() {
        return interfaceMap.values();
    }

}

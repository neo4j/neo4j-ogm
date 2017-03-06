package org.neo4j.ogm.metadata.bytecode;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.metadata.InterfaceInfo;
import org.neo4j.ogm.metadata.InterfacesInfo;

/**
 * Created by markangrish on 06/03/2017.
 */
public class InterfacesInfoBuilder {

    public static InterfacesInfo create(DataInputStream dataInputStream, ConstantPool constantPool) throws IOException {

        Map<String, InterfaceInfo> interfaceMap = new HashMap<>();

        int interfaceCount = dataInputStream.readUnsignedShort();
        for (int i = 0; i < interfaceCount; i++) {
            String interfaceName = constantPool.lookup(dataInputStream.readUnsignedShort()).replace('/', '.');
            interfaceMap.put(interfaceName, new InterfaceInfo(interfaceName));
        }

        return new InterfacesInfo(interfaceMap);
    }
}

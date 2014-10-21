package org.neo4j.ogm.metadata.info;

import java.io.DataInputStream;
import java.io.IOException;

public class ConstantPool {

    private final Object[] pool;

    public ConstantPool(DataInputStream dataInputStream) throws IOException {

        int cpCount = dataInputStream.readUnsignedShort();
        pool = new Object[cpCount];

        for (int i = 1; i < cpCount; ++i) {
            final int tag = dataInputStream.readUnsignedByte();
            switch (tag) {
                case 1: // Modified UTF8
                    pool[i] = dataInputStream.readUTF();
                    break;
                case 3: // int
                case 4: // float
                    dataInputStream.skipBytes(4);
                    break;
                case 5: // long
                case 6: // double
                    dataInputStream.skipBytes(8);
                    i++; // double slot
                    break;
                case 7: // Class
                case 8: // String
                    // Forward or backward reference a Modified UTF8 entry
                    pool[i] = dataInputStream.readUnsignedShort();
                    break;
                case 9: // field ref
                case 10: // method ref
                case 11: // interface ref
                case 12: // name and type
                    dataInputStream.skipBytes(2); // reference to owning class
                    pool[i]=dataInputStream.readUnsignedShort();
                    break;
                case 15: // method handle
                    dataInputStream.skipBytes(3);
                    break;
                case 16: // method type
                    dataInputStream.skipBytes(2);
                    break;
                case 18: // invoke dynamic
                    dataInputStream.skipBytes(4);
                    break;
                default:
                    throw new ClassFormatError("Unknown tag value for constant pool entry: " + tag);
            }
        }
    }

    public String lookup(int entry) throws IOException {

        Object constantPoolObj = pool[entry];
        return (constantPoolObj instanceof Integer
                ? (String) pool[(Integer) constantPoolObj]
                : (String) constantPoolObj);
    }
}

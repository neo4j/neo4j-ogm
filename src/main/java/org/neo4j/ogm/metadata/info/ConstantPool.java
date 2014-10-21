package org.neo4j.ogm.metadata.info;

import java.io.IOException;

public class ConstantPool {

    private final Object[] pool;

    public ConstantPool(Object[] constantPool) {
        this.pool = constantPool;
    }

    public String lookup(int entry) throws IOException {

        Object constantPoolObj = pool[entry];
        return (constantPoolObj instanceof Integer
                ? (String) pool[(Integer) constantPoolObj]
                : (String) constantPoolObj);
    }
}

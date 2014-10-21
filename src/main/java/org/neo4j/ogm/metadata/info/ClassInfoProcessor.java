package org.neo4j.ogm.metadata.info;

import java.io.IOException;
import java.io.InputStream;

public interface ClassInfoProcessor {

    void process(InputStream inputStream) throws IOException;
    void finish();

}

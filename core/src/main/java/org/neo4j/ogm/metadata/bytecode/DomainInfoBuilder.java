package org.neo4j.ogm.metadata.bytecode;

import java.util.ArrayList;
import java.util.List;

import org.neo4j.ogm.metadata.DomainInfo;

/**
 * Created by markangrish on 07/03/2017.
 */
public class DomainInfoBuilder {

    public static DomainInfo create(String... packages) {
        List<String> classPaths = new ArrayList<>();

        for (String packageName : packages) {
            String path = packageName.replace(".", "/");
            // ensure classpath entries are complete, to ensure we don't accidentally admit partial matches.
            if (!path.endsWith("/")) {
                path = path.concat("/");
            }
            classPaths.add(path);
        }

        DomainInfo domainInfo = new DomainInfo();
        new ClassPathScanner().scan(classPaths, domainInfo);

        return domainInfo;
    }
}

package org.neo4j.ogm.metadata;

import org.neo4j.ogm.metadata.dictionary.ClassDictionary;
import org.neo4j.ogm.metadata.dictionary.FieldDictionary;
import org.neo4j.ogm.metadata.dictionary.MethodDictionary;
import org.neo4j.ogm.metadata.info.DomainInfo;

public class MetaData {

    private final DomainInfo domainInfo;

    // injected:
    private ClassDictionary classDictionary;
    private MethodDictionary methodDictionary;
    private FieldDictionary fieldDictionary;

    public MetaData(String... packages) {
        domainInfo = new DomainInfo(packages);
    }



}

package org.neo4j.ogm.strategy.simple;

import org.neo4j.ogm.metadata.dictionary.ClassDictionary;
import org.neo4j.ogm.metadata.info.ClassInfo;
import org.neo4j.ogm.metadata.info.DomainInfo;

public class SimpleClassDictionary extends ClassDictionary {

    public SimpleClassDictionary(DomainInfo domainInfo) {
        super(domainInfo);
    }

    public Class match(String simpleName) {
        ClassInfo classInfo = domainInfo().getClassSimpleName(simpleName);
        if (classInfo != null) {
            try {
                return Class.forName(classInfo.toString());
            } catch (Exception e) {
                throw  new RuntimeException(e);
            }
        }
        return null;
    }

}

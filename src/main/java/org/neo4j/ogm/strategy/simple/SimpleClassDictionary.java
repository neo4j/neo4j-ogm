package org.neo4j.ogm.strategy.simple;

import org.neo4j.ogm.metadata.dictionary.ClassDictionary;
import org.neo4j.ogm.metadata.dictionary.ClassInfo;

public class SimpleClassDictionary extends ClassDictionary {

    public SimpleClassDictionary(String... packages) {
        super(packages);
    }

    public Class match(String simpleName) {
        ClassInfo classInfo = classify().getClassSimpleName(simpleName);
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

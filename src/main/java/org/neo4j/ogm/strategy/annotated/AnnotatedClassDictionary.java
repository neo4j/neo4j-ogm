package org.neo4j.ogm.strategy.annotated;

import org.neo4j.ogm.metadata.dictionary.ClassDictionary;

public class AnnotatedClassDictionary implements ClassDictionary {
    @Override
    public String determineLeafClass(String... taxa) {
        return null;
    }
}

package org.neo4j.ogm.metadata;

import org.neo4j.ogm.metadata.dictionary.ClassDictionary;
import org.neo4j.ogm.metadata.dictionary.FieldDictionary;
import org.neo4j.ogm.metadata.dictionary.MethodDictionary;
import org.neo4j.ogm.metadata.info.DomainInfo;
import org.neo4j.ogm.strategy.annotated.AnnotatedClassDictionary;
import org.neo4j.ogm.strategy.annotated.AnnotatedFieldDictionary;
import org.neo4j.ogm.strategy.annotated.AnnotatedMethodDictionary;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class MetaData {

    private final DomainInfo domainInfo;

    // todo: injected:
    private ClassDictionary classDictionary;
    private MethodDictionary methodDictionary;
    private FieldDictionary fieldDictionary;

    public MetaData(String... packages) {
        domainInfo = new DomainInfo(packages);
        classDictionary = new AnnotatedClassDictionary(domainInfo);
        methodDictionary = new AnnotatedMethodDictionary(domainInfo);
        fieldDictionary = new AnnotatedFieldDictionary(domainInfo);
    }



}

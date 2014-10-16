package org.neo4j.ogm.strategy.annotated;

//import org.neo4j.ogm.annotation.NodeEntity;

import org.neo4j.ogm.metadata.dictionary.ClassInfo;
import org.neo4j.ogm.strategy.simple.SimpleClassDictionary;

import java.awt.*;

public class AnnotatedClassDictionary extends SimpleClassDictionary {

    public AnnotatedClassDictionary(String... packages) {
        super(packages);
    }

    @Override
    public Class match(String label) {
        String annotation = Label.class.getName();
        for (String fqn : classify().getFQNsWithAnnotation(annotation)) {
            ClassInfo classInfo = classify().getNamedClassWithAnnotation(annotation, fqn);
            try {
                Class clazz=Class.forName(classInfo.toString());
                Label labelClass = (Label) clazz.getAnnotation(Label.class);
                if (labelClass.getName() == null) {
                    if (clazz.getSimpleName().equals(label)) {
                        return clazz;
                    }
                } else {
                    if (labelClass.getName().equals(label)) {
                        return clazz;
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return super.match(label);
    }
}

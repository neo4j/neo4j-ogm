package org.neo4j.ogm.strategy.annotated;

import org.neo4j.ogm.annotation.Label;
import org.neo4j.ogm.metadata.info.AnnotationInfo;
import org.neo4j.ogm.metadata.info.ClassInfo;
import org.neo4j.ogm.metadata.info.DomainInfo;
import org.neo4j.ogm.strategy.simple.SimpleClassDictionary;

import java.util.List;

public class AnnotatedClassDictionary extends SimpleClassDictionary {

    public AnnotatedClassDictionary(DomainInfo domainInfo) {
        super(domainInfo);
    }

    @Override
    public Class match(String label) {
        String annotation = Label.class.getName();
        List<ClassInfo> classInfos = domainInfo().getClassInfosWithAnnotation(annotation);
        if (classInfos != null) {
            for (ClassInfo classInfo : classInfos) {
                try {
                    AnnotationInfo annotationInfo = classInfo.annotationsInfo().get(annotation);
                    String name = annotationInfo.get("name");
                    if (name != null && name.equals(label)) {
                        return Class.forName(classInfo.name());
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return super.match(label);
    }
}

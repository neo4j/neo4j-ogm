package org.neo4j.ogm.metadata.builder;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.metadata.AnnotationInfo;
import org.neo4j.ogm.metadata.AnnotationsInfo;

/**
 * Created by markangrish on 07/03/2017.
 */
public class AnnotationsInfoBuilder {

    public static AnnotationsInfo create(Class<?> cls) {
        Map<String, AnnotationInfo> classAnnotations = new HashMap<>();
        final Annotation[] declaredAnnotations = cls.getDeclaredAnnotations();
        for (Annotation annotation: declaredAnnotations) {
            AnnotationInfo info = AnnotationInfoBuilder.create(annotation);
            classAnnotations.put(info.getName(), info);
        }
        return new AnnotationsInfo(classAnnotations);
    }
}

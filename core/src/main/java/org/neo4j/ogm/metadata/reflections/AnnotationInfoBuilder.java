package org.neo4j.ogm.metadata.reflections;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.metadata.AnnotationInfo;

/**
 * Created by markangrish on 07/03/2017.
 */
public class AnnotationInfoBuilder {

    public static AnnotationInfo create(Annotation annotation) {

        Map<String, String> elements = new HashMap<>();

        final Method[] declaredElements = annotation.annotationType().getDeclaredMethods();
        for (Method element : declaredElements) {
            Object value = null;
            try {
                value = element.invoke(annotation, (Object[]) null);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
            elements.put(element.getName(), value != null ? value.toString() : element.getDefaultValue().toString());
        }

        return new AnnotationInfo(annotation.annotationType().getSimpleName(), elements);
    }
}

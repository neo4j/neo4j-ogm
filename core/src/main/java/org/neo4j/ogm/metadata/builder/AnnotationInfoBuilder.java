package org.neo4j.ogm.metadata.builder;

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
                value = element.invoke(annotation);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException("Could not read value of Annotation " + element.getName());
            }
            elements.put(element.getName(), value != null ? convert(element, value) : element.getDefaultValue().toString());
        }

        return new AnnotationInfo(annotation.annotationType().getName(), elements);
    }

    private static String convert(Method element, Object value) {

        if (element.getReturnType().isPrimitive()) {
            return String.valueOf(value);
        }
        else if (element.getReturnType().equals(Class.class)) {
            return ((Class) value).getName();
        }
        else {
            final String result = value.toString();
            if (result.isEmpty()) {
                if (element.getDefaultValue().toString().isEmpty()) {
                    return null;
                }
                return element.getDefaultValue().toString();
            }
            return result;
        }
    }
}

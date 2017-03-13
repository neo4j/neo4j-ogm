package org.neo4j.ogm.metadata.builder;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.metadata.AnnotationInfo;
import org.neo4j.ogm.metadata.MethodInfo;
import org.neo4j.ogm.metadata.MethodsInfo;
import org.neo4j.ogm.metadata.ObjectAnnotations;

/**
 * Created by markangrish on 07/03/2017.
 */
public class MethodsInfoBuilder {

    public static MethodsInfo create(Class<?> cls) {
        Map<String, MethodInfo> methods = new HashMap<>();

        for (Method method : cls.getDeclaredMethods()) {
            final int modifiers = method.getModifiers();
            if (!Modifier.isTransient(modifiers) && !Modifier.isFinal(modifiers) && !Modifier.isStatic(modifiers)) {
                ObjectAnnotations objectAnnotations = new ObjectAnnotations();
                final Annotation[] declaredAnnotations = method.getDeclaredAnnotations();
                for (Annotation annotation : declaredAnnotations) {
                    AnnotationInfo info = AnnotationInfoBuilder.create(annotation);
                    objectAnnotations.put(info.getName(), info);
                }
                methods.put(method.getName(), new MethodInfo(method.getDeclaringClass().getName(), method.getName(), objectAnnotations));
            }
        }

        return new MethodsInfo(methods);
    }
}

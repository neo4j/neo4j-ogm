package org.neo4j.ogm.metadata.reflections;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.Transient;
import org.neo4j.ogm.metadata.AnnotationInfo;
import org.neo4j.ogm.metadata.FieldInfo;
import org.neo4j.ogm.metadata.FieldsInfo;
import org.neo4j.ogm.metadata.ObjectAnnotations;

/**
 * Created by markangrish on 07/03/2017.
 */
public class FieldsInfoBuilder {

    public static FieldsInfo create(Class<?> cls) {
        Map<String, FieldInfo> fields = new HashMap<>();

        for (Field field : cls.getDeclaredFields()) {
            final int modifiers = field.getModifiers();
            if (!Modifier.isTransient(modifiers) && !Modifier.isFinal(modifiers) && !Modifier.isStatic(modifiers)) {
                ObjectAnnotations objectAnnotations = new ObjectAnnotations();
                final Annotation[] declaredAnnotations = field.getDeclaredAnnotations();
                for (Annotation annotation : declaredAnnotations) {
                    AnnotationInfo info = AnnotationInfoBuilder.create(annotation);
                    objectAnnotations.put(info.getName(), info);
                }
                if (objectAnnotations.get(Transient.class) == null) {
                    fields.put(field.getName(), new FieldInfo(field.getName(), field.getType().getTypeName(), field.getGenericType().getTypeName().equals(field.getType().getTypeName()) ? null: field.getGenericType().getTypeName(), objectAnnotations));
                }
            }
        }
        return new FieldsInfo(fields);
    }
}

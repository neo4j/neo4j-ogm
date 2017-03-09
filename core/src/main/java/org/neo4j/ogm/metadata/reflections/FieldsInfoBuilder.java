package org.neo4j.ogm.metadata.reflections;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
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
                    String typeParameterDescriptor = null;
                    final Type genericType = field.getGenericType();
                    if (genericType instanceof ParameterizedType) {
                        ParameterizedType parameterizedType = (ParameterizedType)genericType;
                        final Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                        if (actualTypeArguments.length > 0) {
                            for (Type typeArgument: actualTypeArguments) {
                                if (typeArgument instanceof ParameterizedType) {
                                    ParameterizedType parameterizedTypeArgument = (ParameterizedType)typeArgument;
                                    typeParameterDescriptor = parameterizedTypeArgument.getRawType().getTypeName();
                                    break;
                                }
                                if (typeArgument instanceof TypeVariable || typeArgument instanceof WildcardType) {
                                    typeParameterDescriptor = Object.class.getName();
                                    break;
                                }
                            }
                        }
                        if (typeParameterDescriptor == null) {
                            typeParameterDescriptor = parameterizedType.getRawType().getTypeName();
                        }
                    }
                    if (typeParameterDescriptor == null && (genericType instanceof TypeVariable)) {
                        typeParameterDescriptor = field.getType().getTypeName();
                    }
                    fields.put(field.getName(), new FieldInfo(field.getName(), field.getType().getTypeName(),typeParameterDescriptor, objectAnnotations));
                }
            }
        }
        return new FieldsInfo(fields);
    }
}

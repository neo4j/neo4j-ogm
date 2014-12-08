package org.neo4j.ogm.entityaccess;

import org.neo4j.ogm.metadata.ClassUtils;
import org.neo4j.ogm.metadata.RelationshipUtils;
import org.neo4j.ogm.metadata.info.ClassInfo;
import org.neo4j.ogm.metadata.info.FieldInfo;
import org.neo4j.ogm.metadata.info.MethodInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Default implementation of {@link ObjectAccessStrategy} that looks up information from {@link ClassInfo} in the following order.
 * <ol>
 * <li>Annotated Method (getter/setter)</li>
 * <li>Annotated Field</li>
 * <li>Plain Method (getter/setter)</li>
 * <li>Plain Field</li>
 * </ol>
 * The rationale is simply that we want annotations, whether on fields or on methods, to always take precedence, and we want to
 * use methods in preference to field access, because in many cases hydrating an object means more than just assigning values to
 * fields.
 */
public class DefaultObjectAccessStrategy implements ObjectAccessStrategy {

    private final Logger logger = LoggerFactory.getLogger(DefaultObjectAccessStrategy.class);

    /** Used internally to hide differences in object construction from strategy algorithm. */
    private static interface AccessorFactory<T> {
        T makeMethodAccessor(MethodInfo methodInfo);
        T makeFieldAccessor(FieldInfo fieldInfo);
    }

    @Override
    public ObjectAccess getPropertyWriter(final ClassInfo classInfo, String propertyName) {
        MethodInfo setterInfo = classInfo.propertySetter(propertyName);
        return determinePropertyAccessor(classInfo, propertyName, setterInfo, new AccessorFactory<ObjectAccess>() {
            @Override
            public ObjectAccess makeMethodAccessor(MethodInfo methodInfo) {
                return new MethodAccess(classInfo, methodInfo);
            }

            @Override
            public ObjectAccess makeFieldAccessor(FieldInfo fieldInfo) {
                return new FieldAccess(classInfo, fieldInfo);
            }
        });
    }

    @Override
    public PropertyReader getPropertyReader(final ClassInfo classInfo, String propertyName) {
        MethodInfo getterInfo = classInfo.propertyGetter(propertyName);
        return determinePropertyAccessor(classInfo, propertyName, getterInfo, new AccessorFactory<PropertyReader>() {
            @Override
            public PropertyReader makeMethodAccessor(MethodInfo methodInfo) {
                return new MethodReader(classInfo, methodInfo);
            }
            @Override
            public PropertyReader makeFieldAccessor(FieldInfo fieldInfo) {
                return new FieldReader(classInfo, fieldInfo);
            }
        });
    }

    private <T> T determinePropertyAccessor(ClassInfo classInfo, String propertyName, MethodInfo accessorMethodInfo,
            AccessorFactory<T> factory) {
        if (accessorMethodInfo != null) {
            if (accessorMethodInfo.getAnnotations().isEmpty()) {
                // if there's an annotated field then we should prefer that over the non-annotated method
                FieldInfo fieldInfo = classInfo.propertyField(propertyName);
                if (fieldInfo != null && !fieldInfo.getAnnotations().isEmpty()) {
                    return factory.makeFieldAccessor(fieldInfo);
                }
            }
            return factory.makeMethodAccessor(accessorMethodInfo);
        }

        // fall back to the field if method cannot be found
        FieldInfo fieldInfo = classInfo.propertyField(propertyName);
        if (fieldInfo != null) {
            return factory.makeFieldAccessor(fieldInfo);
        }
        return null;
    }

    @Override
    public ObjectAccess getRelationalWriter(ClassInfo classInfo, String relationshipType, Object parameter) {

        // 1st, try to find a method annotated with the relationship type.
        MethodInfo methodInfo = classInfo.relationshipSetter(relationshipType);
        if (methodInfo != null && !methodInfo.getAnnotations().isEmpty()) {
            Class<?> setterParameterType = ClassUtils.getType(methodInfo.getDescriptor());
            if (setterParameterType.isAssignableFrom(parameter.getClass())) {
                return new MethodAccess(classInfo, methodInfo);
            }
        }

        // 2nd, try to find a field called or annotated as the neo4j relationship type
        FieldInfo fieldInfo = classInfo.relationshipField(relationshipType);
        if (fieldInfo != null && !fieldInfo.getAnnotations().isEmpty() && fieldInfo.isTypeOf(parameter.getClass())) {
            return new FieldAccess(classInfo, fieldInfo);
        }

        // 3rd, try to find a "setXYZ" method where XYZ is derived from the relationship type
        methodInfo = classInfo.relationshipSetter(relationshipType);
        if (methodInfo != null) {
            Class<?> setterParameterType = ClassUtils.getType(methodInfo.getDescriptor());
            if (setterParameterType.isAssignableFrom(parameter.getClass())) {
                return new MethodAccess(classInfo, methodInfo);
            }
        }

        // 4th, try to find a "XYZ" field name where XYZ is derived from the relationship type
        fieldInfo = classInfo.relationshipField(RelationshipUtils.inferFieldName(relationshipType));
        if (fieldInfo != null && fieldInfo.isTypeOf(parameter.getClass())) {
            return new FieldAccess(classInfo, fieldInfo);
        }

        // 5th, try to find a single setter that takes the parameter
        List<MethodInfo> methodInfos = classInfo.findSetters(parameter.getClass());
        if (methodInfos.size() == 1) {
            return new MethodAccess(classInfo, methodInfos.iterator().next());
        }

        // 6th, try to find a field that shares the same type as the parameter
        List<FieldInfo> fieldInfos = classInfo.findFields(parameter.getClass());
        if (fieldInfos.size() == 1) {
            return new FieldAccess(classInfo, fieldInfos.iterator().next());
        }

        return null;
    }

    @Override
    public RelationalReader getRelationalReader(ClassInfo classInfo, String relationshipType) {
        // 1st, try to find a method annotated with the relationship type.
        MethodInfo methodInfo = classInfo.relationshipGetter(relationshipType);
        if (methodInfo != null && !methodInfo.getAnnotations().isEmpty()) {
            return new MethodReader(classInfo, methodInfo);
        }

        // 2nd, try to find a field called or annotated as the neo4j relationship type
        FieldInfo fieldInfo = classInfo.relationshipField(relationshipType);
        if (fieldInfo != null && !fieldInfo.getAnnotations().isEmpty()) {
            return new FieldReader(classInfo, fieldInfo);
        }

        // 3rd, try to find a "getXYZ" method where XYZ is derived from the given relationship type
        methodInfo = classInfo.relationshipGetter(relationshipType);
        if (methodInfo != null) {
            return new MethodReader(classInfo, methodInfo);
        }

        // 4th, try to find a "XYZ" field name where XYZ is derived from the relationship type
        fieldInfo = classInfo.relationshipField(RelationshipUtils.inferFieldName(relationshipType));
        if (fieldInfo != null) {
            return new FieldReader(classInfo, fieldInfo);
        }
        return null;
    }

    @Override
    public Collection<PropertyReader> getPropertyReaders(ClassInfo classInfo) {
        // do we care about "implicit" fields?  i.e., setX/getX with no matching X field

        Collection<PropertyReader> readers = new ArrayList<>();
        for (FieldInfo fieldInfo : classInfo.propertyFields()) {
            MethodInfo getterInfo = classInfo.propertyGetter(fieldInfo.property());
            if (getterInfo != null) {
                if (!getterInfo.getAnnotations().isEmpty() || fieldInfo.getAnnotations().isEmpty()) {
                    readers.add(new MethodReader(classInfo, getterInfo));
                    continue;
                }
            }
            readers.add(new FieldReader(classInfo, fieldInfo));
        }
        return readers;
    }

    @Override
    public Collection<RelationalReader> getRelationalReaders(ClassInfo classInfo) {
        Collection<RelationalReader> readers = new ArrayList<>();
        for (FieldInfo fieldInfo : classInfo.relationshipFields()) {
            MethodInfo getterInfo = classInfo.methodsInfo().get(inferGetterName(fieldInfo));

            if (getterInfo != null) {
                if (!getterInfo.getAnnotations().isEmpty() || fieldInfo.getAnnotations().isEmpty()) {
                    readers.add(new MethodReader(classInfo, getterInfo));
                    continue;
                }
            }
            readers.add(new FieldReader(classInfo, fieldInfo));
        }
        return readers;
    }

    private static String inferGetterName(FieldInfo fieldInfo) {
        StringBuilder getterNameBuilder = new StringBuilder(fieldInfo.getName());
        getterNameBuilder.setCharAt(0, Character.toUpperCase(fieldInfo.getName().charAt(0)));
        return getterNameBuilder.insert(0, "get").toString();
    }

    @Override
    public ObjectAccess getIterableWriter(ClassInfo classInfo, Class<?> parameterType) {
        MethodInfo methodInfo = getIterableMethodInfo(classInfo, parameterType);
        if (methodInfo != null) {
            return new MethodAccess(classInfo, methodInfo);
        }
        FieldInfo fieldInfo = getIterableFieldInfo(classInfo, parameterType);
        if (fieldInfo != null) {
            return new FieldAccess(classInfo, fieldInfo);
        }
        return null;
    }

    @Override
    public PropertyReader getIdentityPropertyReader(ClassInfo classInfo) {
        return new FieldReader(classInfo, classInfo.identityField());
    }

    private MethodInfo getIterableMethodInfo(ClassInfo classInfo, Class<?> parameterType) {
        List<MethodInfo> methodInfos = classInfo.findIterableSetters(parameterType);
        if (methodInfos.size() == 1) {
            return methodInfos.iterator().next();
        }

        logger.warn("Cannot map iterable of {} to instance of {}.  More than one potential matching setter found.",
                parameterType, classInfo.name());
        return null;
    }

    private FieldInfo getIterableFieldInfo(ClassInfo classInfo, Class<?> parameterType) {
        List<FieldInfo> fieldInfos = classInfo.findIterableFields(parameterType);
        if (fieldInfos.size() == 1) {
            return fieldInfos.iterator().next();
        }

        logger.warn("Cannot map iterable of {} to instance of {}.  More than one potential matching field found.",
                parameterType, classInfo.name());
        return null;
    }

}

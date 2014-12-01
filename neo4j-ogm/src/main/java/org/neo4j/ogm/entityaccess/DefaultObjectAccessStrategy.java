package org.neo4j.ogm.entityaccess;

import java.util.List;

import org.neo4j.ogm.metadata.ClassUtils;
import org.neo4j.ogm.metadata.info.ClassInfo;
import org.neo4j.ogm.metadata.info.FieldInfo;
import org.neo4j.ogm.metadata.info.MethodInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        return determineAccessor(classInfo, propertyName, setterInfo, new AccessorFactory<ObjectAccess>() {
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
        return determineAccessor(classInfo, propertyName, getterInfo, new AccessorFactory<PropertyReader>() {
            @Override
            public PropertyReader makeMethodAccessor(MethodInfo methodInfo) {
                return new MethodPropertyReader(classInfo, methodInfo);
            }
            @Override
            public PropertyReader makeFieldAccessor(FieldInfo fieldInfo) {
                return new FieldPropertyReader(classInfo, fieldInfo);
            }
        });
    }

    private <T> T determineAccessor(ClassInfo classInfo, String propertyName, MethodInfo methodInfo, AccessorFactory<T> factory) {
        if (methodInfo != null) {
            if (methodInfo.getAnnotations().isEmpty()) {
                // if there's an annotated field then we should prefer that over the non-annotated method
                FieldInfo fieldInfo = classInfo.propertyField(propertyName);
                if (fieldInfo != null && !fieldInfo.getAnnotations().isEmpty()) {
                    return factory.makeFieldAccessor(fieldInfo);
                }
            }
            return factory.makeMethodAccessor(methodInfo);
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
            return new MethodAccess(classInfo, methodInfo);
        }

        // 2nd, try to find a field called or annotated as the neo4j relationship type
        FieldInfo fieldInfo = classInfo.relationshipField(relationshipType);
        if (fieldInfo != null && !fieldInfo.getAnnotations().isEmpty() && fieldInfo.isTypeOf(parameter.getClass())) {
            return new FieldAccess(classInfo, fieldInfo);
        }

        // 3rd, try to find a "setXYZ" method where XYZ is derived from the relationship type
        methodInfo = classInfo.relationshipSetter(setterNameFromRelationshipType(relationshipType));
        if (methodInfo != null) {
            Class<?> setterParameterType = ClassUtils.getType(methodInfo.getDescriptor());
            if (setterParameterType.isAssignableFrom(parameter.getClass())) {
                return new MethodAccess(classInfo, methodInfo);
            }
        }

        // 4th, try to find a "XYZ" field name where XYZ is derived from the relationship type
        fieldInfo = classInfo.relationshipField(fieldNameFromRelationshipType(relationshipType));
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

    private String setterNameFromRelationshipType(String relationshipType) {
        StringBuilder setterName = resolveMemberFromRelationshipType(new StringBuilder("set"), relationshipType);
        return setterName.toString();
    }

    private String fieldNameFromRelationshipType(String relationshipType) {
        StringBuilder fieldName = resolveMemberFromRelationshipType(new StringBuilder(), relationshipType);
        fieldName.setCharAt(0, Character.toLowerCase(fieldName.charAt(0)));
        return fieldName.toString();
    }

    // guesses the name of a type accessor method, based on the supplied graph attribute
    // the graph attribute can be a node property, e.g. "Name", or a relationship type e.g. "LIKES"
    //
    // A simple attribute e.g. "PrimarySchool" will be mapped to a value "[get,set]PrimarySchool"
    //
    // An attribute with elements separated by underscores will have each element processed and then
    // the parts will be elided to a camelCase name. Elements that imply structure, ("HAS", "IS", "A")
    // will be excluded from the mapping, i.e:
    //
    // "HAS_WHEELS"             => "[get,set]Wheels"
    // "IS_A_BRONZE_MEDALLIST"  => "[get,set]BronzeMedallist"
    // "CHANGED_PLACES_WITH"    => "[get,set]ChangedPlacesWith"
    //
    // the MethodInfo class should help here at some point, but its not accessible currently
    private static StringBuilder resolveMemberFromRelationshipType(StringBuilder sb, String name) {
        if (name != null && name.length() > 0) {
            if (!name.contains("_")) {
                sb.append(name.substring(0, 1).toUpperCase());
                sb.append(name.substring(1).toLowerCase());
            } else {
                String[] parts = name.split("_");
                for (String part : parts) {
                    String test = part.toLowerCase();
                    if ("has|is|a".contains(test)) {
                        continue;
                    }
                    resolveMemberFromRelationshipType(sb, test);
                }
            }
        }
        return sb;
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

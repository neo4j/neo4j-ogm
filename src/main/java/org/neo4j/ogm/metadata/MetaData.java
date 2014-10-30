package org.neo4j.ogm.metadata;

import org.neo4j.ogm.annotation.Label;
import org.neo4j.ogm.annotation.NodeId;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.metadata.info.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.*;

public class MetaData {

    private final DomainInfo domainInfo;

    public MetaData(String... packages) {
        domainInfo = new DomainInfo(packages);
    }

    /**
     * Finds the ClassInfo for the supplied partial class name or label
     *
     * @param name the simple class name or label for a class we want to find
     * @return A ClassInfo matching the supplied name, or null if it doesn't exist
     */
    public ClassInfo classInfo(String name) {
        String annotation = Label.class.getName();
        List<ClassInfo> labelledClasses = domainInfo.getClassInfosWithAnnotation(annotation);
        if (labelledClasses != null) {
            for (ClassInfo labelledClass : labelledClasses) {
                AnnotationInfo annotationInfo = labelledClass.annotationsInfo().get(annotation);
                String value = annotationInfo.get("name", labelledClass.name());
                if (value.equals(name)) {
                    return labelledClass;
                }
            }
        }
        return domainInfo.getClassSimpleName(name);
    }

    /**
     * The identity field is a field annotated with @NodeId, or if none exists, a field
     * of type Long called 'id'
     *
     * @param classInfo the ClassInfo whose fields may contain an identity field
     * @return A FieldInfo object representing the identity field or null if it doesn't exist
     */
    public FieldInfo identityField(ClassInfo classInfo) {
        for (FieldInfo fieldInfo : classInfo.fieldsInfo().fields()) {
            AnnotationInfo annotationInfo = fieldInfo.getAnnotations().get(NodeId.class.getName());
            if (annotationInfo != null) {
                if (fieldInfo.getDescriptor().equals("Ljava/lang/Long;")) {
                    return fieldInfo;
                }
            }
        }
        FieldInfo fieldInfo = classInfo.fieldsInfo().get("id");
        if (fieldInfo != null) {
            if (fieldInfo.getDescriptor().equals("Ljava/lang/Long;")) {
                return fieldInfo;
            }
        }
        return null;
    }

    /**
     * A property field is any field annotated with @Property, or any field that can be mapped to a
     * node property. The identity field is not a property field.
     *
     * @param classInfo the ClassInfo whose fields may contain property fields
     * @return A Collection of FieldInfo objects describing the classInfo's property fields
     */
    public Collection<FieldInfo> propertyFields(ClassInfo classInfo) {
        FieldInfo identityField = identityField(classInfo);
        Set<FieldInfo> fieldInfos = new HashSet<>();
        for (FieldInfo fieldInfo : classInfo.fieldsInfo().fields()) {
            if (!fieldInfo.getName().equals(identityField.getName())) {
                // todo: when building fieldInfos, we must exclude fields annotated @Transient, or with the transient modifier
                if (fieldInfo.getAnnotations().isEmpty()) {
                    if (fieldInfo.isSimple()) {
                        fieldInfos.add(fieldInfo);
                    }
                } else {
                    AnnotationInfo annotationInfo = fieldInfo.getAnnotations().get(Property.class.getName());
                    if (annotationInfo != null) {
                        fieldInfos.add(fieldInfo);
                    }
                }
            }
        }
        return fieldInfos;
    }

    /**
     * A relationship field is any field annotated with @Relationship, or any field that cannot be mapped to a
     * node property. The identity field is not a relationship field.
     *
     * @param classInfo the ClassInfo whose fields may contain relationship fields
     * @return A Collection of FieldInfo objects describing the classInfo's relationship fields
     */
    public Collection<FieldInfo> relationshipFields(ClassInfo classInfo) {
        FieldInfo identityField = identityField(classInfo);
        Set<FieldInfo> fieldInfos = new HashSet<>();
        for (FieldInfo fieldInfo : classInfo.fieldsInfo().fields()) {
            //System.out.println(fieldInfo.getName() + ": " + fieldInfo.getDescriptor());
            if (fieldInfo != identityField) {
                // todo: when building fieldInfos, we must exclude fields annotated @Transient, or with the transient modifier
                if (fieldInfo.getAnnotations().isEmpty()) {
                    if (!fieldInfo.isSimple()) {
                        fieldInfos.add(fieldInfo);
                    }
                } else {
                    AnnotationInfo annotationInfo = fieldInfo.getAnnotations().get(Relationship.class.getName());
                    if (annotationInfo != null) {
                        fieldInfos.add(fieldInfo);
                    }
                }
            }
        }
        return fieldInfos;
    }

    /**
     * Finds the relationship field with a specific name from the specified ClassInfo's relationship fields
     *
     * @param classInfo        the ClassInfo whose fields may contain the required relationship field
     * @param relationshipName the relationshipName of the field to find
     * @return A FieldInfo object describing the required relationship field, or null if it doesn't exist.
     */
    public FieldInfo relationshipField(ClassInfo classInfo, String relationshipName) {
        for (FieldInfo fieldInfo : relationshipFields(classInfo)) {
            if (fieldInfo.relationship().equalsIgnoreCase(relationshipName)) {
                return fieldInfo;
            }
        }
        return null;
    }

    /**
     * Finds the property field with a specific name from the specified ClassInfo's property fields
     *
     * @param classInfo    the ClassInfo whose fields may contain the required property field
     * @param propertyName the propertyName of the field to find
     * @return A FieldInfo object describing the required property field, or null if it doesn't exist.
     */
    public FieldInfo propertyField(ClassInfo classInfo, String propertyName) {
        for (FieldInfo fieldInfo : propertyFields(classInfo)) {
            if (fieldInfo.property().equalsIgnoreCase(propertyName)) {
                return fieldInfo;
            }
        }
        return null;
    }

    /**
     * The identity getter is any getter annotated with @NodeId returning a Long, or if none exists, a getter
     * returning Long called 'getId'
     *
     * @param classInfo the ClassInfo whose fields may contain an identity field
     * @return A FieldInfo object representing the identity field or null if it doesn't exist
     */
    public MethodInfo identityGetter(ClassInfo classInfo) {
        for (MethodInfo methodInfo : classInfo.methodsInfo().getters()) {
            //System.out.println(methodInfo.getName() + ": " + methodInfo.getDescriptor());
            AnnotationInfo annotationInfo = methodInfo.getAnnotations().get(NodeId.class.getName());
            if (annotationInfo != null) {
                if (methodInfo.getDescriptor().equals("()Ljava/lang/Long;")) {
                    return methodInfo;
                }
            }
        }
        MethodInfo methodInfo = classInfo.methodsInfo().get("getId");
        if (methodInfo != null) {
            if (methodInfo.getDescriptor().equals("()Ljava/lang/Long;")) {
                return methodInfo;
            }
        }
        return null;
    }

    /**
     * The identity setter is any setter annotated with @NodeId taking a Long parameter, or if none exists, a setter
     * called 'setId' taking a Long parameter
     *
     * @param classInfo the ClassInfo whose fields may contain an identity field
     * @return A FieldInfo object representing the identity field or null if it doesn't exist
     */
    public MethodInfo identitySetter(ClassInfo classInfo) {
        for (MethodInfo methodInfo : classInfo.methodsInfo().setters()) {
            AnnotationInfo annotationInfo = methodInfo.getAnnotations().get(NodeId.class.getName());
            if (annotationInfo != null) {
                if (methodInfo.getDescriptor().equals("(Ljava/lang/Long;)V")) {
                    return methodInfo;
                }
            }
        }
        MethodInfo methodInfo = classInfo.methodsInfo().get("setId");
        if (methodInfo != null) {
            if (methodInfo.getDescriptor().equals("(Ljava/lang/Long;)V")) {
                return methodInfo;
            }
        }
        return null;
    }

    /**
     * A property getter is any getter annotated with @Property, or any getter whose return type can be mapped to a
     * node property. The identity getter is not a property getter.
     *
     * @param classInfo the ClassInfo whose methods may contain property getters
     * @return A Collection of MethodInfo objects describing the classInfo's property getters
     */
    public Collection<MethodInfo> propertyGetters(ClassInfo classInfo) {
        MethodInfo identityGetter = identityGetter(classInfo);
        Set<MethodInfo> propertyGetters = new HashSet<>();
        for (MethodInfo methodInfo : classInfo.methodsInfo().getters()) {
            if (identityGetter == null || !methodInfo.getName().equals(identityGetter.getName())) {
                if (methodInfo.getAnnotations().isEmpty()) {
                    if (methodInfo.isSimpleGetter()) {
                        propertyGetters.add(methodInfo);
                    }
                } else {
                    AnnotationInfo annotationInfo = methodInfo.getAnnotations().get(Property.class.getName());
                    if (annotationInfo != null) {
                        propertyGetters.add(methodInfo);
                    }
                }
            }
        }
        return propertyGetters;
    }

    /**
     * A property setter is any setter annotated with @Property, or any setter whose parameter type can be mapped to a
     * node property. The identity setter is not a property setter.
     *
     * @param classInfo the ClassInfo whose methods may contain property setters
     * @return A Collection of MethodInfo objects describing the classInfo's property setters
     */
    public Collection<MethodInfo> propertySetters(ClassInfo classInfo) {
        MethodInfo identitySetter = identitySetter(classInfo);
        Set<MethodInfo> propertySetters = new HashSet<>();
        for (MethodInfo methodInfo : classInfo.methodsInfo().setters()) {
            if (identitySetter == null || !methodInfo.getName().equals(identitySetter.getName())) {
                if (methodInfo.getAnnotations().isEmpty()) {
                    if (methodInfo.isSimpleSetter()) {
                        propertySetters.add(methodInfo);
                    }
                } else {
                    AnnotationInfo annotationInfo = methodInfo.getAnnotations().get(Property.class.getName());
                    if (annotationInfo != null) {
                        propertySetters.add(methodInfo);
                    }
                }
            }
        }
        return propertySetters;
    }

    /**
     * A relationship getter is any getter annotated with @Relationship, or any getter whose return type cannot be mapped to a
     * node property. The identity getter is not a property getter.
     *
     * @param classInfo the ClassInfo whose methods may contain property getters
     * @return A Collection of MethodInfo objects describing the classInfo's property getters
     */
    public Collection<MethodInfo> relationshipGetters(ClassInfo classInfo) {
        MethodInfo identityGetter = identityGetter(classInfo);
        Set<MethodInfo> relationshipGetters = new HashSet<>();
        for (MethodInfo methodInfo : classInfo.methodsInfo().getters()) {
            if (identityGetter == null || !methodInfo.getName().equals(identityGetter.getName())) {
                if (methodInfo.getAnnotations().isEmpty()) {
                    if (!methodInfo.isSimpleGetter()) {
                        relationshipGetters.add(methodInfo);
                    }
                } else {
                    AnnotationInfo annotationInfo = methodInfo.getAnnotations().get(Relationship.class.getName());
                    if (annotationInfo != null) {
                        relationshipGetters.add(methodInfo);
                    }
                }
            }
        }
        return relationshipGetters;
    }

    /**
     * A relationship setter is any setter annotated with @Relationship, or any setter whose parameter type cannot be mapped to a
     * node property. The identity setter is not a property getter.
     *
     * @param classInfo the ClassInfo whose methods may contain property getters
     * @return A Collection of MethodInfo objects describing the classInfo's property getters
     */
    public Collection<MethodInfo> relationshipSetters(ClassInfo classInfo) {
        MethodInfo identitySetter = identitySetter(classInfo);
        Set<MethodInfo> relationshipSetters = new HashSet<>();
        for (MethodInfo methodInfo : classInfo.methodsInfo().setters()) {
            if (identitySetter == null || !methodInfo.getName().equals(identitySetter.getName())) {
                if (methodInfo.getAnnotations().isEmpty()) {
                    if (!methodInfo.isSimpleSetter()) {
                        relationshipSetters.add(methodInfo);
                    }
                } else {
                    AnnotationInfo annotationInfo = methodInfo.getAnnotations().get(Relationship.class.getName());
                    if (annotationInfo != null) {
                        relationshipSetters.add(methodInfo);
                    }
                }
            }
        }
        return relationshipSetters;
    }

    /**
     * Finds the relationship getter with a specific name from the specified ClassInfo's relationship getters
     *
     * @param classInfo        the ClassInfo whose getters may contain the required relationship getter
     * @param relationshipName the relationshipName of the getter to find
     * @return A MethodInfo object describing the required relationship getter, or null if it doesn't exist.
     */
    public MethodInfo relationshipGetter(ClassInfo classInfo, String relationshipName) {
        for (MethodInfo methodInfo : relationshipGetters(classInfo)) {
            if (methodInfo.relationship().equalsIgnoreCase(relationshipName)) {
                return methodInfo;
            }
        }
        return null;
    }

    /**
     * Finds the relationship setter with a specific name from the specified ClassInfo's relationship setters
     *
     * @param classInfo        the ClassInfo whose setters may contain the required relationship setter
     * @param relationshipName the relationshipName of the setter to find
     * @return A MethodInfo object describing the required relationship setter, or null if it doesn't exist.
     */
    public MethodInfo relationshipSetter(ClassInfo classInfo, String relationshipName) {
        for (MethodInfo methodInfo : relationshipSetters(classInfo)) {
            if (methodInfo.relationship().equalsIgnoreCase(relationshipName)) {
                return methodInfo;
            }
        }
        return null;
    }

    /**
     * Finds the property setter with a specific name from the specified ClassInfo's property setters
     *
     * @param classInfo    the ClassInfo whose setters may contain the required property setter
     * @param propertyName the propertyName of the setter to find
     * @return A MethodInfo object describing the required property setter, or null if it doesn't exist.
     */
    public MethodInfo propertySetter(ClassInfo classInfo, String propertyName) {
        for (MethodInfo methodInfo : propertySetters(classInfo)) {
            if (methodInfo.property().equalsIgnoreCase(propertyName)) {
                return methodInfo;
            }
        }
        return null;
    }

    /**
     * Finds the property getter with a specific name from the specified ClassInfo's property getters
     *
     * @param classInfo    the ClassInfo whose getters may contain the required property getter
     * @param propertyName the propertyName of the getter to find
     * @return A MethodInfo object describing the required property getter, or null if it doesn't exist.
     */
    public MethodInfo propertyGetter(ClassInfo classInfo, String propertyName) {
        for (MethodInfo methodInfo : propertyGetters(classInfo)) {
            if (methodInfo.property().equalsIgnoreCase(propertyName)) {
                return methodInfo;
            }
        }
        return null;
    }

    /**
     * Given an set of fully qualified names that are possibly within a type hierarchy
     * This function returns the base class from among them.
     * @param
     * @param taxa the taxa (simple class names or labels)
     * @return The ClassInfo representing the base class among the taxa
     */
    public ClassInfo resolve(String... taxa) {
        if (taxa.length > 0) {
            Set<ClassInfo> baseClasses = new HashSet<>();
            for (String taxon : taxa) {
                ClassInfo taxonClassInfo = classInfo(taxon);
                if (taxonClassInfo != null) {
                    ClassInfo baseClassInfo = resolveBaseClass(taxonClassInfo, taxonClassInfo.directSubclasses());
                    if (baseClassInfo != null) {
                        baseClasses.add(baseClassInfo);
                    }
                }
            }
            if (baseClasses.size() > 1) {
                // todo logger.warn
                System.out.println("Multiple leaf classes found in type hierarchy for specified taxa: " + Arrays.toString(taxa) + ". leaf classes are: " + baseClasses);
                return null;
            }
            if (baseClasses.iterator().hasNext()) {
                return baseClasses.iterator().next();
            }
        }
        return null;
    }

    private ClassInfo resolveBaseClass(ClassInfo fqn, List<ClassInfo> classInfoList) {
        if (classInfoList.isEmpty()) {
            return fqn;
        }
        if (classInfoList.size() > 1) {
            // todo logger.warn
            System.out.println("More than one class subclasses " + fqn);
            return null; // turn back oh Man - forget thy foolish ways
        }
        ClassInfo classInfo = classInfoList.iterator().next();
        return resolveBaseClass(classInfo, classInfo.directSubclasses());

    }

    /**
     *
     * @param classInfo
     * @param fieldInfo
     * @return
     */
    public boolean isScalar(ClassInfo classInfo, FieldInfo fieldInfo) {
        Field field = getField(classInfo, fieldInfo);
        return(!Collection.class.isAssignableFrom(field.getType()) && !fieldInfo.getDescriptor().contains("["));
    }

    /**
     *
     * @param classInfo
     * @param fieldInfo
     * @return
     */
    public Field getField(ClassInfo classInfo, FieldInfo fieldInfo) {
        try {
            return Class.forName(classInfo.name()).getDeclaredField(fieldInfo.getName());
        } catch (NoSuchFieldException e) {
            if (classInfo.directSuperclass() != null) {
                return getField(classInfo.directSuperclass(), fieldInfo);
            } else {
                throw new RuntimeException("Field " + fieldInfo.getName() + " not found in class " + classInfo.name() + " or any of its superclasses");
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     *
     * @param classInfo
     * @param methodInfo
     * @param parameter
     * @return
     */
    public Method getMethod(ClassInfo classInfo, MethodInfo methodInfo, Class... parameter) {
        try {
            return Class.forName(classInfo.name()).getDeclaredMethod(methodInfo.getName(), parameter);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Find all setter MethodInfos for the specified ClassInfo whose parameter type matches the supplied class
     *
     * @param classInfo The ClassInfo instance whose MethodInfos will be examined
     * @param parameterType  the setter parameter type to look for.
     * @return
     */
    public List<MethodInfo> findSetters(ClassInfo classInfo, Class parameterType) {
        String setterSignature = "(L" + parameterType.getName().replace(".", "/") + ";)V";
        List<MethodInfo> methodInfos = new ArrayList<>();
        for (MethodInfo methodInfo : classInfo.methodsInfo().methods()) {
            if (methodInfo.getDescriptor().equals(setterSignature)) {
                methodInfos.add(methodInfo);
            }
        }
        return methodInfos;
    }

    /**
     * Find all FieldInfos for the specified ClassInfo whose type matches the supplied fieldType
     *
     * @param classInfo The ClassInfo instance whose FieldInfos will be examined
     * @param fieldType The fieldType to look for
     * @return
     */
    public List<FieldInfo> findFields(ClassInfo classInfo, Class fieldType) {
        String fieldSignature = "L" + fieldType.getName().replace(".", "/") + ";";
        List<FieldInfo> fieldInfos = new ArrayList<>();
        for (FieldInfo fieldInfo : classInfo.fieldsInfo().fields() ) {
            if (fieldInfo.getDescriptor().equals(fieldSignature)) {
                fieldInfos.add(fieldInfo);
            }
        }
        return fieldInfos;
    }

    /**
     *
     * @param classInfo
     */
    public List<FieldInfo> findIterableFields(ClassInfo classInfo) {
        List<FieldInfo> fieldInfos = new ArrayList<>();
        try {
            for (FieldInfo fieldInfo : classInfo.fieldsInfo().fields() ) {
                Class type = getField(classInfo, fieldInfo).getType();
                if (type.isArray() || Collection.class.isAssignableFrom(type)) {
                    fieldInfos.add(fieldInfo);
                }
            }
            return fieldInfos;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *
     * @param classInfo
     */
    public List<MethodInfo> findIterableMethods(ClassInfo classInfo, Class parameterType) {
        List<MethodInfo> methodInfos = new ArrayList<>();
        try {
            Class clazz = Class.forName(classInfo.name());
            for (Method method : clazz.getDeclaredMethods()) {
                MethodInfo methodInfo = classInfo.methodsInfo().get(method.getName());
                if (methodInfo != null) {
                    if (methodInfo.getDescriptor().endsWith(")V")) {
                        if (method.getParameterTypes().length == 1) {
                            Class methodParameterType = method.getParameterTypes()[0];
                            if (methodParameterType.isArray() && methodParameterType.getComponentType() == parameterType) {
                                methodInfos.add(methodInfo);
                            }
                            else if (Collection.class.isAssignableFrom(methodParameterType)) {
                                ParameterizedType parameterizedType = (ParameterizedType) method.getGenericParameterTypes()[0];
                                Class<?> parameterizedTypeClass = (Class<?>) parameterizedType.getActualTypeArguments()[0];
                                if (parameterizedTypeClass == parameterType) {
                                    methodInfos.add(methodInfo);
                                }
                            }
                        }
                    }
                }
            }
            return methodInfos;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}

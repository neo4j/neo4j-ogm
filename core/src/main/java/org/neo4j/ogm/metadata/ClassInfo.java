/*
 * Copyright (c) 2002-2017 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 *  conditions of the subcomponent's license, as noted in the LICENSE file.
 */

package org.neo4j.ogm.metadata;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.neo4j.ogm.annotation.*;
import org.neo4j.ogm.exception.MappingException;
import org.neo4j.ogm.exception.MetadataException;
import org.neo4j.ogm.id.IdStrategy;
import org.neo4j.ogm.id.InternalIdStrategy;
import org.neo4j.ogm.id.UuidStrategy;
import org.neo4j.ogm.session.Neo4jException;
import org.neo4j.ogm.utils.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Maintains object to graph mapping details at the class (type) level
 * The ClassInfo object is used to maintain mappings from Java Types-&gt;Neo4j Labels
 * thereby allowing the correct labels to be applied to new nodes when they
 * are persisted.
 * The ClassInfo object also maintains a map of FieldInfo and MethodInfo objects
 * that maintain the appropriate information for mapping Java class attributes to Neo4j
 * node properties / paths (node)-[:relationship]-&gt;(node), via field or method
 * accessors respectively.
 * Given a type hierarchy, the ClassInfo object guarantees that for any type in that
 * hierarchy, the labels associated with that type will include the labels for
 * all its superclass and interface types as well. This is to avoid the need to iterate
 * through the ClassInfo hierarchy to recover label information.
 *
 * @author Vince Bickers
 * @author Luanne Misquitta
 * @author Mark Angrish
 */
public class ClassInfo {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassInfo.class);

    private final List<ClassInfo> directSubclasses = new ArrayList<>();
    private final List<ClassInfo> directInterfaces = new ArrayList<>();
    private final List<ClassInfo> directImplementingClasses = new ArrayList<>();
    private String className;
    private String directSuperclassName;
    private String neo4jName;
    private boolean isInterface;
    private boolean isAbstract;
    private boolean isEnum;
    private boolean hydrated;
    private FieldsInfo fieldsInfo;
    private MethodsInfo methodsInfo;
    private AnnotationsInfo annotationsInfo;
    private InterfacesInfo interfacesInfo;
    private ClassInfo directSuperclass;
    private Map<Class, List<FieldInfo>> iterableFieldsForType = new HashMap<>();
    private Map<FieldInfo, Field> fieldInfoFields = new ConcurrentHashMap<>();
    private volatile Set<FieldInfo> fieldInfos;
    private volatile Map<String, FieldInfo> propertyFields;
    private volatile Map<String, FieldInfo> indexFields;
    private volatile LazyInstance<FieldInfo> identityField;
    private volatile FieldInfo primaryIndexField = null;
    private volatile FieldInfo labelField = null;
    private volatile boolean labelFieldMapped = false;
    private volatile boolean isPostLoadMethodMapped = false;
    private volatile MethodInfo postLoadMethod;
    private boolean primaryIndexFieldChecked = false;
    private Class<?> cls;
    private Class<? extends IdStrategy> idStrategyClass;
    private IdStrategy idStrategy;

    /**
     * This class was referenced as a superclass of the given subclass.
     *
     * @param name the name of the class
     * @param subclass {@link ClassInfo} of the subclass
     */
    ClassInfo(String name, ClassInfo subclass) {
        this.className = name;
        this.hydrated = false;
        this.fieldsInfo = new FieldsInfo();
        this.methodsInfo = new MethodsInfo();
        this.annotationsInfo = new AnnotationsInfo();
        this.interfacesInfo = new InterfacesInfo();
        addSubclass(subclass);
    }

    public ClassInfo(Class<?> cls) {
        this.cls = cls;
        final int modifiers = cls.getModifiers();
        this.isInterface = Modifier.isInterface(modifiers);
        this.isAbstract = Modifier.isAbstract(modifiers);
        this.isEnum = cls.isEnum();
        this.className = cls.getName();

        if (cls.getSuperclass() != null) {
            this.directSuperclassName = cls.getSuperclass().getName();
        }
        this.interfacesInfo = new InterfacesInfo(cls);
        this.fieldsInfo = new FieldsInfo(this, cls);
        this.methodsInfo = new MethodsInfo(cls);
        this.annotationsInfo = new AnnotationsInfo(cls);

        if (isRelationshipEntity() && labelFieldOrNull() != null) {
            throw new MappingException(String.format("'%s' is a relationship entity. The @Labels annotation can't be applied to " +
                    "relationship entities.", name()));
        }

        for (FieldInfo fieldInfo : fieldsInfo().fields()) {
            if (fieldInfo.hasAnnotation(Property.class) && fieldInfo.hasCompositeConverter()) {
                throw new MappingException(String.format("'%s' has both @Convert and @Property annotations applied to the field '%s'",
                        name(), fieldInfo.getName()));
            }
        }
    }

    /**
     * A class that was previously only seen as a temp superclass of another class can now be fully hydrated.
     *
     * @param classInfoDetails ClassInfo details
     */
    void hydrate(ClassInfo classInfoDetails) {

        if (!this.hydrated) {
            this.hydrated = true;

            this.isAbstract = classInfoDetails.isAbstract;
            this.isInterface = classInfoDetails.isInterface;
            this.isEnum = classInfoDetails.isEnum;
            this.directSuperclassName = classInfoDetails.directSuperclassName;
            this.cls = classInfoDetails.cls;

            //this.interfaces.addAll(classInfoDetails.interfaces());

            this.interfacesInfo.append(classInfoDetails.interfacesInfo());

            this.annotationsInfo.append(classInfoDetails.annotationsInfo());
            this.fieldsInfo.append(classInfoDetails.fieldsInfo());
            this.methodsInfo.append(classInfoDetails.methodsInfo());
        }
    }

    void extend(ClassInfo classInfo) {
        this.interfacesInfo.append(classInfo.interfacesInfo());
        this.fieldsInfo.append(classInfo.fieldsInfo());
        this.methodsInfo.append(classInfo.methodsInfo());
    }

    /**
     * Connect this class to a subclass.
     *
     * @param subclass the subclass
     */
    void addSubclass(ClassInfo subclass) {
        if (subclass.directSuperclass != null && subclass.directSuperclass != this) {
            throw new RuntimeException(subclass.className + " has two superclasses: " + subclass.directSuperclass.className + ", " + this.className);
        }
        subclass.directSuperclass = this;
        this.directSubclasses.add(subclass);
    }

    boolean hydrated() {
        return hydrated;
    }

    public String name() {
        return className;
    }

    String simpleName() {
        return className.substring(className.lastIndexOf('.') + 1);
    }

    public ClassInfo directSuperclass() {
        return directSuperclass;
    }

    /**
     * <p>
     * Retrieves the static labels that are applied to nodes in the database. If the class' instances are persisted by
     * a relationship instead of a node then this method returns an empty collection.
     * </p>
     * <p>
     * Note that this method returns only the static labels. A node entity instance may declare additional labels
     * manged at runtime by using the @Labels annotation on a collection field, therefore the full set of labels to be
     * mapped to a node will be the static labels, in addition to any labels declared by the backing field of an
     * {@link Labels} annotation.
     * </p>
     *
     * @return A {@link Collection} of all the static labels that apply to the node or an empty list if there aren't
     * any, never <code>null</code>
     */
    public Collection<String> staticLabels() {
        return collectLabels(new ArrayList<>());
    }

    public String neo4jName() {
        if (neo4jName == null) {
            AnnotationInfo annotationInfo = annotationsInfo.get(NodeEntity.class);
            if (annotationInfo != null) {
                neo4jName = annotationInfo.get(NodeEntity.LABEL, simpleName());
                return neo4jName;
            }
            annotationInfo = annotationsInfo.get(RelationshipEntity.class);
            if (annotationInfo != null) {
                neo4jName = annotationInfo.get(RelationshipEntity.TYPE, simpleName().toUpperCase());
                return neo4jName;
            }
            neo4jName = simpleName();
        }
        return neo4jName;
    }

    private Collection<String> collectLabels(Collection<String> labelNames) {
        if (!isAbstract || annotationsInfo.get(NodeEntity.class) != null) {
            labelNames.add(neo4jName());
        }
        if (directSuperclass != null && !"java.lang.Object".equals(directSuperclass.className)) {
            directSuperclass.collectLabels(labelNames);
        }
        for (ClassInfo interfaceInfo : directInterfaces()) {
            interfaceInfo.collectLabels(labelNames);
        }
        return labelNames;
    }

    List<ClassInfo> directSubclasses() {
        return directSubclasses;
    }

    List<ClassInfo> directImplementingClasses() {
        return directImplementingClasses;
    }

    List<ClassInfo> directInterfaces() {
        return directInterfaces;
    }

    InterfacesInfo interfacesInfo() {
        return interfacesInfo;
    }

    public Collection<AnnotationInfo> annotations() {
        return annotationsInfo.list();
    }

    public boolean isInterface() {
        return isInterface;
    }

    public boolean isEnum() {
        return isEnum;
    }

    public AnnotationsInfo annotationsInfo() {
        return annotationsInfo;
    }

    String superclassName() {
        return directSuperclassName;
    }

    public FieldsInfo fieldsInfo() {
        return fieldsInfo;
    }

    MethodsInfo methodsInfo() {
        return methodsInfo;
    }


    public FieldInfo identityFieldOrNull() {
        initIdentityField();
        return identityField.get();
    }

    /**
     * The identity field is a field annotated with @NodeId, or if none exists, a field
     * of type Long called 'id'
     *
     * @return A {@link FieldInfo} object representing the identity field never <code>null</code>
     * @throws MappingException if no identity field can be found
     */
    public FieldInfo identityField() {
        initIdentityField();
        FieldInfo field = identityField.get();
        if (field == null) {
            throw new MetadataException("No internal identity field found for class: " + this.className);
        }
        return field;
    }

    private void initIdentityField() {
        if (identityField == null) {
            identityField = new LazyInstance<>(() -> {
                Collection<FieldInfo> identityFields = getFieldInfos(this::isInternalIdentity);
                if (identityFields.size() == 1) {
                    return identityFields.iterator().next();
                }
                if (identityFields.size() > 1) {
                    throw new MetadataException("Expected exactly one internal identity field (@GraphId or @Id with " +
                            "InternalIdStrategy), found " + identityFields.size() + " " + identityFields);
                }
                FieldInfo fieldInfo = fieldsInfo().get("id");
                if (fieldInfo != null) {
                    if (fieldInfo.getTypeDescriptor().equals("java.lang.Long")) {
                        return fieldInfo;
                    }
                }
                return null;
            });
        }
    }

    public boolean hasIdentityField() {
        initIdentityField();
        return identityField.exists();
    }

    // Identity field
    private boolean isInternalIdentity(FieldInfo fieldInfo) {
        return fieldInfo.getAnnotations().has(GraphId.class) ||
                (fieldInfo.getAnnotations().has(Id.class) &&
                        fieldInfo.getAnnotations().has(GeneratedValue.class) &&
                        ((GeneratedValue) fieldInfo.getAnnotations().get(GeneratedValue.class).getAnnotation())
                                .strategy().equals(InternalIdStrategy.class)
                );
    }

    Collection<FieldInfo> getFieldInfos(Predicate<FieldInfo> predicate) {
        return fieldsInfo().fields().stream()
                .filter(predicate)
                .collect(Collectors.toSet());
    }

    /**
     * The label field is an optional field annotated with @Labels.
     *
     * @return A {@link FieldInfo} object representing the label field. Optionally <code>null</code>
     */
    public FieldInfo labelFieldOrNull() {
        if (labelFieldMapped) {
            return labelField;
        }
        if (!labelFieldMapped) {
            for (FieldInfo fieldInfo : fieldsInfo().fields()) {
                if (fieldInfo.isLabelField()) {
                    if (!fieldInfo.isIterable()) {
                        throw new MappingException(String.format(
                                "Field '%s' in class '%s' includes the @Labels annotation, however this field is not a " +
                                        "type of collection.", fieldInfo.getName(), this.name()));
                    }
                    labelFieldMapped = true;
                    labelField = fieldInfo;
                    return labelField;
                }
            }
            labelFieldMapped = true;
        }
        return null;
    }

    public boolean isRelationshipEntity() {
        for (AnnotationInfo info : annotations()) {
            if (info.getName().equals(RelationshipEntity.class.getName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * A property field is any field annotated with @Property, or any field that can be mapped to a
     * node property. The identity field is not a property field.
     *
     * @return A Collection of FieldInfo objects describing the classInfo's property fields
     */
    public Collection<FieldInfo> propertyFields() {
        if (fieldInfos == null) {
            initPropertyFields();
        }
        return fieldInfos;
    }

    /**
     * Finds the property field with a specific property name from the ClassInfo's property fields
     * Note that this method does not allow for property names with differing case. //TODO
     *
     * @param propertyName the propertyName of the field to find
     * @return A FieldInfo object describing the required property field, or null if it doesn't exist.
     */
    public FieldInfo propertyField(String propertyName) {
        if (propertyFields == null) {
            initPropertyFields();
        }
        return propertyFields.get(propertyName.toLowerCase());
    }

    private synchronized void initPropertyFields() {
        if (fieldInfos == null) {
            Collection<FieldInfo> fields = fieldsInfo().fields();

            FieldInfo identityField = identityFieldOrNull();
            Set<FieldInfo> fieldInfos = new HashSet<>(fields.size());
            Map<String, FieldInfo> propertyFields = new HashMap<>(fields.size());

            for (FieldInfo fieldInfo : fields) {
                if (fieldInfo != identityField && !fieldInfo.isLabelField()
                    && !fieldInfo.hasAnnotation(StartNode.class)
                    && !fieldInfo.hasAnnotation(EndNode.class)) {
                    AnnotationInfo annotationInfo = fieldInfo.getAnnotations().get(Property.class);
                    if (annotationInfo == null) {
                        if (fieldInfo.persistableAsProperty()) {
                            fieldInfos.add(fieldInfo);
                            propertyFields.put(fieldInfo.property().toLowerCase(), fieldInfo);
                        }
                    } else {
                        fieldInfos.add(fieldInfo);
                        propertyFields.put(fieldInfo.property().toLowerCase(), fieldInfo);
                    }
                }
            }
            this.fieldInfos = fieldInfos;
            this.propertyFields = propertyFields;
        }
    }

    /**
     * Finds the property field with a specific field name from the ClassInfo's property fields
     *
     * @param propertyName the propertyName of the field to find
     * @return A FieldInfo object describing the required property field, or null if it doesn't exist.
     */
    public FieldInfo propertyFieldByName(String propertyName) {
        for (FieldInfo fieldInfo : propertyFields()) {
            if (fieldInfo.getName().equalsIgnoreCase(propertyName)) {
                return fieldInfo;
            }
        }
        return null;
    }

    /**
     * A relationship field is any field annotated with @Relationship, or any field that cannot be mapped to a
     * node property. The identity field is not a relationship field.
     *
     * @return A Collection of FieldInfo objects describing the classInfo's relationship fields
     */
    public Collection<FieldInfo> relationshipFields() {
        FieldInfo identityField = identityFieldOrNull();
        Set<FieldInfo> fieldInfos = new HashSet<>();
        for (FieldInfo fieldInfo : fieldsInfo().fields()) {
            if (fieldInfo != identityField) {
                AnnotationInfo annotationInfo = fieldInfo.getAnnotations().get(Relationship.class);
                if (annotationInfo == null) {
                    if (!fieldInfo.persistableAsProperty()) {
                        fieldInfos.add(fieldInfo);
                    }
                } else {
                    fieldInfos.add(fieldInfo);
                }
            }
        }
        return fieldInfos;
    }

    /**
     * Finds the relationship field with a specific name from the ClassInfo's relationship fields
     *
     * @param relationshipName the relationshipName of the field to find
     * @return A FieldInfo object describing the required relationship field, or null if it doesn't exist.
     */
    public FieldInfo relationshipField(String relationshipName) {
        for (FieldInfo fieldInfo : relationshipFields()) {
            if (fieldInfo.relationship().equalsIgnoreCase(relationshipName)) {
                return fieldInfo;
            }
        }
        return null;
    }

    /**
     * Finds the relationship field with a specific name and direction from the ClassInfo's relationship fields
     *
     * @param relationshipName the relationshipName of the field to find
     * @param relationshipDirection the direction of the relationship
     * @param strict if true, does not infer relationship type but looks for it in the @Relationship annotation. Null if missing. If false, infers relationship type from FieldInfo
     * @return A FieldInfo object describing the required relationship field, or null if it doesn't exist.
     */
    public FieldInfo relationshipField(String relationshipName, String relationshipDirection, boolean strict) {
        for (FieldInfo fieldInfo : relationshipFields()) {
            String relationship = strict ? fieldInfo.relationshipTypeAnnotation() : fieldInfo.relationship();
            if (relationshipName.equalsIgnoreCase(relationship)) {
                if (((fieldInfo.relationshipDirection(Relationship.OUTGOING).equals(Relationship.INCOMING) || fieldInfo.relationshipDirection(Relationship.OUTGOING).equals(Relationship.UNDIRECTED)) && (relationshipDirection.equals(Relationship.INCOMING)))
                        || (relationshipDirection.equals(Relationship.OUTGOING) && !(fieldInfo.relationshipDirection(Relationship.OUTGOING).equals(Relationship.INCOMING)))) {
                    return fieldInfo;
                }
            }
        }
        return null;
    }

    /**
     * Finds all relationship fields with a specific name and direction from the ClassInfo's relationship fields
     *
     * @param relationshipName the relationshipName of the field to find
     * @param relationshipDirection the direction of the relationship
     * @param strict if true, does not infer relationship type but looks for it in the @Relationship annotation. Null if missing. If false, infers relationship type from FieldInfo
     * @return Set of  FieldInfo objects describing the required relationship field, or empty set if it doesn't exist.
     */
    public Set<FieldInfo> candidateRelationshipFields(String relationshipName, String relationshipDirection, boolean strict) {
        Set<FieldInfo> candidateFields = new HashSet<>();
        for (FieldInfo fieldInfo : relationshipFields()) {
            String relationship = strict ? fieldInfo.relationshipTypeAnnotation() : fieldInfo.relationship();
            if (relationshipName.equalsIgnoreCase(relationship)) {
                if (((fieldInfo.relationshipDirection(Relationship.OUTGOING).equals(Relationship.INCOMING) || fieldInfo.relationshipDirection(Relationship.OUTGOING).equals(Relationship.UNDIRECTED)) && (relationshipDirection.equals(Relationship.INCOMING)))
                        || (relationshipDirection.equals(Relationship.OUTGOING) && !(fieldInfo.relationshipDirection(Relationship.OUTGOING).equals(Relationship.INCOMING)))) {
                    candidateFields.add(fieldInfo);
                }
            }
        }
        return candidateFields;
    }

    /**
     * Finds the relationship field with a specific property name from the ClassInfo's relationship fields
     *
     * @param fieldName the name of the field
     * @return A FieldInfo object describing the required relationship field, or null if it doesn't exist.
     */
    public FieldInfo relationshipFieldByName(String fieldName) {
        for (FieldInfo fieldInfo : relationshipFields()) {
            if (fieldInfo.getName().equalsIgnoreCase(fieldName)) {
                return fieldInfo;
            }
        }
        return null;
    }

    public Field getField(FieldInfo fieldInfo) {
        Field field = fieldInfoFields.get(fieldInfo);
        if (field != null) {
            return field;
        }
        try {
            field = cls.getDeclaredField(fieldInfo.getName());
            fieldInfoFields.put(fieldInfo, field);
            return field;
        } catch (NoSuchFieldException e) {
            if (directSuperclass() != null) {
                field = directSuperclass().getField(fieldInfo);
                fieldInfoFields.put(fieldInfo, field);
                return field;
            } else {
                throw new RuntimeException("Field " + fieldInfo.getName() + " not found in class " + name() + " or any of its superclasses");
            }
        }
    }

    /**
     * Returns the Method corresponding to the supplied MethodInfo as declared by the class represented by this ClassInfo
     *
     * @param methodInfo the MethodInfo used to obtain the Method
     * @return a Method
     */
    public Method getMethod(MethodInfo methodInfo) {
        return methodInfo.getMethod();
    }

    /**
     * Find all FieldInfos for the specified ClassInfo whose type matches the supplied fieldType
     *
     * @param fieldType The field type to look for
     * @return A {@link List} of {@link FieldInfo} objects that are of the given type, never <code>null</code>
     */
    public List<FieldInfo> findFields(Class<?> fieldType) {
        String fieldSignature = fieldType.getName();
        List<FieldInfo> fieldInfos = new ArrayList<>();
        for (FieldInfo fieldInfo : fieldsInfo().fields()) {
            if (fieldInfo.getTypeDescriptor().equals(fieldSignature)) {
                fieldInfos.add(fieldInfo);
            }
        }
        return fieldInfos;
    }

    /**
     * Find all FieldInfos for the specified ClassInfo which have the specified annotation
     *
     * @param annotation The annotation
     * @return A {@link List} of {@link FieldInfo} objects that are of the given type, never <code>null</code>
     */
    public List<FieldInfo> findFields(String annotation) {
        List<FieldInfo> fieldInfos = new ArrayList<>();
        for (FieldInfo fieldInfo : fieldsInfo().fields()) {
            if (fieldInfo.hasAnnotation(annotation)) {
                fieldInfos.add(fieldInfo);
            }
        }
        return fieldInfos;
    }

    /**
     * Retrieves a {@link List} of {@link FieldInfo} representing all of the fields that can be iterated over
     * using a "foreach" loop.
     *
     * @return {@link List} of {@link FieldInfo}
     */
    public List<FieldInfo> findIterableFields() {
        List<FieldInfo> fieldInfos = new ArrayList<>();
        try {
            for (FieldInfo fieldInfo : fieldsInfo().fields()) {
                Class type = getField(fieldInfo).getType();
                if (type.isArray() || Iterable.class.isAssignableFrom(type)) {
                    fieldInfos.add(fieldInfo);
                }
            }
            return fieldInfos;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Finds all fields whose type is equivalent to Array&lt;X&gt; or assignable from Iterable&lt;X&gt;
     * where X is the generic parameter type of the Array or Iterable
     *
     * @param iteratedType the type of iterable
     * @return {@link List} of {@link MethodInfo}, never <code>null</code>
     */
    public List<FieldInfo> findIterableFields(Class iteratedType) {
        if (iterableFieldsForType.containsKey(iteratedType)) {
            return iterableFieldsForType.get(iteratedType);
        }
        List<FieldInfo> fieldInfos = new ArrayList<>();
        String typeSignature = iteratedType.getName();
        String arrayOfTypeSignature = typeSignature + "[]";
        try {
            for (FieldInfo fieldInfo : fieldsInfo().fields()) {
                String fieldType = fieldInfo.getTypeDescriptor();
                if (fieldInfo.isArray() && (fieldType.equals(arrayOfTypeSignature) || fieldInfo.isParameterisedTypeOf(iteratedType))) {
                    fieldInfos.add(fieldInfo);
                } else if (fieldInfo.isIterable() && (fieldType.equals(typeSignature) || fieldInfo.isParameterisedTypeOf(iteratedType))) {
                    fieldInfos.add(fieldInfo);
                }
            }
            iterableFieldsForType.put(iteratedType, fieldInfos);
            return fieldInfos;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Finds all fields whose type is equivalent to Array&lt;X&gt; or assignable from Iterable&lt;X&gt;
     * where X is the generic parameter type of the Array or Iterable and the relationship type backing this iterable is "relationshipType"
     *
     * @param iteratedType the type of iterable
     * @param relationshipType the relationship type
     * @param relationshipDirection the relationship direction
     * @param strict if true, does not infer relationship type but looks for it in the @Relationship annotation. Null if missing. If false, infers relationship type from FieldInfo
     * @return {@link List} of {@link MethodInfo}, never <code>null</code>
     */
    public List<FieldInfo> findIterableFields(Class iteratedType, String relationshipType, String relationshipDirection, boolean strict) {
        List<FieldInfo> fieldInfos = new ArrayList<>();
        for (FieldInfo fieldInfo : findIterableFields(iteratedType)) {
            String relationship = strict ? fieldInfo.relationshipTypeAnnotation() : fieldInfo.relationship();
            if (relationshipType.equals(relationship)) {
                if (((fieldInfo.relationshipDirection(Relationship.OUTGOING).equals(Relationship.INCOMING) || fieldInfo.relationshipDirection(Relationship.OUTGOING).equals(Relationship.UNDIRECTED)) && relationshipDirection.equals(Relationship.INCOMING))
                        || (relationshipDirection.equals(Relationship.OUTGOING) && !(fieldInfo.relationshipDirection(Relationship.OUTGOING).equals(Relationship.INCOMING)))) {
                    fieldInfos.add(fieldInfo);
                }
            }
        }
        return fieldInfos;
    }

    public boolean isTransient() {
        return annotationsInfo.get(Transient.class) != null;
    }

    public boolean isAbstract() {
        return isAbstract;
    }

    /**
     * Returns true if this classInfo is in the subclass hierarchy of b, or if this classInfo is the same as b, false otherwise
     *
     * @param classInfo the classInfo at the toplevel of a type hierarchy to search through
     * @return true if this classInfo is in the subclass hierarchy of classInfo, false otherwise
     */
    boolean isSubclassOf(ClassInfo classInfo) {

        if (this == classInfo) {
            return true;
        }

        boolean found = false;

        for (ClassInfo subclass : classInfo.directSubclasses()) {
            found = isSubclassOf(subclass);
            if (found) {
                break;
            }
        }

        return found;
    }

    /**
     * Get the underlying class represented by this ClassInfo
     *
     * @return the underlying class or null if it cannot be determined
     */
    public Class<?> getUnderlyingClass() {
        return cls;
    }

    /**
     * Gets the class of the type parameter description of the entity related to this.
     * The match is done based on the following-
     * 2. Look for a field explicitly annotated with @Relationship for a type and implied direction
     * 4. Look for a field with name derived from the relationship type for the given direction
     *
     * @param relationshipType the relationship type
     * @param relationshipDirection the relationship direction
     * @return class of the type parameter descriptor or null if it could not be determined
     */
    Class getTypeParameterDescriptorForRelationship(String relationshipType, String relationshipDirection) {
        final boolean STRICT_MODE = true; //strict mode for matching methods and fields, will only look for explicit annotations
        final boolean INFERRED_MODE = false; //inferred mode for matching methods and fields, will infer the relationship type from the getter/setter/property

        try {
            FieldInfo fieldInfo = relationshipField(relationshipType, relationshipDirection, STRICT_MODE);
            if (fieldInfo != null && fieldInfo.getTypeDescriptor() != null) {
                return ClassUtils.getType(fieldInfo.getTypeDescriptor());
            }

            if (!relationshipDirection.equals(Relationship.INCOMING)) { //we always expect an annotation for INCOMING
                fieldInfo = relationshipField(relationshipType, relationshipDirection, INFERRED_MODE);
                if (fieldInfo != null && fieldInfo.getTypeDescriptor() != null) {
                    return ClassUtils.getType(fieldInfo.getTypeDescriptor());
                }
            }
        } catch (RuntimeException e) {
            LOGGER.debug("Could not get {} class type for relationshipType {} and relationshipDirection {} ", className, relationshipType, relationshipDirection);
        }
        return null;
    }

    /**
     * @return If this class contains any fields/properties annotated with @Index.
     */
    public boolean containsIndexes() {
        return !getIndexFields().isEmpty();
    }

    /**
     * @return The <code>FieldInfo</code>s representing the Indexed fields in this class.
     */
    public Collection<FieldInfo> getIndexFields() {
        if (indexFields == null) {
            indexFields = addIndexes();
        }
        return indexFields.values();
    }

    private Map<String, FieldInfo> addIndexes() {
        Map<String, FieldInfo> indexes = new HashMap<>();

        // No way to get declared fields from current byte code impl. Using reflection instead.
        Field[] declaredFields;
        try {
            declaredFields = Class.forName(className, false, Thread.currentThread().getContextClassLoader()).getDeclaredFields();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Could not reflectively read declared fields", e);
        }

        for (FieldInfo fieldInfo : fieldsInfo().fields()) {
            if (isDeclaredField(declaredFields, fieldInfo.getName()) &&
                    (fieldInfo.hasAnnotation(Index.class) || fieldInfo.hasAnnotation(Id.class))) {

                String propertyValue = fieldInfo.property();
                if (fieldInfo.hasAnnotation(Property.class.getName())) {
                    propertyValue = fieldInfo.property();
                }
                indexes.put(propertyValue, fieldInfo);
            }
        }
        return indexes;
    }

    private static boolean isDeclaredField(Field[] declaredFields, String name) {

        for (Field field : declaredFields) {
            if (field.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }


    public FieldInfo primaryIndexField() {
        if (!primaryIndexFieldChecked && primaryIndexField == null) {

            Collection<FieldInfo> primaryIndexFields = getFieldInfos(this::isPrimaryIndexField);
            if (primaryIndexFields.size() > 1) {
                throw new MetadataException("Only one @Id / @Index(primary=true, unique=true) annotation is allowed in a class hierarchy. Please check annotations in the class " + name() + " or its parents");
            } else if (primaryIndexFields.size() == 1) {
                primaryIndexField = primaryIndexFields.iterator().next();
                AnnotationInfo generatedValueAnnotation = primaryIndexField.getAnnotations().get(GeneratedValue.class);
                if (generatedValueAnnotation != null) {
                    GeneratedValue value = (GeneratedValue) generatedValueAnnotation.getAnnotation();
                    idStrategyClass = value.strategy();
                    instantiateIdStrategy();
                }
            }
            validateIdGenerationConfig();
            primaryIndexFieldChecked = true;
        }

        return primaryIndexField;
    }

    public boolean hasPrimaryIndexField() {
        if (!primaryIndexFieldChecked) {
            primaryIndexField();
        }
        return primaryIndexField != null;
    }

    private boolean isPrimaryIndexField(FieldInfo fieldInfo) {
        // primary index field is either
        // field with @Id or @Id @GeneratedValue(strategy=..) where strategy != InternalIdStrategy
        return (fieldInfo.getAnnotations().has(Id.class) &&
                !(fieldInfo.getAnnotations().has(GeneratedValue.class) &&
                        ((GeneratedValue) fieldInfo.getAnnotations().get(GeneratedValue.class).getAnnotation()).strategy().equals(InternalIdStrategy.class)

                )) ||
                // or @Index(primary=true) - backward compatibility
                fieldInfo.getAnnotations().has(Index.class) &&
                        ((Index) fieldInfo.getAnnotations().get(Index.class).getAnnotation()).primary();
    }

    private void instantiateIdStrategy() {
        try {
            idStrategy = idStrategyClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            LOGGER.debug("Could not instantiate {}. Expecting this to be registered manually.", idStrategyClass);
        }
    }

    private void validateIdGenerationConfig() {
        fieldsInfo().fields().forEach(info -> {
            if (info.hasAnnotation(GeneratedValue.class) && !info.hasAnnotation(Id.class)) {
                throw new MetadataException("The type of @Generated field in class " + className + " must be also annotated with @Id.");
            }
        });
        if (UuidStrategy.class.equals(idStrategyClass)
                && !primaryIndexField.isTypeOf(UUID.class)
                && !primaryIndexField.isTypeOf(String.class)) {
            throw new MetadataException("The type of " + primaryIndexField.getName() + " in class " + className + " must be of type java.lang.UUID or java.lang.String because it has an UUID generation strategy.");
        }
    }

    public IdStrategy idStrategy() {
        if (!primaryIndexFieldChecked) {
            primaryIndexField(); // force init
        }
        return idStrategy;
    }

    public Class<? extends IdStrategy> idStrategyClass() {
        return idStrategyClass;
    }

    public void registerIdGenerationStrategy(IdStrategy strategy) {
        if (strategy.getClass().equals(idStrategyClass)) {
            idStrategy = strategy;
        } else {
            throw new IllegalArgumentException("Strategy " + strategy +
                    " is not an instance of " + idStrategyClass);
        }
    }

    public MethodInfo postLoadMethodOrNull() {
        if (isPostLoadMethodMapped) {
            return postLoadMethod;
        }
        if (!isPostLoadMethodMapped) {
            for (MethodInfo methodInfo : methodsInfo().methods()) {
                if (methodInfo.hasAnnotation(PostLoad.class.getName())) {
                    isPostLoadMethodMapped = true;
                    postLoadMethod = methodInfo;
                    return postLoadMethod;
                }
            }
            isPostLoadMethodMapped = true;
        }
        return null;
    }

    public FieldInfo getFieldInfo(String propertyName) {

        // fall back to the field if method cannot be found
        FieldInfo labelField = labelFieldOrNull();
        if (labelField != null && labelField.getName().equals(propertyName)) {
            return labelField;
        }
        FieldInfo propertyField = propertyField(propertyName);
        if (propertyField != null) {
            return propertyField;
        }
        return null;
    }

    /**
     * Return a FieldInfo for the EndNode of a RelationshipEntity
     *
     * @return a FieldInfo for the field annotated as the EndNode, or none if not found
     */
    public FieldInfo getEndNodeReader() {
        if (isRelationshipEntity()) {
            for (FieldInfo fieldInfo : fieldsInfo().fields()) {
                if (fieldInfo.getAnnotations().get(EndNode.class) != null) {
                    return fieldInfo;
                }
            }
            LOGGER.warn("Failed to find an @EndNode on {}", name());
        }

        return null;
    }

    /**
     * Return a FieldInfo for the StartNode of a RelationshipEntity
     *
     * @return a FieldInfo for the field annotated as the StartNode, or none if not found
     */
    public FieldInfo getStartNodeReader() {
        if (isRelationshipEntity()) {

            for (FieldInfo fieldInfo : fieldsInfo().fields()) {
                if (fieldInfo.getAnnotations().get(StartNode.class) != null) {
                    return fieldInfo;
                }
            }
            LOGGER.warn("Failed to find an @StartNode on {}", name());
        }
        return null;
    }
}


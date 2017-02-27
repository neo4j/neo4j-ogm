/*
 * Copyright (c) 2002-2016 "Neo Technology,"
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

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.neo4j.ogm.annotation.*;
import org.neo4j.ogm.classloader.MetaDataClassLoader;
import org.neo4j.ogm.exception.MappingException;
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
public class ClassMetadata {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassMetadata.class);
    private final List<ClassMetadata> directSubclasses = new ArrayList<>();
    private final List<ClassMetadata> directInterfaces = new ArrayList<>();
    private final List<ClassMetadata> directImplementingClasses = new ArrayList<>();
    /**
     * ISSUE-180: synchronized can be used instead of this lock but right now this mechanism is here to see if
     * ConcurrentModificationException stops occurring.
     */
    private final Lock lock = new ReentrantLock();
    private String className;
    private String directSuperclassName;
    private String neo4jName;
    private boolean isInterface;
    private boolean isAbstract;
    private boolean isEnum;
    private boolean hydrated;
    private FieldsInfo fieldsInfo = new FieldsInfo();
    private MethodsInfo methodsInfo = new MethodsInfo();
    private AnnotationsInfo annotationsInfo = new AnnotationsInfo();
    private InterfacesInfo interfacesInfo = new InterfacesInfo();
    private ClassMetadata directSuperclass;
    private Map<Class, List<FieldMetadata>> iterableFieldsForType = new HashMap<>();
    private Map<FieldMetadata, Field> fieldInfoFields = new ConcurrentHashMap<>();
    private volatile Set<FieldMetadata> fieldInfos;
    private volatile Map<String, FieldMetadata> propertyFields;
    private volatile Map<String, FieldMetadata> indexFields;
    private volatile FieldMetadata identityField = null;
    private volatile FieldMetadata primaryIndexField = null;
    private volatile FieldMetadata labelField = null;
    private volatile boolean labelFieldMapped = false;
    private boolean primaryIndexFieldChecked = false;

    // todo move this to a factory class
    public ClassMetadata(InputStream inputStream) throws IOException {

        DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(inputStream, 1024));

        // Magic
        if (dataInputStream.readInt() != 0xCAFEBABE) {
            return;
        }

        dataInputStream.readUnsignedShort();    //minor version
        dataInputStream.readUnsignedShort();    // major version

        ConstantPool constantPool = new ConstantPool(dataInputStream);

        // Access flags
        int flags = dataInputStream.readUnsignedShort();

        isInterface = (flags & 0x0200) != 0;
        isAbstract = (flags & 0x0400) != 0;
        isEnum = (flags & 0x4000) != 0;

        className = constantPool.lookup(dataInputStream.readUnsignedShort()).replace('/', '.');
        String sce = constantPool.lookup(dataInputStream.readUnsignedShort());
        if (sce != null) {
            directSuperclassName = sce.replace('/', '.');
        }
        interfacesInfo = new InterfacesInfo(dataInputStream, constantPool);
        fieldsInfo = new FieldsInfo(dataInputStream, constantPool);
        methodsInfo = new MethodsInfo(className, dataInputStream, constantPool);
        annotationsInfo = new AnnotationsInfo(dataInputStream, constantPool);
        new ClassValidator(this).validate();
        primaryIndexField = primaryIndexField();
    }

    /**
     * This class was referenced as a superclass of the given subclass.
     *
     * @param name the name of the class
     * @param subclass {@link ClassMetadata} of the subclass
     */
    public ClassMetadata(String name, ClassMetadata subclass) {
        this.className = name;
        this.hydrated = false;
        addSubclass(subclass);
    }

    /**
     * A class that was previously only seen as a temp superclass of another class can now be fully hydrated.
     *
     * @param classInfoDetails ClassInfo details
     */
    public void hydrate(ClassMetadata classInfoDetails) {

        if (!this.hydrated) {
            this.hydrated = true;

            this.isAbstract = classInfoDetails.isAbstract;
            this.isInterface = classInfoDetails.isInterface;
            this.isEnum = classInfoDetails.isEnum;
            this.directSuperclassName = classInfoDetails.directSuperclassName;

            //this.interfaces.addAll(classInfoDetails.interfaces());

            this.interfacesInfo.append(classInfoDetails.interfacesInfo());

            this.annotationsInfo.append(classInfoDetails.annotationsInfo());
            this.fieldsInfo.append(classInfoDetails.fieldsInfo());
            this.methodsInfo.append(classInfoDetails.methodsInfo());
        }
    }

    void extend(ClassMetadata classInfo) {
        this.interfacesInfo.append(classInfo.interfacesInfo());
        this.fieldsInfo.append(classInfo.fieldsInfo());
        this.methodsInfo.append(classInfo.methodsInfo());
    }

    /**
     * Connect this class to a subclass.
     *
     * @param subclass the subclass
     */
    public void addSubclass(ClassMetadata subclass) {
        if (subclass.directSuperclass != null && subclass.directSuperclass != this) {
            throw new RuntimeException(subclass.className + " has two superclasses: " + subclass.directSuperclass.className + ", " + this.className);
        }
        subclass.directSuperclass = this;
        this.directSubclasses.add(subclass);
    }

    public boolean hydrated() {
        return hydrated;
    }

    public String name() {
        return className;
    }

    String simpleName() {
        return className.substring(className.lastIndexOf('.') + 1);
    }

    public ClassMetadata directSuperclass() {
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
            try {
                lock.lock();
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
            } finally {
                lock.unlock();
            }
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
        for (ClassMetadata interfaceInfo : directInterfaces()) {
            interfaceInfo.collectLabels(labelNames);
        }
        return labelNames;
    }

    public List<ClassMetadata> directSubclasses() {
        return directSubclasses;
    }

    public List<ClassMetadata> directImplementingClasses() {
        return directImplementingClasses;
    }

    public List<ClassMetadata> directInterfaces() {
        return directInterfaces;
    }

    public org.neo4j.ogm.metadata.InterfacesInfo interfacesInfo() {
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

    public org.neo4j.ogm.metadata.AnnotationsInfo annotationsInfo() {
        return annotationsInfo;
    }

    public String superclassName() {
        return directSuperclassName;
    }

    public org.neo4j.ogm.metadata.FieldsInfo fieldsInfo() {
        return fieldsInfo;
    }

    public org.neo4j.ogm.metadata.MethodsInfo methodsInfo() {
        return methodsInfo;
    }

    private FieldMetadata identityFieldOrNull() {
        try {
            return identityField();
        } catch (MappingException me) {
            return null;
        }
    }

    /**
     * The identity field is a field annotated with @NodeId, or if none exists, a field
     * of type Long called 'id'
     *
     * @return A {@link FieldMetadata} object representing the identity field never <code>null</code>
     * @throws MappingException if no identity field can be found
     */
    public FieldMetadata identityField() {
        if (identityField != null) {
            return identityField;
        }
        try {
            lock.lock();
            if (identityField == null) {
                for (FieldMetadata fieldInfo : fieldsInfo().fields()) {
                    AnnotationInfo annotationInfo = fieldInfo.getAnnotations().get(GraphId.class);
                    if (annotationInfo != null) {
                        if (fieldInfo.getTypeDescriptor().equals("Ljava/lang/Long;")) {
                            identityField = fieldInfo;
                            return fieldInfo;
                        }
                    }
                }
                FieldMetadata fieldInfo = fieldsInfo().get("id");
                if (fieldInfo != null) {
                    if (fieldInfo.getTypeDescriptor().equals("Ljava/lang/Long;")) {
                        identityField = fieldInfo;
                        return fieldInfo;
                    }
                }
                throw new MappingException("No identity field found for class: " + this.className);
            } else {
                return identityField;
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * The label field is an optional field annotated with @Labels.
     *
     * @return A {@link FieldMetadata} object representing the label field. Optionally <code>null</code>
     */
    public FieldMetadata labelFieldOrNull() {
        if (labelFieldMapped) {
            return labelField;
        }
        try {
            lock.lock();
            if (!labelFieldMapped) {
                for (FieldMetadata fieldInfo : fieldsInfo().fields()) {
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
            } else {
                return labelField;
            }
        } finally {
            lock.unlock();
        }
        return null;
    }

    public boolean isRelationshipEntity() {
        for (AnnotationInfo info : annotations()) {
            if (info.getName().equals(RelationshipEntity.class.getCanonicalName())) {
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
    public Collection<FieldMetadata> propertyFields() {
        if (fieldInfos == null) {
            try {
                lock.lock();
                if (fieldInfos == null) {
                    FieldMetadata identityField = identityFieldOrNull();
                    fieldInfos = new HashSet<>();
                    for (FieldMetadata fieldInfo : fieldsInfo().fields()) {
                        if (fieldInfo != identityField && !fieldInfo.isLabelField()) {
                            AnnotationInfo annotationInfo = fieldInfo.getAnnotations().get(Property.class);
                            if (annotationInfo == null) {
                                if (fieldInfo.persistableAsProperty()) {
                                    fieldInfos.add(fieldInfo);
                                }
                            } else {
                                fieldInfos.add(fieldInfo);
                            }
                        }
                    }
                }
            } finally {
                lock.unlock();
            }
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
    public FieldMetadata propertyField(String propertyName) {
        if (propertyFields == null) {

            try {
                lock.lock();
                if (propertyFields == null) {
                    Collection<FieldMetadata> fieldInfos = propertyFields();
                    propertyFields = new HashMap<>(fieldInfos.size());
                    for (FieldMetadata fieldInfo : fieldInfos) {

                        propertyFields.put(fieldInfo.property().toLowerCase(), fieldInfo);
                    }
                }
            } finally {
                lock.unlock();
            }
        }
        return propertyFields.get(propertyName.toLowerCase());
    }


    /**
     * Finds the property field with a specific field name from the ClassInfo's property fields
     *
     * @param propertyName the propertyName of the field to find
     * @return A FieldInfo object describing the required property field, or null if it doesn't exist.
     */
    public FieldMetadata propertyFieldByName(String propertyName) {
        for (FieldMetadata fieldInfo : propertyFields()) {
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
    public Collection<FieldMetadata> relationshipFields() {
        FieldMetadata identityField = identityFieldOrNull();
        Set<FieldMetadata> fieldInfos = new HashSet<>();
        for (FieldMetadata fieldInfo : fieldsInfo().fields()) {
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
    public FieldMetadata relationshipField(String relationshipName) {
        for (FieldMetadata fieldInfo : relationshipFields()) {
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
    public FieldMetadata relationshipField(String relationshipName, String relationshipDirection, boolean strict) {
        for (FieldMetadata fieldInfo : relationshipFields()) {
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
    public Set<FieldMetadata> candidateRelationshipFields(String relationshipName, String relationshipDirection, boolean strict) {
        Set<FieldMetadata> candidateFields = new HashSet<>();
        for (FieldMetadata fieldInfo : relationshipFields()) {
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
    public FieldMetadata relationshipFieldByName(String fieldName) {
        for (FieldMetadata fieldInfo : relationshipFields()) {
            if (fieldInfo.getName().equalsIgnoreCase(fieldName)) {
                return fieldInfo;
            }
        }
        return null;
    }

    public Field getField(FieldMetadata fieldInfo) {
        Field field = fieldInfoFields.get(fieldInfo);
        if (field != null) {
            return field;
        }
        try {
            field = MetaDataClassLoader.loadClass(name()).getDeclaredField(fieldInfo.getName());
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
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the Method corresponding to the supplied MethodInfo as declared by the class represented by this ClassInfo
     *
     * @param methodInfo the MethodInfo used to obtain the Method
     * @return a Method
     */
    public Method getMethod(MethodMetadata methodInfo) {
        return methodInfo.getMethod();
    }

    /**
     * Find all FieldInfos for the specified ClassInfo whose type matches the supplied fieldType
     *
     * @param fieldType The field type to look for
     * @return A {@link List} of {@link FieldMetadata} objects that are of the given type, never <code>null</code>
     */
    public List<FieldMetadata> findFields(Class<?> fieldType) {
        String fieldSignature = "L" + fieldType.getName().replace(".", "/") + ";";
        List<FieldMetadata> fieldInfos = new ArrayList<>();
        for (FieldMetadata fieldInfo : fieldsInfo().fields()) {
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
     * @return A {@link List} of {@link FieldMetadata} objects that are of the given type, never <code>null</code>
     */
    public List<FieldMetadata> findFields(String annotation) {
        List<FieldMetadata> fieldInfos = new ArrayList<>();
        for (FieldMetadata fieldInfo : fieldsInfo().fields()) {
            if (fieldInfo.hasAnnotation(annotation)) {
                fieldInfos.add(fieldInfo);
            }
        }
        return fieldInfos;
    }

    /**
     * Retrieves a {@link List} of {@link FieldMetadata} representing all of the fields that can be iterated over
     * using a "foreach" loop.
     *
     * @return {@link List} of {@link FieldMetadata}
     */
    public List<FieldMetadata> findIterableFields() {
        List<FieldMetadata> fieldInfos = new ArrayList<>();
        try {
            for (FieldMetadata fieldInfo : fieldsInfo().fields()) {
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
     * @return {@link List} of {@link MethodMetadata}, never <code>null</code>
     */
    public List<FieldMetadata> findIterableFields(Class iteratedType) {
        if (iterableFieldsForType.containsKey(iteratedType)) {
            return iterableFieldsForType.get(iteratedType);
        }
        List<FieldMetadata> fieldInfos = new ArrayList<>();
        String typeSignature = "L" + iteratedType.getName().replace('.', '/') + ";";
        String arrayOfTypeSignature = "[" + typeSignature;
        try {
            for (FieldMetadata fieldInfo : fieldsInfo().fields()) {
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
     * @return {@link List} of {@link MethodMetadata}, never <code>null</code>
     */
    public List<FieldMetadata> findIterableFields(Class iteratedType, String relationshipType, String relationshipDirection, boolean strict) {
        List<FieldMetadata> fieldInfos = new ArrayList<>();
        for (FieldMetadata fieldInfo : findIterableFields(iteratedType)) {
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
    public boolean isSubclassOf(ClassMetadata classInfo) {

        if (this == classInfo) {
            return true;
        }

        boolean found = false;

        for (ClassMetadata subclass : classInfo.directSubclasses()) {
            found = isSubclassOf(subclass);
            if (found) {
                break;
            }
        }

        return found;
    }

    public Class<?> getType(String typeParameterDescriptor) {
        return ClassUtils.getType(typeParameterDescriptor);
    }

    /**
     * Get the underlying class represented by this ClassInfo
     *
     * @return the underlying class or null if it cannot be determined
     */
    public Class getUnderlyingClass() {
        try {
            return MetaDataClassLoader.loadClass(className);//Class.forName(className);
        } catch (ClassNotFoundException e) {
            LOGGER.error("Could not get underlying class for {}", className);
        }
        return null;
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
    public Class getTypeParameterDescriptorForRelationship(String relationshipType, String relationshipDirection) {
        final boolean STRICT_MODE = true; //strict mode for matching methods and fields, will only look for explicit annotations
        final boolean INFERRED_MODE = false; //inferred mode for matching methods and fields, will infer the relationship type from the getter/setter/property

        try {
            FieldMetadata fieldInfo = relationshipField(relationshipType, relationshipDirection, STRICT_MODE);
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
    public Collection<FieldMetadata> getIndexFields() {
        if (indexFields == null) {
            indexFields = addIndexes();
        }
        return indexFields.values();
    }

    private Map<String, FieldMetadata> addIndexes() {
        Map<String, FieldMetadata> indexes = new HashMap<>();

        // No way to get declared fields from current byte code impl. Using reflection instead.
        Field[] declaredFields;
        try {
            declaredFields = MetaDataClassLoader.loadClass(className).getDeclaredFields();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Could not reflectively read declared fields", e);
        }

        final String indexAnnotation = Index.class.getCanonicalName();

        for (FieldMetadata fieldInfo : fieldsInfo().fields()) {
            if (isDeclaredField(declaredFields, fieldInfo.getName()) && fieldInfo.hasAnnotation(indexAnnotation)) {

                String propertyValue = fieldInfo.property();
                if (fieldInfo.hasAnnotation(Property.class.getCanonicalName())) {
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


    public FieldMetadata primaryIndexField() {
        if (!primaryIndexFieldChecked && primaryIndexField == null) {
            final String indexAnnotation = Index.class.getCanonicalName();

            for (FieldMetadata fieldInfo : fieldsInfo().fields()) {
                AnnotationInfo annotationInfo = fieldInfo.getAnnotations().get(indexAnnotation);
                if (annotationInfo != null && annotationInfo.get("primary") != null && annotationInfo.get("primary").equals("true")) {

                    if (primaryIndexField == null) {
                        primaryIndexField = fieldInfo;
                    } else {
                        throw new Neo4jException("Each class may only define one primary index.");
                    }
                }
            }
            primaryIndexFieldChecked = true;
        }

        return primaryIndexField;
    }

    public MethodMetadata postLoadMethodOrNull() {
        for (MethodMetadata methodInfo : methodsInfo().methods()) {
            if (methodInfo.hasAnnotation(PostLoad.class.getCanonicalName())) {
                return methodInfo;
            }
        }
        return null;
    }
}


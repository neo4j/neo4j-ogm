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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.neo4j.ogm.ClassUtils;
import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.Transient;
import org.neo4j.ogm.classloader.MetaDataClassLoader;
import org.neo4j.ogm.exception.MappingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Maintains object to graph mapping details at the class (type) level
 *
 * The ClassInfo object is used to maintain mappings from Java Types-&gt;Neo4j Labels
 * thereby allowing the correct labels to be applied to new nodes when they
 * are persisted.
 *
 * The ClassInfo object also maintains a map of FieldInfo and MethodInfo objects
 * that maintain the appropriate information for mapping Java class attributes to Neo4j
 * node properties / paths (node)-[:relationship]-&gt;(node), via field or method
 * accessors respectively.
 *
 * Given a type hierarchy, the ClassInfo object guarantees that for any type in that
 * hierarchy, the labels associated with that type will include the labels for
 * all its superclass and interface types as well. This is to avoid the need to iterate
 * through the ClassInfo hierarchy to recover label information.
 *
 * @author Vince Bickers
 * @author Luanne Misquitta
 */
public class ClassInfo {

    private int majorVersion;
    private int minorVersion;

    private String className;
    private String directSuperclassName;
    private String neo4jName;

    private boolean isInterface;
    private boolean isAbstract;
    private boolean isEnum;
    private boolean hydrated;

    private org.neo4j.ogm.metadata.FieldsInfo fieldsInfo = new org.neo4j.ogm.metadata.FieldsInfo();
    private org.neo4j.ogm.metadata.MethodsInfo methodsInfo= new org.neo4j.ogm.metadata.MethodsInfo();
    private org.neo4j.ogm.metadata.AnnotationsInfo annotationsInfo = new org.neo4j.ogm.metadata.AnnotationsInfo();
    private org.neo4j.ogm.metadata.InterfacesInfo interfacesInfo = new org.neo4j.ogm.metadata.InterfacesInfo();

    private ClassInfo directSuperclass;

    private final List<ClassInfo> directSubclasses = new ArrayList<>();
    private final List<ClassInfo> directInterfaces = new ArrayList<>();
    private final List<ClassInfo> directImplementingClasses = new ArrayList<>();


    private Map<Class, List<MethodInfo>> iterableGettersForType = new HashMap<>();
    private Map<Class, List<MethodInfo>> iterableSettersForType = new HashMap<>();
    private Map<Class,List<FieldInfo>> iterableFieldsForType = new HashMap<>();
    private Map<FieldInfo, Field> fieldInfoFields = new HashMap<>();

    private Set<FieldInfo> fieldInfos;
    private Map<String, FieldInfo> propertyFields;

    private FieldInfo identityField = null;

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassInfo.class);


    // todo move this to a factory class
    public ClassInfo(InputStream inputStream) throws IOException {

        DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(inputStream, 1024));

        // Magic
        if (dataInputStream.readInt() != 0xCAFEBABE) {
            return;
        }

        minorVersion = dataInputStream.readUnsignedShort();    //minor version
        majorVersion = dataInputStream.readUnsignedShort();    // major version

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
        interfacesInfo = new org.neo4j.ogm.metadata.InterfacesInfo(dataInputStream, constantPool);
        fieldsInfo = new org.neo4j.ogm.metadata.FieldsInfo(dataInputStream, constantPool);
        methodsInfo = new org.neo4j.ogm.metadata.MethodsInfo(dataInputStream, constantPool);
        annotationsInfo = new org.neo4j.ogm.metadata.AnnotationsInfo(dataInputStream, constantPool);

    }

    /** A class that was previously only seen as a temp superclass of another class can now be fully hydrated.
     *
     * @param classInfoDetails  ClassInfo details
     */
    public void hydrate(ClassInfo classInfoDetails) {

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

    void extend(ClassInfo classInfo) {
        //this.interfaces.addAll(classInfo.interfaces());
        this.interfacesInfo.append(classInfo.interfacesInfo());

        this.fieldsInfo.append(classInfo.fieldsInfo());
        this.methodsInfo.append(classInfo.methodsInfo());
    }

    /**
     * This class was referenced as a superclass of the given subclass.
     * @param name the name of the class
     * @param subclass {@link ClassInfo} of the subclass
     */
    public ClassInfo(String name, ClassInfo subclass) {
        this.className = name;
        this.hydrated = false;
        addSubclass(subclass);
    }

    /** Connect this class to a subclass.
     *
     * @param subclass the subclass
     */
    public void addSubclass(ClassInfo subclass) {
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

    ClassInfo directSuperclass() {
        return directSuperclass;
    }

    /**
     * Retrieves the labels that are applied to nodes in the database that store information about instances of the class. If
     * the class' instances are persisted by a relationship instead of a node then this method returns an empty collection.
     *
     * @return A {@link Collection} of all the labels that apply to the node or an empty list if there aren't any, never
     *         <code>null</code>
     */
    public Collection<String> labels() {
        return collectLabels(new ArrayList<String>());
    }

    public String neo4jName() {
        if (neo4jName == null) {
            AnnotationInfo annotationInfo = annotationsInfo.get(NodeEntity.CLASS);
            if (annotationInfo != null) {
                neo4jName =  annotationInfo.get(NodeEntity.LABEL, simpleName());
                return neo4jName;
            }
            annotationInfo = annotationsInfo.get(RelationshipEntity.CLASS);
            if (annotationInfo != null) {
                neo4jName =  annotationInfo.get(RelationshipEntity.TYPE, simpleName().toUpperCase());
                return neo4jName;
            }
            neo4jName = simpleName();
        }
        return neo4jName;
    }

    private Collection<String> collectLabels(Collection<String> labelNames) {
        if (!isAbstract || annotationsInfo.get(NodeEntity.CLASS) != null) {
            labelNames.add(neo4jName());
        }
        if (directSuperclass != null && !"java.lang.Object".equals(directSuperclass.className)) {
            directSuperclass.collectLabels(labelNames);
        }
        for(ClassInfo interfaceInfo : directInterfaces()) {
            interfaceInfo.collectLabels(labelNames);
        }
        return labelNames;
    }

    public List<ClassInfo> directSubclasses() {
        return directSubclasses;
    }

    public List<ClassInfo> directImplementingClasses() {
        return directImplementingClasses;
    }

    public List<ClassInfo> directInterfaces() {
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

    @Override
    public String toString() {
        return name();
    }

    private FieldInfo identityFieldOrNull() {
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
     * @return A {@link FieldInfo} object representing the identity field never <code>null</code>
     * @throws MappingException if no identity field can be found
     */
    public FieldInfo identityField() {
        if(identityField != null) {
            return identityField;
        }
        for (FieldInfo fieldInfo : fieldsInfo().fields()) {
            AnnotationInfo annotationInfo = fieldInfo.getAnnotations().get(GraphId.CLASS);
            if (annotationInfo != null) {
                if (fieldInfo.getDescriptor().equals("Ljava/lang/Long;")) {
                    identityField = fieldInfo;
                    return fieldInfo;
                }
            }
        }
        FieldInfo fieldInfo = fieldsInfo().get("id");
        if (fieldInfo != null) {
            if (fieldInfo.getDescriptor().equals("Ljava/lang/Long;")) {
                identityField = fieldInfo;
                return fieldInfo;
            }
        }
        throw new MappingException("No identity field found for class: " + this.className);
    }

    /**
     * A property field is any field annotated with @Property, or any field that can be mapped to a
     * node property. The identity field is not a property field.
     *
     * @return A Collection of FieldInfo objects describing the classInfo's property fields
     */
    public Collection<FieldInfo> propertyFields() {
        if (fieldInfos == null) {
            FieldInfo identityField = identityFieldOrNull();
            fieldInfos = new HashSet<>();
            for (FieldInfo fieldInfo : fieldsInfo().fields()) {
                if (fieldInfo != identityField) {
                    AnnotationInfo annotationInfo = fieldInfo.getAnnotations().get(Property.CLASS);
                    if (annotationInfo == null) {
                        if (fieldInfo.isSimple()) {
                            fieldInfos.add(fieldInfo);
                        }
                    } else {
                        fieldInfos.add(fieldInfo);
                    }
                }
            }
        }
        return fieldInfos;
    }

    /**
     * Finds the property field with a specific property name from the ClassInfo's property fields
     *
     * @param propertyName the propertyName of the field to find
     * @return A FieldInfo object describing the required property field, or null if it doesn't exist.
     */
    public FieldInfo propertyField(String propertyName) {
        if (propertyFields == null) {
            Collection<FieldInfo> fieldInfos = propertyFields();
            propertyFields = new HashMap<>(fieldInfos.size());
            for (FieldInfo fieldInfo : fieldInfos) {
                propertyFields.put(fieldInfo.property(), fieldInfo);
            }
        }
        return propertyFields.get(propertyName);
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
                AnnotationInfo annotationInfo = fieldInfo.getAnnotations().get(Relationship.CLASS);
                if (annotationInfo == null) {
                    if (!fieldInfo.isSimple()) {
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
     * @param relationshipName      the relationshipName of the field to find
     * @param relationshipDirection the direction of the relationship
     * @param strict                if true, does not infer relationship type but looks for it in the @Relationship annotation. Null if missing. If false, infers relationship type from FieldInfo
     * @return A FieldInfo object describing the required relationship field, or null if it doesn't exist.
     */
    public FieldInfo relationshipField(String relationshipName, String relationshipDirection, boolean strict) {
        for (FieldInfo fieldInfo : relationshipFields()) {
            String relationship = strict ? fieldInfo.relationshipTypeAnnotation() : fieldInfo.relationship();
            if (relationshipName.equalsIgnoreCase(relationship)) {
                if(((fieldInfo.relationshipDirection(Relationship.OUTGOING).equals(Relationship.INCOMING) || fieldInfo.relationshipDirection(Relationship.OUTGOING).equals(Relationship.UNDIRECTED))&& (relationshipDirection.equals(Relationship.INCOMING)))
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
     * @param relationshipName      the relationshipName of the field to find
     * @param relationshipDirection the direction of the relationship
     * @param strict                if true, does not infer relationship type but looks for it in the @Relationship annotation. Null if missing. If false, infers relationship type from FieldInfo
     * @return Set of  FieldInfo objects describing the required relationship field, or empty set if it doesn't exist.
     */
    public Set<FieldInfo> candidateRelationshipFields(String relationshipName, String relationshipDirection, boolean strict) {
        Set<FieldInfo> candidateFields = new HashSet<>();
        for (FieldInfo fieldInfo : relationshipFields()) {
            String relationship = strict ? fieldInfo.relationshipTypeAnnotation() : fieldInfo.relationship();
            if (relationshipName.equalsIgnoreCase(relationship)) {
                if(((fieldInfo.relationshipDirection(Relationship.OUTGOING).equals(Relationship.INCOMING) || fieldInfo.relationshipDirection(Relationship.OUTGOING).equals(Relationship.UNDIRECTED))&& (relationshipDirection.equals(Relationship.INCOMING)))
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

    /**
     * The identity getter is any getter annotated with @NodeId returning a Long, or if none exists, a getter
     * returning Long called 'getId'
     *
     * @return A FieldInfo object representing the identity field or null if it doesn't exist
     */
    public MethodInfo identityGetter() {
        for (MethodInfo methodInfo : methodsInfo().getters()) {
            AnnotationInfo annotationInfo = methodInfo.getAnnotations().get(GraphId.CLASS);
            if (annotationInfo != null) {
                if (methodInfo.getDescriptor().equals("()Ljava/lang/Long;")) {
                    return methodInfo;
                }
            }
        }
        MethodInfo methodInfo = methodsInfo().get("getId");
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
     * @return A FieldInfo object representing the identity field or null if it doesn't exist
     */
    public MethodInfo identitySetter() {
        for (MethodInfo methodInfo : methodsInfo().setters()) {
            AnnotationInfo annotationInfo = methodInfo.getAnnotations().get(GraphId.CLASS);
            if (annotationInfo != null) {
                if (methodInfo.getDescriptor().equals("(Ljava/lang/Long;)V")) {
                    return methodInfo;
                }
            }
        }
        MethodInfo methodInfo = methodsInfo().get("setId");
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
     * @return A Collection of MethodInfo objects describing the classInfo's property getters
     */
    public Collection<MethodInfo> propertyGetters() {
        MethodInfo identityGetter = identityGetter();
        Set<MethodInfo> propertyGetters = new HashSet<>();
        for (MethodInfo methodInfo : methodsInfo().getters()) {
            if (identityGetter == null || !methodInfo.getName().equals(identityGetter.getName())) {
                AnnotationInfo annotationInfo = methodInfo.getAnnotations().get(Property.CLASS);
                if (annotationInfo == null) {
                    if (methodInfo.isSimpleGetter()) {
                        propertyGetters.add(methodInfo);
                    }
                } else {
                    propertyGetters.add(methodInfo);
                }
            }
        }
        return propertyGetters;
    }

    /**
     * A property setter is any setter annotated with @Property, or any setter whose parameter type can be mapped to a
     * node property. The identity setter is not a property setter.
     *
     * @return A Collection of MethodInfo objects describing the classInfo's property setters
     */
    public Collection<MethodInfo> propertySetters() {
        MethodInfo identitySetter = identitySetter();
        Set<MethodInfo> propertySetters = new HashSet<>();
        for (MethodInfo methodInfo : methodsInfo().setters()) {
            if (identitySetter == null || !methodInfo.getName().equals(identitySetter.getName())) {
                AnnotationInfo annotationInfo = methodInfo.getAnnotations().get(Property.CLASS);
                if (annotationInfo == null) {
                    if (methodInfo.isSimpleSetter()) {
                        propertySetters.add(methodInfo);
                    }
                } else {
                    propertySetters.add(methodInfo);
                }
            }
        }
        return propertySetters;
    }

    /**
     * A relationship getter is any getter annotated with @Relationship, or any getter whose return type cannot be mapped to a
     * node property. The identity getter is not a property getter.
     *
     * @return A Collection of MethodInfo objects describing the classInfo's property getters
     */
    public Collection<MethodInfo> relationshipGetters() {
        MethodInfo identityGetter = identityGetter();
        Set<MethodInfo> relationshipGetters = new HashSet<>();
        for (MethodInfo methodInfo : methodsInfo().getters()) {
            if (identityGetter == null || !methodInfo.getName().equals(identityGetter.getName())) {
                AnnotationInfo annotationInfo = methodInfo.getAnnotations().get(Relationship.CLASS);
                if (annotationInfo == null) {
                    if (!methodInfo.isSimpleGetter()) {
                        relationshipGetters.add(methodInfo);
                    }
                } else {
                    relationshipGetters.add(methodInfo);
                }
            }
        }
        return relationshipGetters;
    }

    /**
     * A relationship setter is any setter annotated with @Relationship, or any setter whose parameter type cannot be mapped to a
     * node property. The identity setter is not a property getter.
     *
     * @return A Collection of MethodInfo objects describing the classInfo's property getters
     */
    public Collection<MethodInfo> relationshipSetters() {
        MethodInfo identitySetter = identitySetter();
        Set<MethodInfo> relationshipSetters = new HashSet<>();
        for (MethodInfo methodInfo : methodsInfo().setters()) {
            if (identitySetter == null || !methodInfo.getName().equals(identitySetter.getName())) {
                AnnotationInfo annotationInfo = methodInfo.getAnnotations().get(Relationship.CLASS);
                if (annotationInfo == null) {
                    if (!methodInfo.isSimpleSetter()) {
                        relationshipSetters.add(methodInfo);
                    }
                } else {
                    relationshipSetters.add(methodInfo);
                }
            }
        }
        return relationshipSetters;
    }

    /**
     * Finds the relationship getter with a specific name from the specified ClassInfo's relationship getters
     *
     * @param relationshipName the relationshipName of the getter to find
     * @return A MethodInfo object describing the required relationship getter, or null if it doesn't exist.
     */
    public MethodInfo relationshipGetter(String relationshipName) {
        for (MethodInfo methodInfo : relationshipGetters()) {
            if (methodInfo.relationship().equalsIgnoreCase(relationshipName)) {
                return methodInfo;
            }
        }
        return null;
    }

    /**
     * Finds the relationship getter with a specific name and direction from the specified ClassInfo's relationship getters
     *
     * @param relationshipName      the relationshipName of the getter to find
     * @param relationshipDirection the relationship direction
     * @param strict                if true, does not infer relationship type but looks for it in the @Relationship annotation. Null if missing. If false, infers relationship type from MethodInfo
     * @return A MethodInfo object describing the required relationship getter, or null if it doesn't exist.
     */
    public MethodInfo relationshipGetter(String relationshipName, String relationshipDirection, boolean strict) {
        for (MethodInfo methodInfo : relationshipGetters()) {
            String relationship = strict ? methodInfo.relationshipTypeAnnotation() : methodInfo.relationship();
            if (relationshipName.equalsIgnoreCase(relationship)) {
                if(((methodInfo.relationshipDirection(Relationship.OUTGOING).equals(Relationship.INCOMING) || methodInfo.relationshipDirection(Relationship.OUTGOING).equals(Relationship.UNDIRECTED)) && relationshipDirection.equals(Relationship.INCOMING))
                        || (relationshipDirection.equals(Relationship.OUTGOING) && !(methodInfo.relationshipDirection(Relationship.OUTGOING).equals(Relationship.INCOMING)))) {
                    return methodInfo;
                }
            }
        }
        return null;
    }

    /**
     * Finds the relationship setter with a specific name from the specified ClassInfo's relationship setters
     *
     * @param relationshipName the relationshipName of the setter to find
     * @return A MethodInfo object describing the required relationship setter, or null if it doesn't exist.
     */
    public MethodInfo relationshipSetter(String relationshipName) {
        for (MethodInfo methodInfo : relationshipSetters()) {
            if (methodInfo.relationship().equalsIgnoreCase(relationshipName)) {
                return methodInfo;
            }
        }
        return null;
    }

    /**
     * Finds the relationship setter with a specific name and direction from the specified ClassInfo's relationship setters.
     *
     * @param relationshipName      the relationshipName of the setter to find
     * @param relationshipDirection the relationship direction
     * @param strict                if true, does not infer relationship type but looks for it in the @Relationship annotation. Null if missing. If false, infers relationship type from MethodInfo
     * @return A MethodInfo object describing the required relationship setter, or null if it doesn't exist.
     */
    public MethodInfo relationshipSetter(String relationshipName, String relationshipDirection, boolean strict) {
        for (MethodInfo methodInfo : relationshipSetters()) {
            String relationship = strict ? methodInfo.relationshipTypeAnnotation() : methodInfo.relationship();
            if (relationshipName.equalsIgnoreCase(relationship)) {
                if(((methodInfo.relationshipDirection(Relationship.OUTGOING).equals(Relationship.INCOMING) || methodInfo.relationshipDirection(Relationship.OUTGOING).equals(Relationship.UNDIRECTED)) && relationshipDirection.equals(Relationship.INCOMING))
                        || (relationshipDirection.equals(Relationship.OUTGOING) && !(methodInfo.relationshipDirection(Relationship.OUTGOING).equals(Relationship.INCOMING)))) {
                    return methodInfo;
                }
            }
        }
        return null;
    }

    /**
     * Finds all relationship setters with a specific name and direction from the specified ClassInfo's relationship setters.
     *
     * @param relationshipName      the relationshipName of the setter to find
     * @param relationshipDirection the relationship direction
     * @param strict                if true, does not infer relationship type but looks for it in the @Relationship annotation. Null if missing. If false, infers relationship type from MethodInfo
     * @return A Set of MethodInfo object describing the required relationship setter, or empty set if it doesn't exist.
     */
    public Set<MethodInfo> candidateRelationshipSetters(String relationshipName, String relationshipDirection, boolean strict) {
        Set<MethodInfo> candidateSetters = new HashSet<>();
        for (MethodInfo methodInfo : relationshipSetters()) {
            String relationship = strict ? methodInfo.relationshipTypeAnnotation() : methodInfo.relationship();
            if (relationshipName.equalsIgnoreCase(relationship)) {
                if(((methodInfo.relationshipDirection(Relationship.OUTGOING).equals(Relationship.INCOMING) || methodInfo.relationshipDirection(Relationship.OUTGOING).equals(Relationship.UNDIRECTED)) && relationshipDirection.equals(Relationship.INCOMING))
                        || (relationshipDirection.equals(Relationship.OUTGOING) && !(methodInfo.relationshipDirection(Relationship.OUTGOING).equals(Relationship.INCOMING)))) {
                    candidateSetters.add(methodInfo);
                }
            }
        }
        return candidateSetters;
    }

    /**
     * Finds the property setter with a specific name from the specified ClassInfo's property setters
     *
     * @param propertyName the propertyName of the setter to find
     * @return A MethodInfo object describing the required property setter, or null if it doesn't exist.
     */
    public MethodInfo propertySetter(String propertyName) {
        for (MethodInfo methodInfo : propertySetters()) {
            String match = methodInfo.property();
            if (match.equalsIgnoreCase(propertyName) || match.equalsIgnoreCase("set" + propertyName)) {
                return methodInfo;
            }
        }
        return null;
    }

    /**
     * Finds the property getter with a specific name from the specified ClassInfo's property getters
     *
     * @param propertyName the propertyName of the getter to find
     * @return A MethodInfo object describing the required property getter, or null if it doesn't exist.
     */
    public MethodInfo propertyGetter(String propertyName) {
        for (MethodInfo methodInfo : propertyGetters()) {
            String match = methodInfo.property();
            if (match.equalsIgnoreCase(propertyName) || match.equalsIgnoreCase("get" + propertyName)) {
                return methodInfo;
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
            field =  MetaDataClassLoader.loadClass(name()).getDeclaredField(fieldInfo.getName());
            fieldInfoFields.put(fieldInfo, field);
            return field;
        } catch (NoSuchFieldException e) {
            if (directSuperclass() != null) {
                return directSuperclass().getField(fieldInfo);
            } else {
                throw new RuntimeException("Field " + fieldInfo.getName() + " not found in class " + name() + " or any of its superclasses");
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

    }


    public Method getMethod(MethodInfo methodInfo, Class... parameterTypes) {
        try {
            return MetaDataClassLoader.loadClass(name()).getMethod(methodInfo.getName(), parameterTypes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Find all setter MethodInfos for the specified ClassInfo whose parameter type matches the supplied class
     *
     * @param parameterType The setter parameter type to look for.
     * @return A {@link List} of {@link MethodInfo} objects that accept the given parameter type, never <code>null</code>
     */
    public List<MethodInfo> findSetters(Class<?> parameterType) {
        String setterSignature = "(L" + parameterType.getName().replace(".", "/") + ";)V";
        List<MethodInfo> methodInfos = new ArrayList<>();
        for (MethodInfo methodInfo : methodsInfo().methods()) {
            if (methodInfo.getDescriptor().equals(setterSignature)) {
                methodInfos.add(methodInfo);
            }
        }
        return methodInfos;
    }

    /**
     * Find all getter MethodInfos for the specified ClassInfo whose return type matches the supplied class
     *
     * @param returnType The getter return type to look for.
     * @return A {@link List} of {@link MethodInfo} objects that return the given type, never <code>null</code>
     */
    public List<MethodInfo> findGetters(Class<?> returnType) {
        String setterSignature = "()L" + returnType.getName().replace(".", "/") + ";";
        List<MethodInfo> methodInfos = new ArrayList<>();
        for (MethodInfo methodInfo : methodsInfo().methods()) {
            if (methodInfo.getDescriptor().equals(setterSignature)) {
                methodInfos.add(methodInfo);
            }
        }
        return methodInfos;
    }

    /**
     * Find all FieldInfos for the specified ClassInfo whose type matches the supplied fieldType
     *
     * @param fieldType The field type to look for
     * @return A {@link List} of {@link FieldInfo} objects that are of the given type, never <code>null</code>
     */
    public List<FieldInfo> findFields(Class<?> fieldType) {
        String fieldSignature = "L" + fieldType.getName().replace(".", "/") + ";";
        List<FieldInfo> fieldInfos = new ArrayList<>();
        for (FieldInfo fieldInfo : fieldsInfo().fields() ) {
            if (fieldInfo.getDescriptor().equals(fieldSignature)) {
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
        for (FieldInfo fieldInfo : fieldsInfo().fields() ) {
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
            for (FieldInfo fieldInfo : fieldsInfo().fields() ) {
                Class type = getField(fieldInfo).getType();
                if (type.isArray() || Iterable.class.isAssignableFrom(type)) {
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
     * Finds all fields whose type is equivalent to Array&lt;X&gt; or assignable from Iterable&lt;X&gt;
     * where X is the generic parameter type of the Array or Iterable
     *
     * @param iteratedType      the type of iterable
     * @return {@link List} of {@link MethodInfo}, never <code>null</code>
     */
    public List<FieldInfo> findIterableFields(Class iteratedType) {
        if(iterableFieldsForType.containsKey(iteratedType)) {
            return iterableFieldsForType.get(iteratedType);
        }
        List<FieldInfo> fieldInfos = new ArrayList<>();
        String typeSignature = "L" + iteratedType.getName().replace('.', '/') + ";";
        String arrayOfTypeSignature = "[" + typeSignature;
        try {
            for (FieldInfo fieldInfo : fieldsInfo().fields() ) {
                if (fieldInfo.getTypeParameterDescriptor() != null) {
                    if (fieldInfo.getTypeParameterDescriptor().equals(typeSignature) || fieldInfo.isParameterisedTypeOf(iteratedType)) {
                        fieldInfos.add(fieldInfo);
                    }
                } else if (fieldInfo.getDescriptor().equals(arrayOfTypeSignature) || fieldInfo.isParameterisedTypeOf(iteratedType)) {
                    fieldInfos.add(fieldInfo);
                }
            }
            iterableFieldsForType.put(iteratedType, fieldInfos);
            return fieldInfos;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Finds all fields whose type is equivalent to Array&lt;X&gt; or assignable from Iterable&lt;X&gt;
     * where X is the generic parameter type of the Array or Iterable and the relationship type backing this iterable is "relationshipType"
     *
     * @param iteratedType      the type of iterable
     * @param relationshipType  the relationship type
     * @param relationshipDirection the relationship direction
     * @param strict            if true, does not infer relationship type but looks for it in the @Relationship annotation. Null if missing. If false, infers relationship type from FieldInfo
     * @return {@link List} of {@link MethodInfo}, never <code>null</code>
     */
    public List<FieldInfo> findIterableFields(Class iteratedType, String relationshipType, String relationshipDirection, boolean strict) {
        List<FieldInfo> fieldInfos = new ArrayList<>();
        for(FieldInfo fieldInfo : findIterableFields(iteratedType)) {
            String relationship = strict ? fieldInfo.relationshipTypeAnnotation() : fieldInfo.relationship();
            if(relationshipType.equals(relationship)) {
                if(((fieldInfo.relationshipDirection(Relationship.OUTGOING).equals(Relationship.INCOMING) || fieldInfo.relationshipDirection(Relationship.OUTGOING).equals(Relationship.UNDIRECTED)) && relationshipDirection.equals(Relationship.INCOMING))
                        || (relationshipDirection.equals(Relationship.OUTGOING) && !(fieldInfo.relationshipDirection(Relationship.OUTGOING).equals(Relationship.INCOMING)))) {
                    fieldInfos.add(fieldInfo);
                }
            }
        }
        return fieldInfos;
    }


    /**
     * Finds all setter methods whose parameter signature is equivalent to Array&lt;X&gt; or assignable from Iterable&lt;X&gt;
     * where X is the generic parameter type of the Array or Iterable
     *
     * @param iteratedType  the type of iterable
     * @return {@link List} of {@link MethodInfo}, never <code>null</code>
     *
     */
    public List<MethodInfo> findIterableSetters(Class iteratedType) {
        if(iterableSettersForType.containsKey(iteratedType)) {
            return iterableSettersForType.get(iteratedType);
        }
        List<MethodInfo> methodInfos = new ArrayList<>();
        String typeSignature = "L" + iteratedType.getName().replace('.', '/') + ";";
        String arrayOfTypeSignature = "([" + typeSignature + ")V";
        try {
            for (MethodInfo methodInfo : propertySetters()) {
                if (methodInfo.getTypeParameterDescriptor() != null) {
                    if (methodInfo.getTypeParameterDescriptor().equals(typeSignature) || methodInfo.isParameterisedTypeOf(iteratedType)) {
                        methodInfos.add(methodInfo);
                    }
                } else {
                    if (methodInfo.getDescriptor().equals(arrayOfTypeSignature) || methodInfo.isParameterisedTypeOf(iteratedType)) {
                        methodInfos.add(methodInfo);
                    }
                }
            }

            for (MethodInfo methodInfo : relationshipSetters()) {
                if (methodInfo.getTypeParameterDescriptor() != null) {
                    if (methodInfo.getTypeParameterDescriptor().equals(typeSignature) || methodInfo.isParameterisedTypeOf(iteratedType)) {
                        methodInfos.add(methodInfo);
                    } else {
                        if (methodInfo.getDescriptor().equals(arrayOfTypeSignature) || methodInfo.isParameterisedTypeOf(iteratedType)) {
                            methodInfos.add(methodInfo);
                        }
                    }
                }
            }
            iterableSettersForType.put(iteratedType, methodInfos);
            return methodInfos;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Finds all setter methods whose parameter signature is equivalent to Array&lt;X&gt; or assignable from Iterable&lt;X&gt;
     * where X is the generic parameter type of the Array or Iterable and the relationship type this setter is annotated with is "relationshipType"
     * and the relationship direction matches "relationshipDirection"
     *
     * @param iteratedType          the type of iterable
     * @param relationshipType      the relationship type
     * @param relationshipDirection the relationship direction
     * @param strict                if true, does not infer relationship type but looks for it in the @Relationship annotation. Null if missing. If false, infers relationship type from MethodInfo
     * @return {@link List} of {@link MethodInfo}, never <code>null</code>
     * */
    public List<MethodInfo> findIterableSetters(Class iteratedType, String relationshipType, String relationshipDirection, boolean strict) {
        List<MethodInfo> methodInfos = new ArrayList<>();
        for(MethodInfo methodInfo : findIterableSetters(iteratedType)) {
            String relationship = strict ? methodInfo.relationshipTypeAnnotation() : methodInfo.relationship();
            if(relationshipType.equals(relationship)) {
                if(((methodInfo.relationshipDirection(Relationship.OUTGOING).equals(Relationship.INCOMING) || methodInfo.relationshipDirection(Relationship.OUTGOING).equals(Relationship.UNDIRECTED)) && relationshipDirection.equals(Relationship.INCOMING))
                        || (relationshipDirection.equals(Relationship.OUTGOING) && !(methodInfo.relationshipDirection(Relationship.OUTGOING).equals(Relationship.INCOMING)))) {
                    methodInfos.add(methodInfo);
                }
            }
        }
        return methodInfos;
    }


    /**
     * Finds all getter methods whose parameterised return type is equivalent to Array&lt;X&gt; or assignable from Iterable&lt;X&gt;
     * where X is the generic parameter type of the Array or Iterable
     *
     * @param iteratedType  the type of iterable
     * @return {@link List} of {@link MethodInfo}, never <code>null</code>
     */
    public List<MethodInfo> findIterableGetters(Class iteratedType) {
        if(iterableGettersForType.containsKey(iteratedType)) {
            return iterableGettersForType.get(iteratedType);
        }
        List<MethodInfo> methodInfos = new ArrayList<>();
        String typeSignature = "L" + iteratedType.getName().replace('.', '/') + ";";
        String arrayOfTypeSignature = "()[" + typeSignature;
        try {
            for (MethodInfo methodInfo : propertyGetters()) {
                if (methodInfo.getTypeParameterDescriptor() != null) {
                    if (methodInfo.getTypeParameterDescriptor().equals(typeSignature)) {
                        methodInfos.add(methodInfo);
                    }
                } else {
                    if (methodInfo.getDescriptor().equals(arrayOfTypeSignature)) {
                        methodInfos.add(methodInfo);
                    }
                }
            }

            for (MethodInfo methodInfo : relationshipGetters()) {
                if (methodInfo.getTypeParameterDescriptor() != null) {
                    if (methodInfo.getTypeParameterDescriptor().equals(typeSignature)) {
                        methodInfos.add(methodInfo);
                    } else {
                        if (methodInfo.getDescriptor().equals(arrayOfTypeSignature)) {
                            methodInfos.add(methodInfo);
                        }
                    }
                }
            }
            iterableGettersForType.put(iteratedType, methodInfos);
            return methodInfos;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Finds all getter methods whose parameterised return type is equivalent to Array&lt;X&gt; or assignable from Iterable&lt;X&gt;
     * where X is the generic parameter type of the Array or Iterable and the relationship type this getter is annotated with is "relationshipType"
     * and the direction of the relationship is "relationshipDirection"
     *
     * @param iteratedType          the type of iterable
     * @param relationshipType      the relationship type
     * @param relationshipDirection the relationshipDirection
     * @param strict                if true, does not infer relationship type but looks for it in the @Relationship annotation. Null if missing. If false, infers relationship type from MethodInfo
     * @return {@link List} of {@link MethodInfo}, never <code>null</code>
     */
    public List<MethodInfo> findIterableGetters(Class iteratedType, String relationshipType, String relationshipDirection, boolean strict) {
        List<MethodInfo> methodInfos = new ArrayList<>();
        for(MethodInfo methodInfo : findIterableGetters(iteratedType)) {
            String relationship = strict ? methodInfo.relationshipTypeAnnotation() : methodInfo.relationship();

            if(relationshipType.equals(relationship)) {
                if(((methodInfo.relationshipDirection(Relationship.OUTGOING).equals(Relationship.INCOMING) || methodInfo.relationshipDirection(Relationship.OUTGOING).equals(Relationship.UNDIRECTED)) && relationshipDirection.equals(Relationship.INCOMING))
                        || (relationshipDirection.equals(Relationship.OUTGOING) && !(methodInfo.relationshipDirection(Relationship.OUTGOING).equals(Relationship.INCOMING)))) {
                    methodInfos.add(methodInfo);
                }
            }
        }
        return methodInfos;
    }

    public boolean isTransient() {
        return annotationsInfo.get(Transient.CLASS) != null;
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
    public boolean isSubclassOf(ClassInfo classInfo) {

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
    public Class<?> getType(String typeParameterDescriptor) {
        return ClassUtils.getType(typeParameterDescriptor);
    }

    /**
     * Get the underlying class represented by this ClassInfo
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

    /** Gets the class of the type parameter description of the entity related to this.
     *  The match is done based on the following-
     * 1. Look for a setter explicitly annotated with @Relationship for a type and implied direction
     * 2. Look for a field explicitly annotated with @Relationship for a type and implied direction
     * 3. Look for a setter with name derived from the relationship type for the given direction
     * 4. Look for a field with name derived from the relationship type for the given direction
     *
     * @param relationshipType      the relationship type
     * @param relationshipDirection the relationship direction
     * @return class of the type parameter descriptor or null if it could not be determined
    */
    public Class getTypeParameterDescriptorForRelationship(String relationshipType, String relationshipDirection) {
        final boolean STRICT_MODE = true; //strict mode for matching methods and fields, will only look for explicit annotations
        final boolean INFERRED_MODE = false; //inferred mode for matching methods and fields, will infer the relationship type from the getter/setter/property

        try {
            MethodInfo methodInfo = relationshipSetter(relationshipType, relationshipDirection, STRICT_MODE);
            if (methodInfo != null && methodInfo.getTypeDescriptor() != null) {
                return ClassUtils.getType(methodInfo.getTypeDescriptor());
            }

            FieldInfo fieldInfo = relationshipField(relationshipType, relationshipDirection, STRICT_MODE);
            if (fieldInfo != null && fieldInfo.getTypeDescriptor() != null) {
                return ClassUtils.getType(fieldInfo.getTypeDescriptor());
            }

            if (!relationshipDirection.equals(Relationship.INCOMING)) { //we always expect an annotation for INCOMING
                methodInfo = relationshipSetter(relationshipType, relationshipDirection, INFERRED_MODE);
                if (methodInfo != null && methodInfo.getTypeDescriptor() != null) {
                    return ClassUtils.getType(methodInfo.getTypeDescriptor());
                }
                fieldInfo = relationshipField(relationshipType, relationshipDirection, INFERRED_MODE);
                if (fieldInfo != null && fieldInfo.getTypeDescriptor() != null) {
                    return ClassUtils.getType(fieldInfo.getTypeDescriptor());
                }
            }
        }
        catch (RuntimeException e) {
            LOGGER.debug("Could not get {} class type for relationshipType {} and relationshipDirection {} ", new Object[]{className,relationshipType,relationshipDirection});
        }
        return null;

    }

}


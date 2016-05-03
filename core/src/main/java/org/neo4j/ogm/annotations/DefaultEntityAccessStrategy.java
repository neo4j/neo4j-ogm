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

package org.neo4j.ogm.annotations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.ogm.ClassUtils;
import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.StartNode;
import org.neo4j.ogm.context.DirectedRelationship;
import org.neo4j.ogm.context.DirectedRelationshipForType;
import org.neo4j.ogm.metadata.AnnotationInfo;
import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.metadata.FieldInfo;
import org.neo4j.ogm.metadata.MethodInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of {@link EntityAccessStrategy} that looks up information from {@link ClassInfo} in the following order.
 * <ol>
 * <li>Annotated Method (getter/setter)</li>
 * <li>Annotated Field</li>
 * <li>Plain Method (getter/setter)</li>
 * <li>Plain Field</li>
 * </ol>
 * The rationale is simply that we want annotations, whether on fields or on methods, to always take precedence, and we want to
 * use methods in preference to field access, because in many cases hydrating an object means more than just assigning values to
 * fields.
 *
 * @author Adam George
 * @author Luanne Misquitta
 */
public class DefaultEntityAccessStrategy implements EntityAccessStrategy {

    private final Logger logger = LoggerFactory.getLogger(DefaultEntityAccessStrategy.class);

    //TODO make these LRU caches with configurable size
    private static Map<ClassInfo, Map<DirectedRelationship,RelationalReader>> relationalReaderCache = new HashMap<>();
    private static Map<ClassInfo, Map<DirectedRelationshipForType,RelationalWriter>> relationalWriterCache = new HashMap<>();
    private static Map<ClassInfo, Map<DirectedRelationshipForType,RelationalWriter>> iterableWriterCache = new HashMap<>();
    private static Map<ClassInfo, Map<DirectedRelationshipForType,RelationalReader>> iterableReaderCache = new HashMap<>();
    private static Map<ClassInfo, Map<Class, RelationalWriter>> relationshipEntityWriterCache = new HashMap<>();
    private static Map<ClassInfo, Map<String, EntityAccess>> propertyWriterCache = new HashMap<>();
    private static Map<ClassInfo, Map<String, PropertyReader>> propertyReaderCache = new HashMap<>();
    private static Map<ClassInfo, Collection<PropertyReader>> propertyReaders = new HashMap<>();
    private static Map<ClassInfo, PropertyReader> identityPropertyReaderCache = new HashMap<>();
    private static Map<ClassInfo, Collection<RelationalReader>> relationalReaders = new HashMap<>();

    private final boolean STRICT_MODE = true; //strict mode for matching readers and writers, will only look for explicit annotations
    private final boolean INFERRED_MODE = false; //inferred mode for matching readers and writers, will infer the relationship type from the getter/setter



    /** Used internally to hide differences in object construction from strategy algorithm. */
    private static interface AccessorFactory<T> {
        T makeMethodAccessor(MethodInfo methodInfo);
        T makeFieldAccessor(FieldInfo fieldInfo);
    }

    @Override
    public EntityAccess getPropertyWriter(final ClassInfo classInfo, String propertyName) {
        if(propertyWriterCache.get(classInfo) == null) {
            propertyWriterCache.put(classInfo,new HashMap<String, EntityAccess>());
        }
        EntityAccess writer = propertyWriterCache.get(classInfo).get(propertyName);
        if(writer != null) {
            return writer;
        }

        MethodInfo setterInfo = classInfo.propertySetter(propertyName);
        EntityAccess propertyWriter = determinePropertyAccessor(classInfo, propertyName, setterInfo, new AccessorFactory<EntityAccess>() {
            @Override
            public EntityAccess makeMethodAccessor(MethodInfo methodInfo) {
                return new MethodWriter(classInfo, methodInfo);
            }

            @Override
            public EntityAccess makeFieldAccessor(FieldInfo fieldInfo) {
                return new FieldWriter(classInfo, fieldInfo);
            }
        });
        propertyWriterCache.get(classInfo).put(propertyName, propertyWriter);
        return propertyWriter;
    }

    @Override
    public PropertyReader getPropertyReader(final ClassInfo classInfo, String propertyName) {
        if(propertyReaderCache.get(classInfo) == null) {
            propertyReaderCache.put(classInfo, new HashMap<String, PropertyReader>());
        }
        PropertyReader reader =  propertyReaderCache.get(classInfo).get(propertyName);
        if(reader != null) {
            return reader;
        }

        MethodInfo getterInfo = classInfo.propertyGetter(propertyName);
        PropertyReader propertyReader =  determinePropertyAccessor(classInfo, propertyName, getterInfo, new AccessorFactory<PropertyReader>() {
            @Override
            public PropertyReader makeMethodAccessor(MethodInfo methodInfo) {
                return new MethodReader(classInfo, methodInfo);
            }

            @Override
            public PropertyReader makeFieldAccessor(FieldInfo fieldInfo) {
                return new FieldReader(classInfo, fieldInfo);
            }
        });
        propertyReaderCache.get(classInfo).put(propertyName, propertyReader);
        return propertyReader;
    }

    private <T> T determinePropertyAccessor(ClassInfo classInfo, String propertyName, MethodInfo accessorMethodInfo,
                                            AccessorFactory<T> factory) {
        if (accessorMethodInfo != null) {
            if (!accessorMethodInfo.hasAnnotation(Property.CLASS)) {
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
    public RelationalWriter getRelationalWriter(ClassInfo classInfo, String relationshipType, String relationshipDirection, Object scalarValue) {
        if (relationalWriterCache.get(classInfo) == null) {
            relationalWriterCache.put(classInfo, new HashMap<DirectedRelationshipForType, RelationalWriter>());
        }
        DirectedRelationshipForType directedRelationship = new DirectedRelationshipForType(relationshipType, relationshipDirection, scalarValue.getClass());
        if (relationalWriterCache.get(classInfo).containsKey(directedRelationship)) {
            return relationalWriterCache.get(classInfo).get(directedRelationship);
        }
        // 1st, try to find a scalar method which is explicitly annotated with the relationship type and direction
        for (MethodInfo methodInfo : classInfo.candidateRelationshipSetters(relationshipType, relationshipDirection, STRICT_MODE)) {
            if (methodInfo != null && !methodInfo.getAnnotations().isEmpty()) {

                if (methodInfo.isTypeOf(scalarValue.getClass()) ||
                        methodInfo.isParameterisedTypeOf(scalarValue.getClass()) ||
                        methodInfo.isArrayOf(scalarValue.getClass())) {
                    MethodWriter methodWriter = new MethodWriter(classInfo, methodInfo);
                    relationalWriterCache.get(classInfo).put(directedRelationship, methodWriter);
                    return methodWriter;
                }
            }
        }

        // 2nd, try to find a scalar or vector field explicitly annotated as the neo4j relationship type and direction
        for (FieldInfo fieldInfo : classInfo.candidateRelationshipFields(relationshipType, relationshipDirection, STRICT_MODE)) {
            if (fieldInfo != null && !fieldInfo.getAnnotations().isEmpty()) {
                if (fieldInfo.isTypeOf(scalarValue.getClass()) ||
                        fieldInfo.isParameterisedTypeOf(scalarValue.getClass()) ||
                        fieldInfo.isArrayOf(scalarValue.getClass())) {
                    FieldWriter fieldWriter = new FieldWriter(classInfo, fieldInfo);
                    relationalWriterCache.get(classInfo).put(directedRelationship, fieldWriter);
                    return fieldWriter;
                }
            }
        }

        //If the direction is INCOMING, then the annotation should have been present and we should have found a match already.
        //If it's outgoing, then proceed to find other matches
        if (!relationshipDirection.equals(Relationship.INCOMING)) {
            // 3rd, try to find a scalar method annotated with the relationship type and direction, allowing for implied relationships
            for (MethodInfo methodInfo : classInfo.candidateRelationshipSetters(relationshipType, relationshipDirection, INFERRED_MODE)) {
                if (methodInfo != null && !methodInfo.getAnnotations().isEmpty()) {

                    if (methodInfo.isTypeOf(scalarValue.getClass()) ||
                            methodInfo.isParameterisedTypeOf(scalarValue.getClass()) ||
                            methodInfo.isArrayOf(scalarValue.getClass())) {
                        MethodWriter methodWriter = new MethodWriter(classInfo, methodInfo);
                        relationalWriterCache.get(classInfo).put(directedRelationship, methodWriter);
                        return methodWriter;
                    }
                }
            }

            // 4th, try to find a scalar or vector field annotated as the neo4j relationship type and direction, allowing for implied relationships
            for (FieldInfo fieldInfo : classInfo.candidateRelationshipFields(relationshipType, relationshipDirection, INFERRED_MODE)) {
                if (fieldInfo != null && !fieldInfo.getAnnotations().isEmpty()) {
                    if (fieldInfo.isTypeOf(scalarValue.getClass()) ||
                            fieldInfo.isParameterisedTypeOf(scalarValue.getClass()) ||
                            fieldInfo.isArrayOf(scalarValue.getClass())) {
                        FieldWriter fieldWriter = new FieldWriter(classInfo, fieldInfo);
                        relationalWriterCache.get(classInfo).put(directedRelationship, fieldWriter);
                        return fieldWriter;
                    }
                }
            }

            // 5th, try to find a "setXYZ" method where XYZ is derived from the relationship type
            for (MethodInfo methodInfo : classInfo.candidateRelationshipSetters(relationshipType, relationshipDirection, INFERRED_MODE)) {
                if (methodInfo != null) {
                    if (methodInfo.isTypeOf(scalarValue.getClass()) ||
                            methodInfo.isParameterisedTypeOf(scalarValue.getClass()) ||
                            methodInfo.isArrayOf(scalarValue.getClass())) {
                        MethodWriter methodWriter = new MethodWriter(classInfo, methodInfo);
                        relationalWriterCache.get(classInfo).put(directedRelationship, methodWriter);
                        return methodWriter;
                    }
                }
            }

            // 6th, try to find a "XYZ" field name where XYZ is derived from the relationship type
            for (FieldInfo fieldInfo : classInfo.candidateRelationshipFields(relationshipType, relationshipDirection, INFERRED_MODE)) {
                if (fieldInfo != null) {
                    if (fieldInfo.isTypeOf(scalarValue.getClass()) ||
                            fieldInfo.isParameterisedTypeOf(scalarValue.getClass()) ||
                            fieldInfo.isArrayOf(scalarValue.getClass())) {
                        FieldWriter fieldWriter = new FieldWriter(classInfo, fieldInfo);
                        relationalWriterCache.get(classInfo).put(directedRelationship, fieldWriter);
                        return fieldWriter;
                    }
                }
            }

            // 7th, try to find a unique setter method that takes the parameter
            List<MethodInfo> methodInfos = classInfo.findSetters(scalarValue.getClass());
            if (methodInfos.size() == 1) {
                MethodInfo candidateMethodInfo = methodInfos.iterator().next();
                if (!candidateMethodInfo.relationshipDirection(Relationship.UNDIRECTED).equals(Relationship.INCOMING)) {
                    MethodWriter methodWriter = new MethodWriter(classInfo, candidateMethodInfo);
                    relationalWriterCache.get(classInfo).put(directedRelationship, methodWriter);
                    return methodWriter;
                }
            }

            // 8th, try to find a unique field that has the same type as the parameter
            List<FieldInfo> fieldInfos = classInfo.findFields(scalarValue.getClass());
            if (fieldInfos.size() == 1) {
                FieldInfo candidateFieldInfo = fieldInfos.iterator().next();
                if (!candidateFieldInfo.relationshipDirection(Relationship.UNDIRECTED).equals(Relationship.INCOMING)) {
                    FieldWriter fieldWriter = new FieldWriter(classInfo, candidateFieldInfo);
                    relationalWriterCache.get(classInfo).put(directedRelationship, fieldWriter);
                    return fieldWriter;
                }
            }
        }
        relationalWriterCache.get(classInfo).put(directedRelationship, null);
        return null;
    }

    @Override
    public RelationalReader getRelationalReader(ClassInfo classInfo, String relationshipType, String relationshipDirection) {
        if(relationalReaderCache.get(classInfo) == null) {
            relationalReaderCache.put(classInfo, new HashMap<DirectedRelationship, RelationalReader>());
        }
        DirectedRelationship directedRelationship = new DirectedRelationship(relationshipType,relationshipDirection);
        if(relationalReaderCache.get(classInfo).containsKey(directedRelationship)) {
            return relationalReaderCache.get(classInfo).get(directedRelationship);
        }

        // 1st, try to find a method explicitly annotated with the relationship type and direction.
        MethodInfo methodInfo = classInfo.relationshipGetter(relationshipType, relationshipDirection, STRICT_MODE);
        if (methodInfo != null && !methodInfo.getAnnotations().isEmpty()) {
            MethodReader methodReader = new MethodReader(classInfo, methodInfo);
            relationalReaderCache.get(classInfo).put(directedRelationship, methodReader);
            return methodReader;
        }

        // 2nd, try to find a field explicitly annotated with the neo4j relationship type and direction
        FieldInfo fieldInfo = classInfo.relationshipField(relationshipType, relationshipDirection, STRICT_MODE);
        if (fieldInfo != null && !fieldInfo.getAnnotations().isEmpty()) {
            FieldReader fieldReader =  new FieldReader(classInfo, fieldInfo);
            relationalReaderCache.get(classInfo).put(directedRelationship, fieldReader);
            return fieldReader;
        }

        //If the direction is INCOMING, then the annotation should have been present and we should have found a match already.
        //If it's outgoing, then proceed to find other matches
        if(!relationshipDirection.equals(Relationship.INCOMING)) {

            // 3rd, try to find a method  annotated with the relationship type and direction, allowing for implied relationships
            methodInfo = classInfo.relationshipGetter(relationshipType, relationshipDirection, INFERRED_MODE);
            if (methodInfo != null && !methodInfo.getAnnotations().isEmpty()) {
                MethodReader methodReader = new MethodReader(classInfo, methodInfo);
                relationalReaderCache.get(classInfo).put(directedRelationship, methodReader);
                return methodReader;
            }

            // 4th, try to find a field  annotated with the neo4j relationship type and direction, allowing for implied relationships
            fieldInfo = classInfo.relationshipField(relationshipType, relationshipDirection, INFERRED_MODE);
            if (fieldInfo != null && !fieldInfo.getAnnotations().isEmpty()) {
                FieldReader fieldReader = new FieldReader(classInfo, fieldInfo);
                relationalReaderCache.get(classInfo).put(directedRelationship, fieldReader);
                return fieldReader;
            }


            // 5th, try to find a "getXYZ" method where XYZ is derived from the given relationship type
            if (methodInfo != null) {
                MethodReader methodReader =  new MethodReader(classInfo, methodInfo);
                relationalReaderCache.get(classInfo).put(directedRelationship, methodReader);
                return methodReader;
            }

            // 6th, try to find a "XYZ" field name where XYZ is derived from the relationship type
            if (fieldInfo != null) {
                FieldReader fieldReader =  new FieldReader(classInfo, fieldInfo);
                relationalReaderCache.get(classInfo).put(directedRelationship, fieldReader);
                return fieldReader;
            }
        }
        relationalReaderCache.get(classInfo).put(directedRelationship, null);
        return null;
    }

    @Override
    public Collection<PropertyReader> getPropertyReaders(ClassInfo classInfo) {
        // do we care about "implicit" fields?  i.e., setX/getX with no matching X field
        if(propertyReaders.get(classInfo) != null) {
            return propertyReaders.get(classInfo);
        }
        Collection<PropertyReader> readers = new ArrayList<>();
        for (FieldInfo fieldInfo : classInfo.propertyFields()) {
            MethodInfo getterInfo = classInfo.propertyGetter(fieldInfo.property());
            if (getterInfo != null) { //if we have a getter
                if (getterInfo.hasAnnotation(Property.CLASS) || fieldInfo.getAnnotations().isEmpty()) { //and the getter is annotated with @Property OR the field is not annotated
                    readers.add(new MethodReader(classInfo, getterInfo)); //use the getter
                    continue;
                }
            }
            readers.add(new FieldReader(classInfo, fieldInfo)); //otherwise use the field
        }
        propertyReaders.put(classInfo, readers);
        return readers;
    }

    @Override
    public Collection<RelationalReader> getRelationalReaders(ClassInfo classInfo) {
        if(relationalReaders.get(classInfo) != null) {
            return relationalReaders.get(classInfo);
        }
        Collection<RelationalReader> readers = new ArrayList<>();

        for (FieldInfo fieldInfo : classInfo.relationshipFields()) {
            MethodInfo getterInfo = classInfo.methodsInfo().get(inferGetterName(fieldInfo));
            if (getterInfo != null) {
                if (getterInfo.hasAnnotation(Relationship.CLASS) || !fieldInfo.hasAnnotation(Relationship.CLASS)) {
                    readers.add(new MethodReader(classInfo, getterInfo));
                    continue;
                }
            }
            readers.add(new FieldReader(classInfo, fieldInfo));
        }
        relationalReaders.put(classInfo, readers);
        return readers;
    }

    private static String inferGetterName(FieldInfo fieldInfo) {
        StringBuilder getterNameBuilder = new StringBuilder(fieldInfo.getName());
        getterNameBuilder.setCharAt(0, Character.toUpperCase(fieldInfo.getName().charAt(0)));
        return getterNameBuilder.insert(0, "get").toString();
    }

    @Override
    public RelationalWriter getIterableWriter(ClassInfo classInfo, Class<?> parameterType, String relationshipType, String relationshipDirection) {
        if(iterableWriterCache.get(classInfo) == null) {
            iterableWriterCache.put(classInfo, new HashMap<DirectedRelationshipForType, RelationalWriter>());
        }
        DirectedRelationshipForType directedRelationshipForType = new DirectedRelationshipForType(relationshipType,relationshipDirection, parameterType);
        if(iterableWriterCache.get(classInfo).containsKey(directedRelationshipForType)) {
            return iterableWriterCache.get(classInfo).get(directedRelationshipForType);
        }

        //1st find a method annotated with type and direction
        MethodInfo methodInfo = getIterableSetterMethodInfo(classInfo, parameterType, relationshipType, relationshipDirection, STRICT_MODE);
        if (methodInfo != null) {
            MethodWriter methodWriter =  new MethodWriter(classInfo, methodInfo);
            cacheIterableMethodWriter(classInfo, parameterType, relationshipType, relationshipDirection, directedRelationshipForType, methodInfo, methodWriter);
            return methodWriter;
        }

        //2nd find a field annotated with type and direction
        FieldInfo fieldInfo = getIterableFieldInfo(classInfo, parameterType, relationshipType, relationshipDirection, STRICT_MODE);
        if (fieldInfo != null) {
            FieldWriter fieldWriter = new FieldWriter(classInfo, fieldInfo);
            cacheIterableFieldWriter(classInfo, parameterType, relationshipType, relationshipDirection, directedRelationshipForType, fieldInfo, fieldWriter);
            return fieldWriter;
        }

        //If relationshipDirection=INCOMING, we should have found an annotated field already

        if(!relationshipDirection.equals(Relationship.INCOMING)) {
            //3rd find a method with implied type and direction
            methodInfo = getIterableSetterMethodInfo(classInfo, parameterType, relationshipType, relationshipDirection, INFERRED_MODE);
            if (methodInfo != null) {
                MethodWriter methodWriter = new MethodWriter(classInfo, methodInfo);
                cacheIterableMethodWriter(classInfo, parameterType, relationshipType, relationshipDirection, directedRelationshipForType, methodInfo, methodWriter);
                return methodWriter;
            }

            //4th find a field with implied type and direction
            fieldInfo = getIterableFieldInfo(classInfo, parameterType, relationshipType, relationshipDirection, INFERRED_MODE);
            if (fieldInfo != null) {
                FieldWriter fieldWriter = new FieldWriter(classInfo, fieldInfo);
                cacheIterableFieldWriter(classInfo, parameterType, relationshipType, relationshipDirection, directedRelationshipForType, fieldInfo, fieldWriter);
                return fieldWriter;
            }
        }
        iterableWriterCache.get(classInfo).put(directedRelationshipForType, null);
        return null;
    }



    @Override
    public RelationalReader getIterableReader(ClassInfo classInfo, Class<?> parameterType, String relationshipType, String relationshipDirection) {
        if(iterableReaderCache.get(classInfo) == null) {
            iterableReaderCache.put(classInfo, new HashMap<DirectedRelationshipForType, RelationalReader>());
        }
        DirectedRelationshipForType directedRelationshipForType = new DirectedRelationshipForType(relationshipType,relationshipDirection, parameterType);
        if(iterableReaderCache.get(classInfo).containsKey(directedRelationshipForType)) {
            return iterableReaderCache.get(classInfo).get(directedRelationshipForType);
        }

        //1st find a method annotated with type and direction
        MethodInfo methodInfo = getIterableGetterMethodInfo(classInfo, parameterType, relationshipType, relationshipDirection, STRICT_MODE);
        if (methodInfo != null) {
            MethodReader methodReader =  new MethodReader(classInfo, methodInfo);
            iterableReaderCache.get(classInfo).put(directedRelationshipForType, methodReader);
            return methodReader;
        }

        //2nd find a field annotated with type and direction
        FieldInfo fieldInfo = getIterableFieldInfo(classInfo, parameterType, relationshipType, relationshipDirection, STRICT_MODE);
        if (fieldInfo != null) {
            FieldReader fieldReader = new FieldReader(classInfo, fieldInfo);
            iterableReaderCache.get(classInfo).put(directedRelationshipForType, fieldReader);
            return fieldReader;
        }

        //If relationshipDirection=INCOMING, we should have found an annotated field already

        if(!relationshipDirection.equals(Relationship.INCOMING)) {
            //3rd find a method with implied type and direction
            methodInfo = getIterableGetterMethodInfo(classInfo, parameterType, relationshipType, relationshipDirection, INFERRED_MODE);
            if (methodInfo != null) {
                MethodReader methodReader = new MethodReader(classInfo, methodInfo);
                iterableReaderCache.get(classInfo).put(directedRelationshipForType, methodReader);
                return methodReader;
            }

            //4th find a field with implied type and direction
            fieldInfo = getIterableFieldInfo(classInfo, parameterType, relationshipType, relationshipDirection, INFERRED_MODE);
            if (fieldInfo != null) {
                FieldReader fieldReader = new FieldReader(classInfo, fieldInfo);
                iterableReaderCache.get(classInfo).put(directedRelationshipForType, fieldReader);
                return fieldReader;
            }
        }
        iterableReaderCache.get(classInfo).put(directedRelationshipForType, null);
        return null;
    }

    @Override
    public PropertyReader getIdentityPropertyReader(ClassInfo classInfo) {
        PropertyReader propertyReader = identityPropertyReaderCache.get(classInfo);
        if (propertyReader != null) {
            return propertyReader;
        }
        propertyReader = new FieldReader(classInfo, classInfo.identityField());
        identityPropertyReaderCache.put(classInfo, propertyReader);
        return propertyReader;
    }

    @Override
    public RelationalReader getEndNodeReader(ClassInfo relationshipEntityClassInfo) {
        for (FieldInfo fieldInfo : relationshipEntityClassInfo.relationshipFields()) {
            if (fieldInfo.getAnnotations().get(EndNode.CLASS) != null) {
                return new FieldReader(relationshipEntityClassInfo, fieldInfo);
            }
        }
        logger.warn("Failed to find an @EndNode on {}", relationshipEntityClassInfo);
        return null;
    }

    @Override
    public RelationalReader getStartNodeReader(ClassInfo relationshipEntityClassInfo) {
        for (FieldInfo fieldInfo : relationshipEntityClassInfo.relationshipFields()) {
            if (fieldInfo.getAnnotations().get(StartNode.CLASS) != null) {
                return new FieldReader(relationshipEntityClassInfo, fieldInfo);
            }
        }
        logger.warn("Failed to find an @StartNode on {}", relationshipEntityClassInfo);
        return null;
    }

    @Override
    public RelationalWriter getRelationalEntityWriter(ClassInfo classInfo, Class entityAnnotation) {
        if (entityAnnotation.getName() == null) {
            throw new RuntimeException(entityAnnotation.getSimpleName() + " is not defined on " + classInfo.name());
        }

        if (relationshipEntityWriterCache.get(classInfo) == null) {
            relationshipEntityWriterCache.put(classInfo, new HashMap<Class, RelationalWriter>());
        }
        if (relationshipEntityWriterCache.get(classInfo).containsKey(entityAnnotation)) {
            return relationshipEntityWriterCache.get(classInfo).get(entityAnnotation);
        }

        //Find annotated field
        FieldInfo field = null;
        for(FieldInfo fieldInfo : classInfo.relationshipFields()) {
            if(fieldInfo.getAnnotations().get(entityAnnotation.getName()) != null) {
                field = fieldInfo;
                break;
            }
        }
        if(field != null) {
            String setter = "set" + field.getName().substring(0,1).toUpperCase() + field.getName().substring(1);
            //Preferably find a setter for the field
            for(MethodInfo methodInfo : classInfo.relationshipSetters()) {
                if (methodInfo.getName().equals(setter)) {
                    MethodWriter methodWriter = new MethodWriter(classInfo, methodInfo);
                    relationshipEntityWriterCache.get(classInfo).put(entityAnnotation, methodWriter);
                    return methodWriter;
                }

            }
            //Otherwise use the field
            FieldWriter fieldWriter =  new FieldWriter(classInfo,field);
            relationshipEntityWriterCache.get(classInfo).put(entityAnnotation,fieldWriter);
            return fieldWriter;
        }
        relationshipEntityWriterCache.get(classInfo).put(entityAnnotation,null);
        return null;
    }

    private MethodInfo getIterableSetterMethodInfo(ClassInfo classInfo, Class<?> parameterType, String relationshipType, String relationshipDirection, boolean strict) {
        List<MethodInfo> methodInfos = classInfo.findIterableSetters(parameterType, relationshipType, relationshipDirection, strict);
        if (methodInfos.size() == 0) {
            if(!strict) {
                methodInfos = classInfo.findIterableSetters(parameterType);
            }
        }
        if (methodInfos.size() == 1) {
            MethodInfo candidateMethodInfo = methodInfos.iterator().next();
            //If the method is annotated, then the relationship type must match
            if (candidateMethodInfo.hasAnnotation(Relationship.CLASS)) {
                AnnotationInfo relationshipAnnotation = candidateMethodInfo.getAnnotations().get(Relationship.CLASS);
                if(!relationshipType.equals(relationshipAnnotation.get(Relationship.TYPE, null))) {
                    return null;
                }
            }
            //If the relationshipDirection is incoming and the candidateMethodInfo is also incoming or undirected
            if(relationshipDirection.equals(Relationship.INCOMING) &&
                    (candidateMethodInfo.relationshipDirection(Relationship.OUTGOING).equals(Relationship.INCOMING)) ||
                    (candidateMethodInfo.relationshipDirection(Relationship.OUTGOING).equals(Relationship.UNDIRECTED))) {
                return candidateMethodInfo;
            }
            //If the relationshipDirection is not incoming and the candidateMethodInfo is not incoming
            if(!relationshipDirection.equals(Relationship.INCOMING) && !candidateMethodInfo.relationshipDirection(Relationship.OUTGOING).equals(Relationship.INCOMING)) {
                return candidateMethodInfo;
            }
        }

        if (methodInfos.size() > 0) {
            logger.warn("Cannot map iterable of {} to instance of {}. More than one potential matching setter found.",
                    parameterType, classInfo.name());
        }

        return null;
    }

    private MethodInfo getIterableGetterMethodInfo(ClassInfo classInfo, Class<?> parameterType, String relationshipType, String relationshipDirection, boolean strict) {
        List<MethodInfo> methodInfos = classInfo.findIterableGetters(parameterType, relationshipType, relationshipDirection, strict);
        if(methodInfos.size() == 0) {
            if(!strict) {
                methodInfos = classInfo.findIterableGetters(parameterType);
            }
        }
        if (methodInfos.size() == 1) {
            MethodInfo candidateMethodInfo = methodInfos.iterator().next();
            if (candidateMethodInfo.hasAnnotation(Relationship.CLASS)) {
                AnnotationInfo relationshipAnnotation = candidateMethodInfo.getAnnotations().get(Relationship.CLASS);
                if(!relationshipType.equals(relationshipAnnotation.get(Relationship.TYPE, null))) {
                    return null;
                }
            }
            //If the relationshipDirection is incoming and the candidateMethodInfo is also incoming or undirected
            if(relationshipDirection.equals(Relationship.INCOMING) &&
                    (candidateMethodInfo.relationshipDirection(Relationship.OUTGOING).equals(Relationship.INCOMING)) ||
                    (candidateMethodInfo.relationshipDirection(Relationship.OUTGOING).equals(Relationship.UNDIRECTED))) {
                return candidateMethodInfo;
            }
            //If the relationshipDirection is not incoming and the candidateMethodInfo is not incoming
            if(!relationshipDirection.equals(Relationship.INCOMING) && !candidateMethodInfo.relationshipDirection(Relationship.OUTGOING).equals(Relationship.INCOMING)) {
                return candidateMethodInfo;
            }
        }

        if (methodInfos.size() > 0) {
            logger.warn("Cannot map iterable of {} to instance of {}.  More than one potential matching getter found.",
                    parameterType, classInfo.name());
        }
        return null;
    }

    private FieldInfo getIterableFieldInfo(ClassInfo classInfo, Class<?> parameterType, String relationshipType, String relationshipDirection, boolean strict) {
        List<FieldInfo> fieldInfos = classInfo.findIterableFields(parameterType, relationshipType, relationshipDirection, strict);
        if(fieldInfos.size() == 0) {
            if(!strict) {
                fieldInfos = classInfo.findIterableFields(parameterType);
            }
        }
        if (fieldInfos.size() == 1) {
            FieldInfo candidateFieldInfo = fieldInfos.iterator().next();
            if (candidateFieldInfo.hasAnnotation(Relationship.CLASS)) {
                AnnotationInfo relationshipAnnotation = candidateFieldInfo.getAnnotations().get(Relationship.CLASS);
                if(!relationshipType.equals(relationshipAnnotation.get(Relationship.TYPE, null))) {
                    return null;
                }
            }
            //If the relationshipDirection is incoming and the candidateFieldInfo is also incoming or undirected
            if(relationshipDirection.equals(Relationship.INCOMING) &&
                    (candidateFieldInfo.relationshipDirection(Relationship.OUTGOING).equals(Relationship.INCOMING)) ||
                    (candidateFieldInfo.relationshipDirection(Relationship.OUTGOING).equals(Relationship.UNDIRECTED))) {
                return candidateFieldInfo;
            }
            //If the relationshipDirection is not incoming and the candidateFieldInfo is not incoming
            if(!relationshipDirection.equals(Relationship.INCOMING) && !candidateFieldInfo.relationshipDirection(Relationship.OUTGOING).equals(Relationship.INCOMING)) {
                return candidateFieldInfo;
            }
        }

        if (fieldInfos.size() > 0) {
            logger.warn("Cannot map iterable of {} to instance of {}. More than one potential matching field found.",
                    parameterType, classInfo.name());
        }

        return null;
    }

    private void cacheIterableFieldWriter(ClassInfo classInfo, Class<?> parameterType, String relationshipType, String relationshipDirection, DirectedRelationshipForType directedRelationshipForType, FieldInfo fieldInfo, FieldWriter fieldWriter) {
        if(fieldInfo.isParameterisedTypeOf(parameterType)) {
            //Cache the writer for the superclass used in the type param
            directedRelationshipForType = new DirectedRelationshipForType(relationshipType,relationshipDirection, ClassUtils.getType(fieldInfo.getTypeParameterDescriptor()));
        }
        iterableWriterCache.get(classInfo).put(directedRelationshipForType, fieldWriter);
    }

    private void cacheIterableMethodWriter(ClassInfo classInfo, Class<?> parameterType, String relationshipType, String relationshipDirection, DirectedRelationshipForType directedRelationshipForType, MethodInfo methodInfo, MethodWriter methodWriter) {
        if(methodInfo.isParameterisedTypeOf(parameterType)) {
            //Cache the writer for the superclass used in the type param
            directedRelationshipForType = new DirectedRelationshipForType(relationshipType, relationshipDirection, ClassUtils.getType(methodInfo.getTypeParameterDescriptor()));
        }
        iterableWriterCache.get(classInfo).put(directedRelationshipForType, methodWriter);
    }

}
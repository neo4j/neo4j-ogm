/*
 * Copyright (c) 2002-2022 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neo4j.ogm.metadata.reflect;

import static org.neo4j.ogm.support.ClassUtils.*;

import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.Relationship.Direction;
import org.neo4j.ogm.context.DirectedRelationship;
import org.neo4j.ogm.context.DirectedRelationshipForType;
import org.neo4j.ogm.metadata.AnnotationInfo;
import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.metadata.DescriptorMappings;
import org.neo4j.ogm.metadata.FieldInfo;
import org.neo4j.ogm.session.Utils;
import org.neo4j.ogm.support.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Determines how entities should be accessed in both reading and writing scenarios by looking up information from
 * {@link ClassInfo} in the following order.
 * <ol>
 * <li>Annotated Field</li>
 * <li>Plain Field</li>
 * </ol>
 * The rationale is simply that we want annotations, whether on fields or on methods, to always take precedence, and we want to
 * use methods in preference to field access, because in many cases hydrating an object means more than just assigning values to
 * fields.
 *
 * @author Adam George
 * @author Luanne Misquitta
 * @author Michael J. Simons
 */
public class EntityAccessManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntityAccessManager.class);

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static Object merge(Class<?> parameterType, Object newValues, Object[] currentValues, Class elementType) {
        if (currentValues != null) {
            return merge(parameterType, newValues, Arrays.asList(currentValues), elementType);
        } else {
            return merge(parameterType, newValues, new ArrayList(), elementType);
        }
    }

    /**
     * Merges the contents of <em>collection</em> with <em>hydrated</em> ensuring no duplicates and returns the result as an
     * instance of the given parameter type.
     *
     * @param collectionType The type of Iterable or array to return
     * @param newValues     The objects to merge into a collection of the given parameter type, which may not necessarily be of a
     *                      type assignable from <em>collectionType</em> already
     * @param currentValues The Iterable to merge into, which may be <code>null</code> if a new collection needs creating
     * @param elementType   The type of the element in the array or collection (After conversion has been applied)
     * @return The result of the merge, as an instance of the specified parameter type
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static Object merge(Class<?> collectionType, Object newValues, Collection currentValues, Class elementType) {

        boolean targetIsArray = collectionType.isArray();

        // While we expect newValues to be an iterable, there are a couple of exceptions
        if (newValues != null) {
            boolean needsByteArray = targetIsArray && (collectionType.getComponentType() == byte.class || collectionType.getComponentType() == Byte.class);
            // 1. This happens only in case of the HTTP transport, that insists of representing primitive byte arrays as
            //    base64 encoded string
            if (needsByteArray && newValues instanceof String) {
                newValues = CollectionUtils.iterableOf(Base64.getDecoder().decode((String) newValues));
            } else {
                // 2. A primitive array cannot be cast directly to Iterable
                newValues = boxPrimitiveArray(newValues);

                // 3. A char[] may come in as a String or an array of String[]
                newValues = stringToCharacterIterable(newValues, collectionType, elementType);
            }
        }

        // Array needs a different coercion and special treatment to properly reassign the existing collection
        if (targetIsArray) {

            Class<?> componentType = collectionType.getComponentType();

            // Merge and coerce is done directly on the component type
            Collection<Object> mergedValues = mergeAndCoerce(componentType, (Iterable) newValues, currentValues);

            Object targetArray = Array.newInstance(componentType, mergedValues.size());
            AtomicInteger cnt = new AtomicInteger(0);
            mergedValues.forEach(object -> Array.set(targetArray, cnt.getAndIncrement(), object));
            return targetArray;
        }

        // create the desired type of collection and use it for the merge
        Collection newCollection = createTargetCollection(collectionType,
            mergeAndCoerce(elementType, (Iterable) newValues, currentValues));
        if (newCollection != null) {
            return newCollection;
        }

        // hydrated is unusable at this point so we can just set the other collection if it's compatible
        if (newValues != null && collectionType.isAssignableFrom(newValues.getClass())) {
            return newValues;
        }

        throw new RuntimeException("Unsupported: " + collectionType.getName());
    }

    /**
     * Merges and coerces two collections. The elements in right have precedence over the left, so the right collection
     * dominates the order. That is mostly due to the fact that the call stack leading here when completly hydrating new
     * collections from queries puts the freshly hydrated elements into the right.
     *
     * @param targetElementType
     * @param left
     * @param right
     * @return The merged collection with no duplicates
     */
    private static Collection<Object> mergeAndCoerce(Class targetElementType, Iterable<Object> left, Iterable<Object> right) {

        // Turn each collection into the correct target type
        Collection<Object> coercedLeft = coerceCollection(targetElementType, left);
        Collection<Object> coercedRight = coerceCollection(targetElementType, right);

        // Remove duplicates
        coercedRight.removeAll(coercedLeft);

        // Finally create the union without duplicates
        Collection<Object> result = new ArrayList<>(coercedLeft.size() + coercedRight.size());
        result.addAll(coercedRight);
        result.addAll(coercedLeft);
        return result;
    }

    private static Collection<Object> coerceCollection(Class<Object> targetElementType, Iterable<Object> source) {

        if (source == null) {
            return Collections.emptyList();
        } else {
            Iterator<Object> it = source.iterator();
            if (!it.hasNext()) {
                return Collections.emptyList();
            } else {
                Collection<Object> target = new ArrayList<>();
                it.forEachRemaining(v -> target.add(Utils.coerceTypes(targetElementType, v)));
                return target;
            }
        }
    }

    private static Collection<?> createTargetCollection(Class<?> collectionType, Collection collection) {
        if (Vector.class.isAssignableFrom(collectionType)) {
            return collection instanceof Vector ? collection : new Vector<>(collection);
        }
        if (List.class.isAssignableFrom(collectionType)) {
            return collection instanceof ArrayList ? collection : new ArrayList<>(collection);
        }
        if (SortedSet.class.isAssignableFrom(collectionType)) {
            return collection instanceof TreeSet ? collection : new TreeSet<>(collection);
        }
        if (Set.class.isAssignableFrom(collectionType)) {
            return collection instanceof HashSet ? collection : new HashSet<>(collection);
        }
        return null;
    }

    /**
     * Convert to an Iterable of Character if the value is a String
     *
     * @param value the object, which may be a String, String[], Collection of String
     * @return List of Character if the value is a String, or the value unchanged
     */
    private static Object stringToCharacterIterable(Object value, Class parameterType, Class elementType) {
        boolean convertCharacters = false;
        if (value instanceof String) {
            char[] chars = ((String) value).toCharArray();
            List<Character> characters = new ArrayList<>(chars.length);
            for (char c : chars) {
                characters.add(c);
            }
            return characters;
        }

        if (parameterType.getComponentType() != null) {
            if (parameterType.getComponentType().equals(Character.class)) {
                convertCharacters = true;
            }
        } else {
            if (elementType == Character.class || elementType == char.class) {
                convertCharacters = true;
            }
        }

        if (value.getClass().isArray() && convertCharacters && value.getClass().getComponentType()
            .equals(String.class)) {
            String[] strings = (String[]) value;
            List<Character> characters = new ArrayList<>(strings.length);
            for (String s : strings) {
                characters.add(s.toCharArray()[0]);
            }
            return characters;
        }

        if (value.getClass().isArray() && (elementType == String.class || isEnum(elementType))) {
            String[] strings = (String[]) value;
            return Arrays.asList(strings);
        }
        return value;
    }

    private static Object boxPrimitiveArray(Object value) {
        if (value.getClass().isArray() && value.getClass().getComponentType().isPrimitive()) {
            switch (value.getClass().getComponentType().toString()) {
                case "int":
                    int[] intArray = (int[]) value;
                    List<Integer> boxedIntList = new ArrayList<>(intArray.length);
                    for (int i : intArray) {
                        boxedIntList.add(i);
                    }
                    return boxedIntList;

                case "float":
                    float[] floatArray = (float[]) value;
                    List<Float> boxedFloatList = new ArrayList<>(floatArray.length);
                    for (float f : floatArray) {
                        boxedFloatList.add(f);
                    }
                    return boxedFloatList;

                case "long":
                    long[] longArray = (long[]) value;
                    List<Long> boxedLongList = new ArrayList<>(longArray.length);
                    for (long l : longArray) {
                        boxedLongList.add(l);
                    }
                    return boxedLongList;

                case "double":
                    double[] dblArray = (double[]) value;
                    List<Double> boxedDoubleList = new ArrayList<>(dblArray.length);
                    for (double d : dblArray) {
                        boxedDoubleList.add(d);
                    }
                    return boxedDoubleList;

                case "boolean":
                    boolean[] booleanArray = (boolean[]) value;
                    List<Boolean> boxedBooleanList = new ArrayList<>(booleanArray.length);
                    for (boolean b : booleanArray) {
                        boxedBooleanList.add(b);
                    }
                    return boxedBooleanList;

                case "char":
                    char[] charArray = (char[]) value;
                    List<Character> boxedCharList = new ArrayList<>(charArray.length);
                    for (char c : charArray) {
                        boxedCharList.add(c);
                    }
                    return boxedCharList;
            }
        }
        return value;
    }

    private static Map<ClassInfo, Map<DirectedRelationship, FieldInfo>> relationalReaderCache = new ConcurrentHashMap<>();
    private static Map<ClassInfo, Map<DirectedRelationshipForType, FieldInfo>> relationalWriterCache = new ConcurrentHashMap<>();
    private static Map<ClassInfo, Map<DirectedRelationshipForType, FieldInfo>> iterableWriterCache = new ConcurrentHashMap<>();

    private static final boolean STRICT_MODE = true; //strict mode for matching readers and writers, will only look for explicit annotations
    private static final boolean INFERRED_MODE = false; //inferred mode for matching readers and writers, will infer the relationship type from the getter/setter

    /**
     * Returns a FieldWriter for a scalar value represented as a relationship in the graph (i.e. not a primitive property)
     *
     * @param classInfo             the ClassInfo (or a superclass thereof) declaring the relationship
     * @param relationshipType      the name of the relationship as it is in the graph
     * @param relationshipDirection the direction of the relationship as it is in the graph
     * @param scalarValue           an Object whose class the relationship is defined for
     * @return a valid FieldWriter or null if none is found
     */
    public static FieldInfo getRelationalWriter(ClassInfo classInfo, String relationshipType,
        Direction relationshipDirection, Object scalarValue) {
        return getRelationalWriter(classInfo, relationshipType, relationshipDirection, scalarValue.getClass());
    }

    /**
     * Returns a FieldWriter for a scalar type on a ClassInfo that is not a primitive graph property
     *
     * @param classInfo             the ClassInfo (or a superclass thereof) declaring the relationship
     * @param relationshipType      the name of the relationship as it is in the graph
     * @param relationshipDirection the direction of the relationship as it is in the graph
     * @param objectType            the class the relationship is defined for
     * @return a valid FieldWriter or null if none is found
     */
    public static FieldInfo getRelationalWriter(ClassInfo classInfo, String relationshipType,
        Direction relationshipDirection, Class<?> objectType) {

        final DirectedRelationshipForType directedRelationship = new DirectedRelationshipForType(relationshipType,
            relationshipDirection, objectType);
        final Map<DirectedRelationshipForType, FieldInfo> typeFieldInfoMap = relationalWriterCache
            .computeIfAbsent(classInfo, key -> new ConcurrentHashMap<>());

        if (typeFieldInfoMap.containsKey(directedRelationship)) {
            return typeFieldInfoMap.get(directedRelationship);
        }

        while (classInfo != null) {

            // 1st, try to find a scalar or vector field explicitly annotated as the neo4j relationship type and direction
            for (FieldInfo fieldInfo : classInfo
                .candidateRelationshipFields(relationshipType, relationshipDirection, STRICT_MODE)) {
                if (fieldInfo != null && !fieldInfo.getAnnotations().isEmpty()) {
                    if (fieldInfo.isTypeOf(objectType) ||
                        fieldInfo.isParameterisedTypeOf(objectType) ||
                        fieldInfo.isArrayOf(objectType)) {
                        typeFieldInfoMap.put(directedRelationship, fieldInfo);
                        return fieldInfo;
                    }
                }
            }

            //If the direction is INCOMING, then the annotation should have been present and we should have found a match already.
            //If it's outgoing, then proceed to find other matches
            if (relationshipDirection != Direction.INCOMING) {

                // 2nd, try to find a scalar or vector field annotated as the neo4j relationship type and direction, allowing for implied relationships
                final Set<FieldInfo> candidateRelationshipFields = classInfo
                    .candidateRelationshipFields(relationshipType, relationshipDirection, INFERRED_MODE);
                for (FieldInfo fieldInfo : candidateRelationshipFields) {
                    if (fieldInfo != null && !fieldInfo.getAnnotations().isEmpty()) {
                        if (fieldInfo.isTypeOf(objectType) ||
                            fieldInfo.isParameterisedTypeOf(objectType) ||
                            fieldInfo.isArrayOf(objectType)) {
                            typeFieldInfoMap.put(directedRelationship, fieldInfo);
                            return fieldInfo;
                        }
                    }
                }

                // 3rd, try to find a "XYZ" field name where XYZ is derived from the relationship type
                for (FieldInfo fieldInfo : candidateRelationshipFields) {
                    if (fieldInfo != null) {
                        if (fieldInfo.isTypeOf(objectType) ||
                            fieldInfo.isParameterisedTypeOf(objectType) ||
                            fieldInfo.isArrayOf(objectType)) {
                            typeFieldInfoMap.put(directedRelationship, fieldInfo);
                            return fieldInfo;
                        }
                    }
                }

                // 4th, try to find a unique field that has the same type as the parameter
                List<FieldInfo> fieldInfos = classInfo.findFields(objectType);
                if (fieldInfos.size() == 1) {
                    FieldInfo candidateField = fieldInfos.iterator().next();

                    if (candidateField.relationshipDirectionOrDefault(Direction.UNDIRECTED) != Direction.INCOMING) {

                        if (candidateField.relationshipTypeAnnotation() == null) {
                            typeFieldInfoMap.put(directedRelationship, candidateField);
                            return candidateField;
                        }
                    }
                }
            }
            // walk up the object hierarchy
            classInfo = classInfo.directSuperclass();
        }
        return null;
    }

    /**
     * Returns a FieldInfo for a scalar type definition on a ClassInfo that is not a primitive graph property
     *
     * @param classInfo             A ClassInfo declaring the type definition
     * @param relationshipType      The name of the relationship in the graph
     * @param relationshipDirection The direction of the relationship in the graph
     * @return A FieldInfo or null if none exists
     */
    public static FieldInfo getRelationalReader(ClassInfo classInfo, String relationshipType,
        Direction relationshipDirection) {

        final DirectedRelationship directedRelationship = new DirectedRelationship(relationshipType,
            relationshipDirection);
        final Map<DirectedRelationship, FieldInfo> relationshipFieldInfoMap = relationalReaderCache
            .computeIfAbsent(classInfo, key -> new ConcurrentHashMap<>());

        if (relationshipFieldInfoMap.containsKey(directedRelationship)) {
            return relationshipFieldInfoMap.get(directedRelationship);
        }

        while (classInfo != null) {
            // 1st, try to find a field explicitly annotated with the neo4j relationship type and direction
            FieldInfo fieldInfo = classInfo.relationshipField(relationshipType, relationshipDirection, STRICT_MODE);
            if (fieldInfo != null && !fieldInfo.getAnnotations().isEmpty()) {
                relationshipFieldInfoMap.put(directedRelationship, fieldInfo);
                return fieldInfo;
            }

            //If the direction is INCOMING, then the annotation should have been present and we should have found a match already.
            //If it's outgoing, then proceed to find other matches
            if (relationshipDirection != Direction.INCOMING) {

                // 3rd, try to find a field  annotated with the neo4j relationship type and direction, allowing for implied relationships
                fieldInfo = classInfo.relationshipField(relationshipType, relationshipDirection, INFERRED_MODE);
                if (fieldInfo != null && !fieldInfo.getAnnotations().isEmpty()) {
                    relationshipFieldInfoMap.put(directedRelationship, fieldInfo);
                    return fieldInfo;
                }

                // 4th, try to find a "XYZ" field name where XYZ is derived from the relationship type
                if (fieldInfo != null) {
                    relationshipFieldInfoMap.put(directedRelationship, fieldInfo);
                    return fieldInfo;
                }
            }
            classInfo = classInfo.directSuperclass();
        }
        return null;
    }

    /**
     * Returns an FieldWriter for an iterable of a non-primitive scalar type defined by a ClassInfo
     *
     * @param classInfo             the ClassInfo (or a superclass thereof) declaring the iterable relationship
     * @param relationshipType      the name of the relationship as it is in the graph
     * @param relationshipDirection the direction of the relationship as it is in the graph
     * @param parameterType         the type that will be iterated over
     * @return a valid FieldWriter or null if none is found
     */
    public static FieldInfo getIterableField(ClassInfo classInfo, Class<?> parameterType, String relationshipType,
        Direction relationshipDirection) {

        final ClassInfo lookupClassInfo = classInfo;
        final DirectedRelationshipForType directedRelationshipForType = new DirectedRelationshipForType(
            relationshipType,
            relationshipDirection, parameterType);
        final Map<DirectedRelationshipForType, FieldInfo> typeFieldInfoMap = iterableWriterCache
            .computeIfAbsent(lookupClassInfo, key -> new ConcurrentHashMap<>());

        if (typeFieldInfoMap.containsKey(directedRelationshipForType)) {
            return typeFieldInfoMap.get(directedRelationshipForType);
        }

        while (classInfo != null) {

            //1st find a field annotated with type and direction
            FieldInfo fieldInfo = getIterableFieldInfo(classInfo, parameterType, relationshipType,
                relationshipDirection, STRICT_MODE);
            if (fieldInfo != null) {
                cacheIterableFieldWriter(lookupClassInfo, parameterType, relationshipType, relationshipDirection,
                    directedRelationshipForType, fieldInfo, fieldInfo);
                return fieldInfo;
            }

            // If relationshipDirection=INCOMING, we should have found an annotated field already

            if (relationshipDirection != Direction.INCOMING) {

                //3rd, find a field with implied type and direction
                fieldInfo = getIterableFieldInfo(classInfo, parameterType, relationshipType, relationshipDirection,
                    INFERRED_MODE);
                if (fieldInfo != null) {
                    cacheIterableFieldWriter(lookupClassInfo, parameterType, relationshipType, relationshipDirection,
                        directedRelationshipForType, fieldInfo, fieldInfo);
                    return fieldInfo;
                }
            }
            classInfo = classInfo.directSuperclass();
        }
        return null;
    }

    // TODO: lookup via classinfo hierarchy
    private static FieldInfo getIterableFieldInfo(ClassInfo classInfo, Class<?> parameterType, String relationshipType,
        Direction relationshipDirection, boolean strict) {
            List<FieldInfo> fieldInfos = classInfo
                .findIterableFields(parameterType, relationshipType, relationshipDirection, strict);
            if (fieldInfos.size() == 0) {
                if (!strict) {
                    fieldInfos = classInfo.findIterableFields(parameterType);
                }
            }
            if (fieldInfos.size() == 1) {
                FieldInfo candidateFieldInfo = fieldInfos.iterator().next();
                if (candidateFieldInfo.hasAnnotation(Relationship.class)) {
                    AnnotationInfo relationshipAnnotation = candidateFieldInfo.getAnnotations().get(Relationship.class);
                    if (!relationshipType.equals(relationshipAnnotation.get(Relationship.TYPE, null))) {
                        return null;
                    }
                }
                // If the relationshipDirection is incoming and the candidateFieldInfo is also incoming or undirected
                if (relationshipDirection == Direction.INCOMING &&
                    candidateFieldInfo.relationshipDirectionOrDefault(Direction.OUTGOING) == Direction.INCOMING ||
                    candidateFieldInfo.relationshipDirectionOrDefault(Direction.OUTGOING) == Direction.UNDIRECTED) {
                    return candidateFieldInfo;
                }
                // If the relationshipDirection is not incoming and the candidateFieldInfo is not incoming
                if (relationshipDirection != Direction.INCOMING && candidateFieldInfo
                    .relationshipDirectionOrDefault(Direction.OUTGOING) != Direction.INCOMING) {
                    return candidateFieldInfo;
                }
            }

            if (fieldInfos.size() > 0) {
                LOGGER.warn("Cannot map iterable of {} to instance of {}. More than one potential matching field found.",
                    parameterType, classInfo.name());
            }

            return null;
    }

    private static void cacheIterableFieldWriter(ClassInfo classInfo, Class<?> parameterType, String relationshipType,
        Direction relationshipDirection, DirectedRelationshipForType directedRelationshipForType, FieldInfo fieldInfo,
        FieldInfo fieldAccessor) {
        if (fieldInfo.isParameterisedTypeOf(parameterType)) {
            // Cache the writer for the superclass used in the type param
            directedRelationshipForType = new DirectedRelationshipForType(relationshipType, relationshipDirection,
                DescriptorMappings.getType(fieldInfo.getTypeDescriptor()));
        }
        iterableWriterCache.get(classInfo).put(directedRelationshipForType, fieldAccessor);
    }
}

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
package org.neo4j.ogm.metadata;

import static java.util.stream.Collectors.*;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import org.neo4j.ogm.annotation.*;
import org.neo4j.ogm.annotation.Relationship.Direction;
import org.neo4j.ogm.autoindex.AutoIndexManager;
import org.neo4j.ogm.driver.TypeSystem;
import org.neo4j.ogm.exception.core.InvalidPropertyFieldException;
import org.neo4j.ogm.exception.core.MappingException;
import org.neo4j.ogm.exception.core.MetadataException;
import org.neo4j.ogm.id.IdStrategy;
import org.neo4j.ogm.id.InternalIdStrategy;
import org.neo4j.ogm.id.UuidStrategy;
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
 * @author Michael J. Simons
 * @author Torsten Kuhnhenne
 * @author Nicolas Labrot
 */
public class ClassInfo {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassInfo.class);

    private final List<ClassInfo> directSubclasses = new ArrayList<>();
    private volatile Set<ClassInfo> allSubclasses;
    private final List<ClassInfo> directInterfaces = new ArrayList<>();
    private final List<ClassInfo> directImplementingClasses = new ArrayList<>();
    /**
     * Indirect super classes will only be filled for Kotlin classes that make use of Kotlin's
     * <a href="https://kotlinlang.org/docs/reference/delegation.html">"Implementation by Delegation"</a>. Find a
     * more in depth of the inner workings
     * <a href="https://medium.com/til-kotlin/how-kotlins-class-delegation-works-and-di-101-de9887b95d3e">here</a>
     * (and feel free to replace the above medium link with a real spec doc).
     */
    private final List<ClassInfo> indirectSuperClasses = new ArrayList<>();
    private final String className;

    private final boolean isInterface;
    private final boolean isAbstract;
    private final boolean isEnum;

    private ClassInfo directSuperclass;
    private String directSuperclassName;
    private String neo4jName;

    private final FieldsInfo fieldsInfo;
    private final MethodsInfo methodsInfo;
    private final AnnotationsInfo annotationsInfo;
    private final InterfacesInfo interfacesInfo;
    private final Class<?> cls;

    private final Map<Class, List<FieldInfo>> iterableFieldsForType = new HashMap<>();
    private final Map<FieldInfo, Field> fieldInfoFields = new ConcurrentHashMap<>();

    private volatile Map<String, FieldInfo> propertyFields;
    private volatile Map<String, FieldInfo> indexFields;
    private volatile Collection<FieldInfo> requiredFields;
    private volatile Collection<CompositeIndex> compositeIndexes;
    private volatile Optional<FieldInfo> identityField;
    private volatile Optional<FieldInfo> versionField;
    private volatile Optional<FieldInfo> primaryIndexField;
    private volatile FieldInfo labelField = null;
    private volatile boolean labelFieldMapped = false;
    private volatile Optional<MethodInfo> postLoadMethod;
    private volatile Collection<String> staticLabels;
    private volatile Set<FieldInfo> relationshipFields;
    private volatile Optional<FieldInfo> endNodeReader;
    private volatile Optional<FieldInfo> startNodeReader;

    private Class<? extends IdStrategy> idStrategyClass;
    private IdStrategy idStrategy;

    public ClassInfo(Class<?> cls, TypeSystem typeSystem) {
        this(cls, null, typeSystem);
    }

    /**
     * @param cls        The type of this class
     * @param parent     Will be filled if containing class is a Kotlin class and this class is the type of a Kotlin delegate.
     * @param typeSystem The typesystem in use
     */
    private ClassInfo(Class<?> cls, Field parent, TypeSystem typeSystem) {
        this.cls = cls;
        final int modifiers = cls.getModifiers();
        this.isInterface = Modifier.isInterface(modifiers);
        this.isAbstract = Modifier.isAbstract(modifiers);
        this.isEnum = org.neo4j.ogm.support.ClassUtils.isEnum(cls);
        this.className = cls.getName();

        if (cls.getSuperclass() != null) {
            this.directSuperclassName = cls.getSuperclass().getName();
        }
        this.interfacesInfo = new InterfacesInfo(cls);
        this.fieldsInfo = new FieldsInfo(this, cls, parent, typeSystem);
        this.methodsInfo = new MethodsInfo(cls, parent);
        this.annotationsInfo = new AnnotationsInfo(cls);

        if (isRelationshipEntity() && labelFieldOrNull() != null) {
            throw new MappingException(
                String.format("'%s' is a relationship entity. The @Labels annotation can't be applied to " +
                    "relationship entities.", name()));
        }

        for (FieldInfo fieldInfo : fieldsInfo().fields()) {
            if (fieldInfo.hasAnnotation(Property.class) && fieldInfo.hasCompositeConverter()) {
                throw new MappingException(
                    String.format("'%s' has both @Convert and @Property annotations applied to the field '%s'",
                        name(), fieldInfo.getName()));
            }
        }

        if (KotlinDetector.isKotlinType(cls)) {
            this.inspectLocalDelegates(typeSystem);
        }
    }

    private void inspectLocalDelegates(TypeSystem typeSystem) {

        for (Field field : this.cls.getDeclaredFields()) {
            if (!isKotlinDelegate(field)) {
                continue;
            }

            ClassInfo indirectSuperClass = new ClassInfo(field.getType(), field, typeSystem);
            this.extend(indirectSuperClass);
            this.indirectSuperClasses.add(indirectSuperClass);
        }
    }

    private static boolean isKotlinDelegate(Field field) {

        return field.isSynthetic() && field.getName().startsWith("$$delegate_");
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
            throw new RuntimeException(
                subclass.className + " has two superclasses: " + subclass.directSuperclass.className + ", "
                    + this.className);
        }
        subclass.directSuperclass = this;
        this.directSubclasses.add(subclass);
    }

    public String name() {
        return className;
    }

    String simpleName() {
        return deriveSimpleName(this.cls);
    }

    public static String deriveSimpleName(Class<?> clazz) {

        String className = clazz.getName();
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
     * managed at runtime by using the @Labels annotation on a collection field, therefore the full set of labels to be
     * mapped to a node will be the static labels, in addition to any labels declared by the backing field of an
     * {@link Labels} annotation.
     * </p>
     *
     * @return A {@link Collection} of all the static labels that apply to the node or an empty list if there aren't
     * any, never <code>null</code>
     */
    public Collection<String> staticLabels() {

        Collection<String> knownStaticLabels = this.staticLabels;
        if (knownStaticLabels == null) {
            synchronized (this) {
                knownStaticLabels = this.staticLabels;
                if (knownStaticLabels == null) {
                    this.staticLabels = Collections.unmodifiableCollection(collectLabels());
                    knownStaticLabels = this.staticLabels;
                }
            }
        }
        return knownStaticLabels;
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
            if (!isAbstract) {
                neo4jName = simpleName();
            }
        }
        return neo4jName;
    }

    private Collection<String> collectLabels() {

        List<String> labels = new ArrayList<>();
        if (!isAbstract || annotationsInfo.get(NodeEntity.class) != null) {
            labels.add(neo4jName());
        }
        if (directSuperclass != null && !"java.lang.Object".equals(directSuperclass.className)) {
            labels.addAll(directSuperclass.collectLabels());
        }
        for (ClassInfo interfaceInfo : directInterfaces()) {
            labels.addAll(interfaceInfo.collectLabels());
        }
        for (ClassInfo indirectSuperClass : indirectSuperClasses) {
            labels.addAll(indirectSuperClass.collectLabels());
        }
        return labels;
    }

    public List<ClassInfo> directSubclasses() {
        return directSubclasses;
    }

    /**
     * @return A list of all implementing and extending subclasses.
     * @since 3.1.20
     */
    public Collection<ClassInfo> allSubclasses() {

        Set<ClassInfo> computedSubclasses = this.allSubclasses;
        if (computedSubclasses == null) {
            synchronized (this) {
                computedSubclasses = this.allSubclasses;
                if (computedSubclasses == null) {
                    this.allSubclasses = computeSubclasses();
                    computedSubclasses = this.allSubclasses;
                }
            }
        }
        return computedSubclasses;
    }

    private Set<ClassInfo> computeSubclasses() {

        Set<ClassInfo> computedSubclasses = new HashSet<>();
        for (ClassInfo classInfo : this.directSubclasses()) {
            computedSubclasses.add(classInfo);
            computedSubclasses.addAll(classInfo.allSubclasses());
        }
        for (ClassInfo classInfo : this.directImplementingClasses()) {
            computedSubclasses.add(classInfo);
            computedSubclasses.addAll(classInfo.allSubclasses());
        }
        return Collections.unmodifiableSet(computedSubclasses);
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
        return getOrComputeIdentityField().orElse(null);
    }

    /**
     * The identity field is a field annotated with @NodeId, or if none exists, a field
     * of type Long called 'id'
     *
     * @return A {@link FieldInfo} object representing the identity field never <code>null</code>
     * @throws MappingException if no identity field can be found
     */
    public FieldInfo identityField() {
        return getOrComputeIdentityField()
            .orElseThrow(() -> new MetadataException("No internal identity field found for class: " + this.className));
    }

    private Optional<FieldInfo> getOrComputeIdentityField() {

        Optional<FieldInfo> result = this.identityField;
        if (result == null) {
            synchronized (this) {
                result = this.identityField;
                if (result == null) {
                    // Didn't want to add yet another method related to determining the identy field
                    // so the actual resolving of the field inside the Double-checked locking here
                    // has been inlined.
                    Collection<FieldInfo> identityFields = getFieldInfos(FieldInfo::isInternalIdentity);
                    if (identityFields.size() == 1) {
                        this.identityField = Optional.of(identityFields.iterator().next());
                    } else if (identityFields.size() > 1) {
                        throw new MetadataException("Expected exactly one internal identity field (@Id with " +
                            "InternalIdStrategy), found " + identityFields.size() + " " + identityFields);
                    } else {
                        this.identityField = fieldsInfo.fields().stream()
                            .filter(f -> "id".equals(f.getName()))
                            .filter(f -> "java.lang.Long".equals(f.getTypeDescriptor()))
                            .findFirst();
                    }
                    result = this.identityField;
                }
            }
        }
        return result;
    }

    public boolean hasIdentityField() {
        return getOrComputeIdentityField().isPresent();
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
     * @throws InvalidPropertyFieldException if the recognized property fields contain a field that is not
     *                                       actually persistable as property.
     */
    public Collection<FieldInfo> propertyFields() {

        return getOrComputePropertyFields().values();
    }

    /**
     * Finds the property field with a specific property name from the ClassInfo's property fields
     * Note that this method does not allow for property names with differing case. //TODO
     *
     * @param propertyName the propertyName of the field to find
     * @return A FieldInfo object describing the required property field, or null if it doesn't exist.
     * @throws InvalidPropertyFieldException if the recognized property fields contain a field that is not
     *                                       actually persistable as property.
     */
    public FieldInfo propertyField(String propertyName) {

        return propertyName == null ? null : getOrComputePropertyFields().get(propertyName);
    }

    private Map<String, FieldInfo> getOrComputePropertyFields() {

        Map<String, FieldInfo> result = this.propertyFields;
        if (result == null) {
            synchronized (this) {
                result = this.propertyFields;
                if (result == null) {
                    Collection<FieldInfo> fields = fieldsInfo().fields();

                    FieldInfo optionalIdentityField = identityFieldOrNull();
                    Map<String, FieldInfo> intermediateFieldMap = new HashMap<>(fields.size());

                    for (FieldInfo fieldInfo : fields) {
                        if (fieldInfo == optionalIdentityField || fieldInfo.isLabelField() || fieldInfo.hasAnnotation(StartNode.class) || fieldInfo.hasAnnotation(EndNode.class)) {
                            continue;
                        }

                        if (!fieldInfo.getAnnotations().has(Property.class)) {

                            // If a field is not marked explicitly as a property but is persistable as such, add it.
                            if (fieldInfo.persistableAsProperty()) {
                                intermediateFieldMap.put(fieldInfo.property(), fieldInfo);
                            }
                        } else if (fieldInfo.persistableAsProperty()) {

                            // If it is marked as a property, then it should be persistable as such
                            intermediateFieldMap.put(fieldInfo.property(), fieldInfo);
                        } else {

                            // Otherwise, throw a fitting exception
                            throw new InvalidPropertyFieldException(fieldInfo);
                        }
                    }
                    this.propertyFields = Collections.unmodifiableMap(intermediateFieldMap);
                    result = this.propertyFields;
                }
            }
        }
        return result;
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

        Collection<FieldInfo> result = this.relationshipFields;
        if (result == null) {
            synchronized (this) {
                result = this.relationshipFields;
                if (result == null) {
                    FieldInfo optionalIdentityField = identityFieldOrNull();
                    Set<FieldInfo> identifiedRelationshipFields = new HashSet<>();
                    for (FieldInfo fieldInfo : fieldsInfo().fields()) {
                        if (fieldInfo == optionalIdentityField) {
                            continue;
                        }

                        if (fieldInfo.getAnnotations().has(Relationship.class)) {
                            identifiedRelationshipFields.add(fieldInfo);
                        } else if (!fieldInfo.persistableAsProperty()) {
                            identifiedRelationshipFields.add(fieldInfo);
                        }
                    }
                    this.relationshipFields = Collections.unmodifiableSet(identifiedRelationshipFields);
                    result = this.relationshipFields;
                }
            }
        }
        return result;
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
     * @param relationshipDirection the direction of the relationship represented by a string
     * @param strict                if true, does not infer relationship type but looks for it in the @Relationship annotation. Null if missing. If false, infers relationship type from FieldInfo
     * @return A FieldInfo object describing the required relationship field, or null if it doesn't exist.
     */
    public FieldInfo relationshipField(String relationshipName, Direction relationshipDirection, boolean strict) {
        for (FieldInfo fieldInfo : relationshipFields()) {
            String relationship = strict ? fieldInfo.relationshipTypeAnnotation() : fieldInfo.relationship();
            if (relationshipName.equalsIgnoreCase(relationship)) {
                Direction declaredDirection = fieldInfo.relationshipDirectionOrDefault(Direction.OUTGOING);
                if (isActualDirectionCompatibleWithDeclaredDirection(relationshipDirection, declaredDirection)) {
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
    public Set<FieldInfo> candidateRelationshipFields(String relationshipName, Direction relationshipDirection, boolean strict) {
        Set<FieldInfo> candidateFields = new HashSet<>();
        for (FieldInfo fieldInfo : relationshipFields()) {
            String relationship = strict ? fieldInfo.relationshipTypeAnnotation() : fieldInfo.relationship();
            if (relationshipName.equalsIgnoreCase(relationship)) {
                Direction declaredDirection = fieldInfo.relationshipDirectionOrDefault(Direction.OUTGOING);
                if (isActualDirectionCompatibleWithDeclaredDirection(relationshipDirection, declaredDirection)) {
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
                throw new RuntimeException(
                    "Field " + fieldInfo.getName() + " not found in class " + name() + " or any of its superclasses");
            }
        }
    }

    /**
     * Find all FieldInfos for the specified ClassInfo whose type matches the supplied fieldType
     *
     * @param fieldType The field type to look for
     * @return A {@link List} of {@link FieldInfo} objects that are of the given type, never <code>null</code>
     */
    public List<FieldInfo> findFields(Class<?> fieldType) {

        String fieldSignature = fieldType.getName();
        Predicate<FieldInfo> matchesType = f -> f.getTypeDescriptor().equals(fieldSignature);
        return fieldsInfo().fields().stream().filter(matchesType).collect(toList());
    }

    /**
     * Find all FieldInfos for the specified ClassInfo which have the specified annotation
     *
     * @param annotation The annotation
     * @return A {@link List} of {@link FieldInfo} objects that are of the given type, never <code>null</code>
     */
    public List<FieldInfo> findFields(String annotation) {

        Predicate<FieldInfo> hasAnnotation = f -> f.hasAnnotation(annotation);
        return fieldsInfo().fields().stream().filter(hasAnnotation).collect(toList());
    }

    /**
     * Retrieves a {@link List} of {@link FieldInfo} representing all of the fields that can be iterated over
     * using a "foreach" loop.
     *
     * @return {@link List} of {@link FieldInfo}
     */
    public List<FieldInfo> findIterableFields() {

        Predicate<FieldInfo> isIterable = f -> {
            // The actual call to getField might throw an exception.
            // While FieldInfo#type() should also return the type,
            // ClassInfo#getField has side-effects which I cannot judge
            // atm, so better keep it here
            // and wrap the predicate in an exception below.
            Class type = getField(f).getType();
            return type.isArray() || Iterable.class.isAssignableFrom(type);
        };

        // See comment inside predicate regarding this exception.
        try {
            return fieldsInfo().fields().stream()
                .filter(isIterable).collect(toList());
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

        String typeSignature = iteratedType.getName();
        String arrayOfTypeSignature = typeSignature + "[]";

        Predicate<FieldInfo> isIterableOfType = f -> {
            String fieldType = f.getTypeDescriptor();

            boolean isMatchingArray =
                f.isArray() && (fieldType.equals(arrayOfTypeSignature) || f.isParameterisedTypeOf(iteratedType));
            boolean isMatchingIterable =
                f.isIterable() && (fieldType.equals(typeSignature) || f.isParameterisedTypeOf(iteratedType));

            return isMatchingArray || isMatchingIterable;
        };

        return fieldsInfo().fields().stream()
            .filter(isIterableOfType).collect(toList());
    }

    /**
     * Finds all fields whose type is equivalent to Array&lt;X&gt; or assignable from Iterable&lt;X&gt;
     * where X is the generic parameter type of the Array or Iterable and the relationship type backing this iterable is "relationshipType"
     *
     * @param iteratedType          the type of iterable
     * @param relationshipType      the relationship type
     * @param relationshipDirection the relationship direction
     * @param strict                if true, does not infer relationship type but looks for it in the @Relationship annotation. Null if missing. If false, infers relationship type from FieldInfo
     * @return {@link List} of {@link MethodInfo}, never <code>null</code>
     */
    public List<FieldInfo> findIterableFields(Class<?> iteratedType, String relationshipType, Direction relationshipDirection, boolean strict) {

        List<FieldInfo> iterableFields = new ArrayList<>();
        for (FieldInfo fieldInfo : findIterableFields(iteratedType)) {
            String relationship = strict ? fieldInfo.relationshipTypeAnnotation() : fieldInfo.relationship();
            if (relationshipType.equals(relationship)) {
                Direction declaredDirection = fieldInfo.relationshipDirectionOrDefault(Direction.OUTGOING);
                if (isActualDirectionCompatibleWithDeclaredDirection(relationshipDirection, declaredDirection)) {
                    iterableFields.add(fieldInfo);
                }
            }
        }
        return iterableFields;
    }

    private static boolean isActualDirectionCompatibleWithDeclaredDirection(Direction actual, Direction declared) {
        return ((declared == Direction.INCOMING || declared == Direction.UNDIRECTED) && actual == Direction.INCOMING) ||
            (declared != Direction.INCOMING && actual == Direction.OUTGOING);
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

        if (classInfo == null) {
            return false;
        }

        if (this == classInfo) {
            return true;
        }

        for (ClassInfo subclass : classInfo.directSubclasses()) {
            if (isSubclassOf(subclass)) {
                return true;
            }
        }

        return this.indirectSuperClasses.stream()
            .anyMatch(c -> c.getUnderlyingClass() == classInfo.getUnderlyingClass());
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
     * @param relationshipType      the relationship type
     * @param relationshipDirection the relationship direction
     * @return class of the type parameter descriptor or null if it could not be determined
     */
    Class<?> getTypeParameterDescriptorForRelationship(String relationshipType, Direction relationshipDirection) {
        final boolean STRICT_MODE = true; //strict mode for matching methods and fields, will only look for explicit annotations
        final boolean INFERRED_MODE = false; //inferred mode for matching methods and fields, will infer the relationship type from the getter/setter/property

        try {
            FieldInfo fieldInfo = relationshipField(relationshipType, relationshipDirection, STRICT_MODE);
            if (fieldInfo != null && fieldInfo.getTypeDescriptor() != null) {
                return DescriptorMappings.getType(fieldInfo.getTypeDescriptor());
            }

            if (relationshipDirection != Direction.INCOMING) { //we always expect an annotation for INCOMING
                fieldInfo = relationshipField(relationshipType, relationshipDirection, INFERRED_MODE);
                if (fieldInfo != null && fieldInfo.getTypeDescriptor() != null) {
                    return DescriptorMappings.getType(fieldInfo.getTypeDescriptor());
                }
            }
        } catch (RuntimeException e) {
            LOGGER.debug("Could not get {} class type for relationshipType {} and relationshipDirection {} ", className,
                relationshipType, relationshipDirection);
        }
        return null;
    }

    /**
     * @return If this class contains any fields/properties annotated with @Index.
     */
    public boolean containsIndexes() {
        return !(getIndexFields().isEmpty() && getCompositeIndexes().isEmpty());
    }

    /**
     * @return The <code>FieldInfo</code>s representing the Indexed fields in this class.
     */
    public Collection<FieldInfo> getIndexFields() {

        Map<String, FieldInfo> result = this.indexFields;
        if (result == null) {
            synchronized (this) {
                result = this.indexFields;
                if (result == null) {
                    Map<String, FieldInfo> indexes = new HashMap<>();
                    Field[] declaredFields = cls.getDeclaredFields();

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
                    this.indexFields = Collections.unmodifiableMap(indexes);
                    result = this.indexFields;
                }
            }
        }
        return result.values();
    }

    private static boolean isDeclaredField(Field[] declaredFields, String name) {

        for (Field field : declaredFields) {
            if (field.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public Collection<CompositeIndex> getCompositeIndexes() {

        Collection<CompositeIndex> result = this.compositeIndexes;
        if (result == null) {
            synchronized (this) {
                result = this.compositeIndexes;
                if (result == null) {
                    CompositeIndex[] annotations = cls.getDeclaredAnnotationsByType(CompositeIndex.class);
                    List<CompositeIndex> intermediateResult = new ArrayList<>(annotations.length);

                    for (CompositeIndex annotation : annotations) {
                        String[] properties =
                            annotation.value().length > 0 ? annotation.value() : annotation.properties();

                        if (properties.length < 1) {
                            throw new MetadataException("Incorrect CompositeIndex definition on " + className +
                                ". Provide at least 1 property");
                        }

                        for (String property : properties) {
                            // Determine the original field in case the user uses a MapCompositeConverter.
                            Matcher m = AutoIndexManager.COMPOSITE_KEY_MAP_COMPOSITE_PATTERN.matcher(property);
                            if (m.matches()) {
                                property = m.group(1);
                            }

                            FieldInfo fieldInfo = propertyField(property);
                            if (fieldInfo == null) {
                                throw new MetadataException(
                                    "Incorrect CompositeIndex definition on " + className + ". Property " +
                                        property + " does not exists.");
                            }
                        }
                        intermediateResult.add(annotation);
                    }
                    this.compositeIndexes = Collections.unmodifiableList(intermediateResult);
                    result = this.compositeIndexes;
                }
            }
        }
        return result;
    }

    public FieldInfo primaryIndexField() {

        return getOrComputePrimaryIndexField().orElse(null);
    }

    private Optional<FieldInfo> getOrComputePrimaryIndexField() {

        Optional<FieldInfo> result = this.primaryIndexField;
        if (result == null) {
            synchronized (this) {
                result = this.primaryIndexField;
                if (result == null) {
                    Optional<FieldInfo> potentialPrimaryIndexField = Optional.empty();
                    Collection<FieldInfo> primaryIndexFields = getFieldInfos(this::isPrimaryIndexField);
                    if (primaryIndexFields.size() > 1) {
                        throw new MetadataException(
                            "Only one @Id / @Index(primary=true, unique=true) annotation is allowed in a class hierarchy. Please check annotations in the class "
                                + name() + " or its parents");
                    } else if (!primaryIndexFields.isEmpty()) {
                        FieldInfo selectedField = primaryIndexFields.iterator().next();
                        AnnotationInfo generatedValueAnnotation = selectedField.getAnnotations().get(GeneratedValue.class);
                        if (generatedValueAnnotation != null) {
                            // Here's a funny hidden side effect I wasn't able to refactor out with a clear idea during
                            // rethinking the whole collection of synchronized blocks :(
                            GeneratedValue value = (GeneratedValue) generatedValueAnnotation.getAnnotation();
                            idStrategyClass = value.strategy();
                            try {
                                idStrategy = idStrategyClass.getDeclaredConstructor().newInstance();
                            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                                LOGGER.debug("Could not instantiate {}. Expecting this to be registered manually.", idStrategyClass);
                            }
                        }
                        potentialPrimaryIndexField = Optional.of(selectedField);
                    }
                    result = validateIdGenerationConfigFor(potentialPrimaryIndexField);
                }
            }
        }
        return result;
    }

    private Optional<FieldInfo> validateIdGenerationConfigFor(Optional<FieldInfo> potentialPrimaryIndexField) {
        fieldsInfo().fields().forEach(info -> {
            if (info.hasAnnotation(GeneratedValue.class) && !info.hasAnnotation(Id.class)) {
                throw new MetadataException(
                    "The type of @Generated field in class " + className + " must be also annotated with @Id.");
            }
        });
        if (UuidStrategy.class.equals(idStrategyClass)) {
            potentialPrimaryIndexField.ifPresent(selectedField -> {
                if (!(selectedField.isTypeOf(UUID.class) || selectedField.isTypeOf(String.class))) {
                    throw new MetadataException(
                        "The type of " + selectedField.getName() + " in class " + className
                            + " must be of type java.lang.UUID or java.lang.String because it has an UUID generation strategy.");
                }
            });
        }
        return potentialPrimaryIndexField;
    }

    public boolean hasPrimaryIndexField() {
        return getOrComputePrimaryIndexField().isPresent();
    }

    private boolean isPrimaryIndexField(FieldInfo fieldInfo) {

        boolean hasIdAnnotation = fieldInfo.hasAnnotation(Id.class);
        boolean hasStrategyOtherThanInternal = !fieldInfo.hasAnnotation(GeneratedValue.class)
            || !((GeneratedValue) fieldInfo.getAnnotations().get(GeneratedValue.class).getAnnotation()).strategy()
            .equals(InternalIdStrategy.class);

        return hasIdAnnotation && hasStrategyOtherThanInternal;
    }

    public IdStrategy idStrategy() {
        // Forces initialization of the id strategy
        return getOrComputePrimaryIndexField()
            .map(ignored -> idStrategy).orElse(null);
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

        Optional<MethodInfo> result = this.postLoadMethod;
        if (result == null) {
            synchronized (this) {
                result = this.postLoadMethod;
                if (result == null) {
                    Collection<MethodInfo> possiblePostLoadMethods = methodsInfo
                        .findMethodInfoBy(methodInfo -> methodInfo.hasAnnotation(PostLoad.class));
                    if (possiblePostLoadMethods.size() > 1) {
                        throw new MetadataException(String
                            .format("Cannot have more than one post load method annotated with @PostLoad for class '%s'",
                                this.className));
                    }

                    this.postLoadMethod = possiblePostLoadMethods.stream().findFirst();
                    result = this.postLoadMethod;
                }
            }
        }

        return result.orElse(null);
    }

    public FieldInfo getFieldInfo(String propertyName) {

        // fall back to the field if method cannot be found
        FieldInfo optionalLabelField = labelFieldOrNull();
        if (optionalLabelField != null && optionalLabelField.getName().equals(propertyName)) {
            return optionalLabelField;
        }
        FieldInfo propertyField = propertyField(propertyName);
        if (propertyField != null) {
            return propertyField;
        }

        return fieldsInfo.get(propertyName);
    }

    /**
     * Return a FieldInfo for the EndNode of a RelationshipEntity
     *
     * @return a FieldInfo for the field annotated as the EndNode, or none if not found
     */
    public FieldInfo getEndNodeReader() {

        Optional<FieldInfo> result = this.endNodeReader;
        if (result == null) {
            synchronized (this) {
                result = this.endNodeReader;
                if (result == null) {
                    if (isRelationshipEntity()) {
                        endNodeReader = fieldsInfo().fields().stream()
                            .filter(fieldInfo -> fieldInfo.getAnnotations().get(EndNode.class) != null)
                            .findFirst();
                        if (!endNodeReader.isPresent()) {
                            LOGGER.warn("Failed to find an @EndNode on {}", name());
                        }
                    } else {
                        endNodeReader = Optional.empty();
                    }
                    result = this.endNodeReader;
                }
            }
        }

        return result.orElse(null);
    }

    /**
     * Return a FieldInfo for the StartNode of a RelationshipEntity
     *
     * @return a FieldInfo for the field annotated as the StartNode, or none if not found
     */
    public FieldInfo getStartNodeReader() {

        Optional<FieldInfo> result = this.startNodeReader;
        if (result == null) {
            synchronized (this) {
                result = this.startNodeReader;
                if (result == null) {
                    if (isRelationshipEntity()) {
                        startNodeReader = fieldsInfo().fields().stream()
                            .filter(fieldInfo -> fieldInfo.getAnnotations().get(StartNode.class) != null)
                            .findFirst();

                        if (!startNodeReader.isPresent()) {
                            LOGGER.warn("Failed to find an @StartNode on {}", name());
                        }
                    } else {
                        startNodeReader = Optional.empty();
                    }
                    result = this.startNodeReader;
                }
            }
        }

        return result.orElse(null);
    }

    /**
     * Returns if the class as fields annotated with @Required annotation
     */
    public boolean hasRequiredFields() {
        return !requiredFields().isEmpty();
    }

    public Collection<FieldInfo> requiredFields() {
        if (requiredFields == null) {
            requiredFields = new ArrayList<>();

            for (FieldInfo fieldInfo : propertyFields()) {
                if (fieldInfo.getAnnotations().has(Required.class)) {
                    requiredFields.add(fieldInfo);
                }
            }
        }
        return requiredFields;
    }

    public boolean hasVersionField() {
        return getOrComputeVersionField().isPresent();
    }

    public FieldInfo getVersionField() {
        return getOrComputeVersionField().orElse(null);
    }

    private Optional<FieldInfo> getOrComputeVersionField() {

        Optional<FieldInfo> result = this.versionField;
        if (result == null) {
            synchronized (this) {
                result = this.versionField;
                if (result == null) {
                    Collection<FieldInfo> fields = getFieldInfos(FieldInfo::isVersionField);

                    if (fields.size() > 1) {
                        throw new MetadataException("Only one version field is allowed, found " + fields);
                    }

                    Iterator<FieldInfo> iterator = fields.iterator();
                    if (iterator.hasNext()) {
                        this.versionField = Optional.of(iterator.next());
                    } else {
                        // cache that there is no version field
                        this.versionField = Optional.empty();
                    }
                    result = this.versionField;
                }
            }
        }
        return result;
    }

    /**
     * Reads the value of the entity's primary index field if any.
     *
     * @param entity
     * @return
     */
    public Object readPrimaryIndexValueOf(Object entity) {

        Objects.requireNonNull(entity, "Entity to read from must not be null.");
        Object value = null;

        if (this.hasPrimaryIndexField()) {
            // One has to use #read here to get the ID as defined in the entity.
            // #readProperty gives back the converted value the database sees.
            // This breaks immediate in LoadOneDelegate#lookup(Class, Object).
            // That is called by LoadOneDelegate#load(Class, Serializable, int)
            // immediately after loading (and finding(!!) an entity, which is never
            // returned directly but goes through a cache.
            // However, LoadOneDelegate#load(Class, Serializable, int) deals with the
            // ID as defined in the domain and so we have to use that in the same way here.
            value = this.primaryIndexField().read(entity);
        }
        return value;
    }

    public Function<Object, Optional<Object>> getPrimaryIndexOrIdReader() {

        Function<Object, Optional<Object>> reader;

        if (this.hasPrimaryIndexField()) {
            reader = t -> Optional.ofNullable(this.readPrimaryIndexValueOf(t));
        } else {
            reader = t -> Optional.ofNullable(this.identityField().read(t));
        }

        return reader;
    }

    static Object getInstanceOrDelegate(Object instance, Field delegateHolder) {
        if (delegateHolder == null) {
            return instance;
        } else {
            return AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
                try {
                    delegateHolder.setAccessible(true);
                    return delegateHolder.get(instance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    @Override
    public String toString() {
        return "ClassInfo{" +
            "className='" + className + '\'' +
            ", neo4jName='" + neo4jName + '\'' +
            '}';
    }
}

package org.neo4j.ogm.metadata.info;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.metadata.MappingException;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.*;

/**
 * Maintains object to graph mapping details at the class (type) level
 *
 * The ClassInfo object is used to maintain mappings from Java Types->Neo4j Labels
 * thereby allowing the correct labels to be applied to new nodes when they
 * are persisted.
 *
 * The ClassInfo object also maintains a map of FieldInfo and MethodInfo objects
 * that maintain the appropriate information for mapping Java class attributes to Neo4j
 * node properties / paths (node)-[:relationship]->(node), via field or method
 * accessors respectively.
 *
 * Given a type hierarchy, the ClassInfo object guarantees that for any type in that
 * hierarchy, the labels associated with that type will include the labels for
 * all its superclass and interface types as well. This is to avoid the need to iterate
 * through the ClassInfo hierarchy to recover label information.
 */
public class ClassInfo {

    private String className;
    private int majorVersion;
    private int minorVersion;
    private String directSuperclassName;
    private boolean isInterface;
    private boolean hydrated;

    private FieldsInfo fieldsInfo = new FieldsInfo();
    private MethodsInfo methodsInfo= new MethodsInfo();
    private AnnotationsInfo annotationsInfo = new AnnotationsInfo();

    private ClassInfo directSuperclass;
    private final ArrayList<ClassInfo> directSubclasses = new ArrayList<>();

    // ??
    private final HashSet<InterfaceInfo> interfaces = new HashSet<>();
    private InterfacesInfo interfacesInfo = new InterfacesInfo();

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

        className = constantPool.lookup(dataInputStream.readUnsignedShort()).replace('/', '.');
        String sce = constantPool.lookup(dataInputStream.readUnsignedShort());
        if (sce != null) {
            directSuperclassName = sce.replace('/', '.');
        }
        interfacesInfo = new InterfacesInfo(dataInputStream, constantPool);
        fieldsInfo = new FieldsInfo(dataInputStream, constantPool);
        methodsInfo = new MethodsInfo(dataInputStream, constantPool);
        annotationsInfo = new AnnotationsInfo(dataInputStream, constantPool);

    }

    /** A class that was previously only seen as a superclass of another class can now be fully hydrated. */
    public void hydrate(ClassInfo classInfo) {
       if (!this.hydrated) {
            this.hydrated = true;
            this.interfaces.addAll(classInfo.interfaces());
            this.annotationsInfo.append(classInfo.annotationsInfo());
            this.fieldsInfo.append(classInfo.fieldsInfo());
            this.methodsInfo.append(classInfo.methodsInfo());
       }
    }

    void extend(ClassInfo classInfo) {
        this.interfaces.addAll(classInfo.interfaces());
        this.fieldsInfo.append(classInfo.fieldsInfo());
        this.methodsInfo.append(classInfo.methodsInfo());
    }

    /** This class was referenced as a superclass of the given subclass. */
    public ClassInfo(String name, ClassInfo subclass) {
        this.className = name;
        this.hydrated = false;
        addSubclass(subclass);
    }

    /** Connect this class to a subclass. */
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

    // todo: we currently only set one label per class. is this a problem?
    public String label() {
        AnnotationInfo annotationInfo = annotationsInfo.get(NodeEntity.CLASS);
        return((annotationInfo != null) ? annotationInfo.get(NodeEntity.LABEL, simpleName()) : simpleName());
    }

    private Collection<String> collectLabels(Collection<String> labelNames) {
        labelNames.add(label());
        if (directSuperclass != null && !"java.lang.Object".equals(directSuperclass.className)) {
            directSuperclass.collectLabels(labelNames);
        }
        return labelNames;
    }

    public List<ClassInfo> directSubclasses() {
        return directSubclasses;
    }

    Set<InterfaceInfo> interfaces() {
        return interfaces;
    }

    public Collection<AnnotationInfo> annotations() {
        return annotationsInfo.list();
    }

    public boolean isInterface() {
        return isInterface;
    }

    public AnnotationsInfo annotationsInfo() {
        return annotationsInfo;
    }
    public String superclassName() {
        return directSuperclassName;
    }

    public FieldsInfo fieldsInfo() {
        return fieldsInfo;
    }

    public MethodsInfo methodsInfo() {
        return methodsInfo;
    }

    @Override
    public String toString() {
        return name();
    }

    /**
     * The identity field is a field annotated with @NodeId, or if none exists, a field
     * of type Long called 'id'
     *
     * @return A {@link FieldInfo} object representing the identity field never <code>null</code>
     * @throws MappingException if no identity field can be found
     */
    public FieldInfo identityField() {
        for (FieldInfo fieldInfo : fieldsInfo().fields()) {
            AnnotationInfo annotationInfo = fieldInfo.getAnnotations().get(GraphId.CLASS);
            if (annotationInfo != null) {
                if (fieldInfo.getDescriptor().equals("Ljava/lang/Long;")) {
                    return fieldInfo;
                }
            }
        }
        FieldInfo fieldInfo = fieldsInfo().get("id");
        if (fieldInfo != null) {
            if (fieldInfo.getDescriptor().equals("Ljava/lang/Long;")) {
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
        FieldInfo identityField = identityField();
        Set<FieldInfo> fieldInfos = new HashSet<>();
        for (FieldInfo fieldInfo : fieldsInfo().fields()) {
            if (!fieldInfo.getName().equals(identityField.getName())) {
                // todo: when building fieldInfos, we must exclude fields annotated @Transient, or with the transient modifier
                if (fieldInfo.getAnnotations().isEmpty()) {
                    if (fieldInfo.isSimple()) {
                        fieldInfos.add(fieldInfo);
                    }
                } else {
                    AnnotationInfo annotationInfo = fieldInfo.getAnnotations().get(Property.CLASS);
                    if (annotationInfo != null) {
                        fieldInfos.add(fieldInfo);
                    }
                }
            }
        }
        return fieldInfos;
    }

    /**
     * Finds the property field with a specific name from the ClassInfo's property fields
     *
     * @param propertyName the propertyName of the field to find
     * @return A FieldInfo object describing the required property field, or null if it doesn't exist.
     */
    public FieldInfo propertyField(String propertyName) {
        for (FieldInfo fieldInfo : propertyFields()) {
            if (fieldInfo.property().equalsIgnoreCase(propertyName)) {
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
        FieldInfo identityField = identityField();
        Set<FieldInfo> fieldInfos = new HashSet<>();
        for (FieldInfo fieldInfo : fieldsInfo().fields()) {
            if (fieldInfo != identityField) {
                // todo: when building fieldInfos, we must exclude fields annotated @Transient, or with the transient modifier
                if (fieldInfo.getAnnotations().isEmpty()) {
                    if (!fieldInfo.isSimple()) {
                        fieldInfos.add(fieldInfo);
                    }
                } else {
                    AnnotationInfo annotationInfo = fieldInfo.getAnnotations().get(Relationship.CLASS);
                    if (annotationInfo != null) {
                        fieldInfos.add(fieldInfo);
                    }
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
     * The identity getter is any getter annotated with @NodeId returning a Long, or if none exists, a getter
     * returning Long called 'getId'
     *
     * @return A FieldInfo object representing the identity field or null if it doesn't exist
     */
    public MethodInfo identityGetter() {
        for (MethodInfo methodInfo : methodsInfo().getters()) {
            //LOGGER.info(methodInfo.getName() + ": " + methodInfo.getDescriptor());
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
                if (methodInfo.getAnnotations().isEmpty()) {
                    if (methodInfo.isSimpleGetter()) {
                        propertyGetters.add(methodInfo);
                    }
                } else {
                    AnnotationInfo annotationInfo = methodInfo.getAnnotations().get(Property.CLASS);
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
     * @return A Collection of MethodInfo objects describing the classInfo's property setters
     */
    public Collection<MethodInfo> propertySetters() {
        MethodInfo identitySetter = identitySetter();
        Set<MethodInfo> propertySetters = new HashSet<>();
        for (MethodInfo methodInfo : methodsInfo().setters()) {
            if (identitySetter == null || !methodInfo.getName().equals(identitySetter.getName())) {
                if (methodInfo.getAnnotations().isEmpty()) {
                    if (methodInfo.isSimpleSetter()) {
                        propertySetters.add(methodInfo);
                    }
                } else {
                    AnnotationInfo annotationInfo = methodInfo.getAnnotations().get(Property.CLASS);
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
     * @return A Collection of MethodInfo objects describing the classInfo's property getters
     */
    public Collection<MethodInfo> relationshipGetters() {
        MethodInfo identityGetter = identityGetter();
        Set<MethodInfo> relationshipGetters = new HashSet<>();
        for (MethodInfo methodInfo : methodsInfo().getters()) {
            if (identityGetter == null || !methodInfo.getName().equals(identityGetter.getName())) {
                if (methodInfo.getAnnotations().isEmpty()) {
                    if (!methodInfo.isSimpleGetter()) {
                        relationshipGetters.add(methodInfo);
                    }
                } else {
                    AnnotationInfo annotationInfo = methodInfo.getAnnotations().get(Relationship.CLASS);
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
     * @return A Collection of MethodInfo objects describing the classInfo's property getters
     */
    public Collection<MethodInfo> relationshipSetters() {
        MethodInfo identitySetter = identitySetter();
        Set<MethodInfo> relationshipSetters = new HashSet<>();
        for (MethodInfo methodInfo : methodsInfo().setters()) {
            if (identitySetter == null || !methodInfo.getName().equals(identitySetter.getName())) {
                if (methodInfo.getAnnotations().isEmpty()) {
                    if (!methodInfo.isSimpleSetter()) {
                        relationshipSetters.add(methodInfo);
                    }
                } else {
                    AnnotationInfo annotationInfo = methodInfo.getAnnotations().get(Relationship.CLASS);
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

    /**
     *
     * @param fieldInfo
     * @return
     */
    public boolean isScalar(FieldInfo fieldInfo) {
        Field field = getField(fieldInfo);
        return(!Collection.class.isAssignableFrom(field.getType()) && !fieldInfo.getDescriptor().contains("["));
    }

    /**
     *
     * @param fieldInfo
     * @return
     */
    public Field getField(FieldInfo fieldInfo) {
        try {
            return Class.forName(name()).getDeclaredField(fieldInfo.getName());
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

    /**
     *
     * @param methodInfo
     * @param parameterTypes
     * @return
     */
    public Method getMethod(MethodInfo methodInfo, Class... parameterTypes) {
        try {
            return Class.forName(name()).getMethod(methodInfo.getName(), parameterTypes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Find all setter MethodInfos for the specified ClassInfo whose parameter type matches the supplied class
     *
     * @param parameterType  the setter parameter type to look for.
     * @return
     */
    public List<MethodInfo> findSetters(Class parameterType) {
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
     * Find all FieldInfos for the specified ClassInfo whose type matches the supplied fieldType
     *
     * @param fieldType The fieldType to look for
     * @return
     */
    public List<FieldInfo> findFields(Class fieldType) {
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
     *
     */
    public List<FieldInfo> findIterableFields() {
        List<FieldInfo> fieldInfos = new ArrayList<>();
        try {
            for (FieldInfo fieldInfo : fieldsInfo().fields() ) {
                Class type = getField(fieldInfo).getType();
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
     * Finds all fields whose type is equivalent to Array<X> or assignable from Collection<X>
     * where X is the generic parameter type of the Array or Collection
     */
    public List<FieldInfo> findIterableFields(Class iteratedType) {
        List<FieldInfo> fieldInfos = new ArrayList<>();
        try {
            for (FieldInfo fieldInfo : fieldsInfo().fields() ) {
                Field field = getField(fieldInfo);
                Class type = field.getType();
                if (type.isArray() && type.getComponentType().equals(iteratedType)) {
                    fieldInfos.add(fieldInfo);
                }
                else if (Collection.class.isAssignableFrom(type)) {
                    ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
                    Class<?> parameterizedTypeClass = (Class<?>) parameterizedType.getActualTypeArguments()[0];
                    if (parameterizedTypeClass == iteratedType) {
                        fieldInfos.add(fieldInfo);
                    }
                }
            }
            return fieldInfos;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

     /**
     * Finds all setter methods whose parameter signature is equivalent to Array<X> or assignable from Collection<X>
     * where X is the generic parameter type of the Array or Collection
     */
    public List<MethodInfo> findIterableSetters(Class iteratedType) {
        List<MethodInfo> methodInfos = new ArrayList<>();
        try {
            Class clazz = Class.forName(name());
            for (Method method : clazz.getDeclaredMethods()) {
                MethodInfo methodInfo = methodsInfo().get(method.getName());
                if (methodInfo != null) {
                    if (methodInfo.getDescriptor().endsWith(")V")) {
                        if (method.getParameterTypes().length == 1) {
                            Class methodParameterType = method.getParameterTypes()[0];
                            if (methodParameterType.isArray() && methodParameterType.getComponentType() == iteratedType) {
                                methodInfos.add(methodInfo);
                            }
                            else if (Collection.class.isAssignableFrom(methodParameterType)) {
                                ParameterizedType parameterizedType = (ParameterizedType) method.getGenericParameterTypes()[0];
                                Class<?> parameterizedTypeClass = (Class<?>) parameterizedType.getActualTypeArguments()[0];
                                if (parameterizedTypeClass == iteratedType) {
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


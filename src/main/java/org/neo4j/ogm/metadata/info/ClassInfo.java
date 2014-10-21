package org.neo4j.ogm.metadata.info;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
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
 *
 * The inverse mapping, LabelInfo, maintains mappings from Neo4j labels to specific
 * Java Types.
 *
 */
public class ClassInfo {

    private String className;
    private int majorVersion;
    private int minorVersion;
    private String directSuperclassName;
    private boolean isInterface;

    private FieldsInfo fieldsInfo = new FieldsInfo();
    private MethodsInfo methodsInfo= new MethodsInfo();
    private AnnotationsInfo annotationsInfo = new AnnotationsInfo();
    private InterfacesInfo interfacesInfo = new InterfacesInfo();

    private boolean hydrated;

    // set later - if we need them...
    private ClassInfo directSuperclass;
    private ArrayList<ClassInfo> directSubclasses = new ArrayList<>();

    private HashSet<InterfaceInfo> interfaces = new HashSet<>();

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
        directSuperclassName = constantPool.lookup(dataInputStream.readUnsignedShort()).replace('/', '.');

        interfacesInfo = new InterfacesInfo(dataInputStream, constantPool);
        fieldsInfo = new FieldsInfo(dataInputStream, constantPool);
        methodsInfo = new MethodsInfo(dataInputStream, constantPool);
        annotationsInfo = new AnnotationsInfo(dataInputStream, constantPool);

    }

    public ClassInfo(String name, Collection<InterfaceInfo> interfaces, AnnotationsInfo annotationsInfo, FieldsInfo fieldsInfo, MethodsInfo methodsInfo) {
        this.className = name;
        this.fieldsInfo = fieldsInfo;
        this.methodsInfo = methodsInfo;

        this.hydrated = true;
        this.interfaces.addAll(interfaces);
        this.annotationsInfo = annotationsInfo;
    }

    /** A class that was previously only seen as a superclass of another class can now be fully hydrated. */
    public void hydrate(ClassInfo classInfo) {
        this.hydrated = true;
        this.interfaces.addAll(classInfo.interfaces());
        this.annotationsInfo.addAll(classInfo.annotations());
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

    public ClassInfo directSuperclass() {
        return directSuperclass;
    }

    public List<ClassInfo> directSubclasses() {
        return directSubclasses;
    }

    public Set<InterfaceInfo> interfaces() {
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

    @Override
    public String toString() {
        return name();
    }

}


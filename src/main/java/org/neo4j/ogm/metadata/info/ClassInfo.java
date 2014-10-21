package org.neo4j.ogm.metadata.info;

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
 * ClassInfo objects are intended to be retrieved from the Classify object, which is
 * the core class of the OGM metadata.
 */
public class ClassInfo {

    private String name;
    private boolean visited;

    private ClassInfo directSuperclass;
    private ArrayList<ClassInfo> directSubclasses = new ArrayList<>();
    private HashSet<InterfaceInfo> interfaces = new HashSet<>();

    // TODO: reify
    private Map<String,ObjectAnnotations> fieldInfos = new HashMap<>();
    private Map<String, ObjectAnnotations> methodInfos = new HashMap<>();

    private Set<AnnotationInfo> classAnnotations = new HashSet<>();

    public ClassInfo(String name, Set<InterfaceInfo> interfaces, Set<AnnotationInfo> annotations, Map<String, ObjectAnnotations> fieldAnnotations, Map<String, ObjectAnnotations> methodAnnotations) {
        this.name = name;
        this.fieldInfos = fieldAnnotations;
        this.methodInfos = methodAnnotations;

        this.visit(interfaces, annotations);
    }

    /**
     * If this method is called by another class, then it was previously cited as a superclass, and now has been
     * itself visited on the classpath.
     */
    public void visit(Set<InterfaceInfo> interfaces, Set<AnnotationInfo> annotations) {
        this.visited = true;
        this.interfaces.addAll(interfaces);
        this.classAnnotations.addAll(annotations);
    }

    /** This class was referenced as a superclass of the given subclass. */
    public ClassInfo(String name, ClassInfo subclass) {
        this.name = name;
        this.visited = false;
        addSubclass(subclass);
    }

    /** Connect this class to a subclass. */
    public void addSubclass(ClassInfo subclass) {
        if (subclass.directSuperclass != null && subclass.directSuperclass != this) {
            throw new RuntimeException(subclass.name + " has two superclasses: " + subclass.directSuperclass.name + ", " + this.name);
        }
        subclass.directSuperclass = this;
        this.directSubclasses.add(subclass);
    }

    public boolean visited() {
        return visited;
    }

    public String name() {
        return name;
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

    public Set<AnnotationInfo> annotations() {
        return classAnnotations;
    }

    @Override
    public String toString() {
        return name();
    }

}


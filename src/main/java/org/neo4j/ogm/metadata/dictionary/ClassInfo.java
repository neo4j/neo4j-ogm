package org.neo4j.ogm.metadata.dictionary;

import java.util.ArrayList;
import java.util.HashSet;

public class ClassInfo {

    /** Class name */
    String name;

    /**
     * Set to true when this class is visited in the classpath (false if the class is so far only
     * cited as a superclass)
     */
    boolean visited;

    /** Direct superclass */
    ClassInfo directSuperclass;

    /** Direct subclasses */
    ArrayList<ClassInfo> directSubclasses = new ArrayList<>();

    /** All interfaces */
    HashSet<String> interfaces = new HashSet<>();

    /** All annotations */
    HashSet<String> annotations = new HashSet<>();

    /** This class was visited on the classpath. */
    public ClassInfo(String name, ArrayList<String> interfaces, HashSet<String> annotations) {
        this.name = name;
        this.visit(interfaces, annotations);
    }

    /**
     * If called by another class, this class was previously cited as a superclass, and now has been
     * itself visited on the classpath.
     */
    public void visit(ArrayList<String> interfaces, HashSet<String> annotations) {
        this.visited = true;
        this.interfaces.addAll(interfaces);
        this.annotations.addAll(annotations);
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
            throw new RuntimeException(subclass.name + " has two superclasses: "
                    + subclass.directSuperclass.name + ", " + this.name);
        }
        subclass.directSuperclass = this;
        this.directSubclasses.add(subclass);
    }

    @Override
    public String toString() {
        return name;
    }
}


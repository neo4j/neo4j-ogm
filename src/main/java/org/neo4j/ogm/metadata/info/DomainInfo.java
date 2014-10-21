package org.neo4j.ogm.metadata.info;

import org.neo4j.ogm.metadata.ClassPathScanner;
import org.neo4j.ogm.metadata.MappingException;

import java.io.*;
import java.util.*;

/**
 * A Type Hierarchy (including Interfaces) is actually a DAG. Maybe we should be using Neo? !!
 *
 * This class needs a lot of tidying up
 */
public class DomainInfo implements ClassInfoProcessor {

    private List<String> classPaths = new ArrayList<>();

    private final HashMap<String, ClassInfo> classNameToClassInfo = new HashMap<>();
    private final HashMap<String, InterfaceInfo> interfaceNameToInterfaceInfo = new HashMap<>();
    private final HashMap<String, ArrayList<ClassInfo>> annotationNameToClassInfo = new HashMap<>();
    private final HashMap<String, ArrayList<ClassInfo>> interfaceNameToClassInfo = new HashMap<>();

    private ConstantPool constantPool;

    private void buildAnnotationNameToClassInfoMap() {
        // A <-[:has_annotation]- T
        for (ClassInfo classInfo : classNameToClassInfo.values()) {
            for (AnnotationInfo annotation : classInfo.annotations()) {
                ArrayList<ClassInfo> classInfoList = annotationNameToClassInfo.get(annotation.getName());
                if (classInfoList == null) {
                    annotationNameToClassInfo.put(annotation.getName(), classInfoList = new ArrayList<>());
                }
                classInfoList.add(classInfo);
            }
        }
    }

    private void buildInterfaceHierarchy() {
        // I - [:extends] -> J
        for (InterfaceInfo interfaceInfo : interfaceNameToInterfaceInfo.values()) {
            constructInterfaceHierarcy(interfaceInfo);
        }
    }

    private void constructInterfaceHierarcy(InterfaceInfo interfaceInfo) {
        if (interfaceInfo.allSuperInterfaces().isEmpty() && !interfaceInfo.superInterfaces().isEmpty()) {
            interfaceInfo.allSuperInterfaces().addAll(interfaceInfo.superInterfaces());
            for (InterfaceInfo superinterfaceInfo : interfaceInfo.superInterfaces()) {
                if (superinterfaceInfo != null) {
                    constructInterfaceHierarcy(superinterfaceInfo);
                    interfaceInfo.allSuperInterfaces().addAll(superinterfaceInfo.allSuperInterfaces());
                }
            }
        }
    }

    private void buildInterfaceNameToClassInfoMap() {
        // T -[:implements]-> I
        for (ClassInfo classInfo : classNameToClassInfo.values()) {
            HashSet<InterfaceInfo> interfaceAndSuperinterfaces = new HashSet<>();
            for (InterfaceInfo interfaceInfo : classInfo.interfaces()) {
                interfaceAndSuperinterfaces.add(interfaceInfo);
                if (interfaceInfo != null) {
                    interfaceAndSuperinterfaces.addAll(interfaceInfo.allSuperInterfaces());
                }
            }
            for (InterfaceInfo interfaceInfo : interfaceAndSuperinterfaces) {
                ArrayList<ClassInfo> classInfoList = interfaceNameToClassInfo.get(interfaceInfo.name());
                if (classInfoList == null) {
                    interfaceNameToClassInfo.put(interfaceInfo.name(), classInfoList = new ArrayList<>());
                }
                classInfoList.add(classInfo);
            }
        }
    }

    public void buildTransitiveInterfaceImplementations() {
        // transitive interface implementations: S-[:extends]->T-[:implements]->I  => S-[:implements]->I
        for (String interfaceName : interfaceNameToClassInfo.keySet()) {
            ArrayList<ClassInfo> classes = interfaceNameToClassInfo.get(interfaceName);
            HashSet<ClassInfo> subClasses = new HashSet<>(classes);
            for (ClassInfo classInfo : classes) {
                if (classInfo != null) {
                    for (ClassInfo subClassInfo : classInfo.directSubclasses()) {
                        subClasses.add(subClassInfo);
                    }
                }
            }
            interfaceNameToClassInfo.put(interfaceName, new ArrayList<>(subClasses));
        }
    }

    public void finish() {

        if (classNameToClassInfo.isEmpty() && interfaceNameToInterfaceInfo.isEmpty()) {
            return;
        }

//        /*
//         * get the root classes in the type hierarchy.
//         */
//        ArrayList<ClassInfo> roots = new ArrayList<>();
//        for (ClassInfo classInfo : classNameToClassInfo.values()) {
//            if (classInfo.directSuperclass() == null) {
//                roots.add(classInfo);
//            }
//        }
//
//        // R<-[:extends]-T*
//        LinkedList<ClassInfo> nodes = new LinkedList<>();
//        nodes.addAll(roots);
//        while (!nodes.isEmpty()) {
//            ClassInfo head = nodes.removeFirst();
//            for (ClassInfo subclass : head.directSubclasses()) {
//                nodes.add(subclass);
//            }
//        }

        buildAnnotationNameToClassInfoMap();
        buildInterfaceHierarchy();
        buildInterfaceNameToClassInfoMap();
        buildTransitiveInterfaceImplementations();

        // TODO: transitive annotations
        // if a superclass type, method or field is annotated, inject the annotation to subclasses
        // explicitly. Saves having to walk through type hierarchies to find an annotation.
        // must also include annotated interfaces.  WHICH WE DONT DO YET.

    }

    public void process(final InputStream inputStream) throws IOException {

        DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(inputStream, 1024));

        // Magic
        if (dataInputStream.readInt() != 0xCAFEBABE) {
            return;
        }

        dataInputStream.readUnsignedShort();    //minor version
        dataInputStream.readUnsignedShort();    // major version

        constantPool = new ConstantPool(dataInputStream);

        // Access flags
        int flags = dataInputStream.readUnsignedShort();
        boolean isInterface = (flags & 0x0200) != 0;

        String className = constantPool.lookup(dataInputStream.readUnsignedShort()).replace('/', '.');
        String superclassName = constantPool.lookup(dataInputStream.readUnsignedShort()).replace('/', '.');

        // get the information for this class
        InterfacesInfo interfacesInfo = new InterfacesInfo(dataInputStream, constantPool);
        FieldsInfo fieldsInfo = new FieldsInfo(dataInputStream, constantPool);
        MethodsInfo methodsInfo = new MethodsInfo(dataInputStream, constantPool);
        AnnotationsInfo classAnnotations = new AnnotationsInfo(dataInputStream, constantPool);

        // split reader here, and return the interfaces and annotations ?
        // this class IS AN INTERFACE
        if (isInterface) {
            // its an interface ref
            InterfaceInfo thisInterfaceInfo = interfaceNameToInterfaceInfo.get(className);
            if (thisInterfaceInfo == null) {
                interfaceNameToInterfaceInfo.put(className, new InterfaceInfo(className));
            } else {
                return;
            }

        } else {
            // its a class ref
            ClassInfo thisClassInfo = classNameToClassInfo.get(className);
            if (thisClassInfo == null) {
                thisClassInfo = new ClassInfo(className, interfacesInfo.list(), classAnnotations, fieldsInfo, methodsInfo);
                classNameToClassInfo.put(className, thisClassInfo);
            } else if (thisClassInfo.visited()) {
                return;
            } else {
                // todo: class info should have annotationsInfo, which should be a map.
                thisClassInfo.visit(interfacesInfo.list(), classAnnotations.list());
            }

            ClassInfo superclassInfo = classNameToClassInfo.get(superclassName);
            if (superclassInfo == null) {
                classNameToClassInfo.put(superclassName, new ClassInfo(superclassName, thisClassInfo));
            } else {
                superclassInfo.addSubclass(thisClassInfo);
            }
        }

    }

    // the public API. All the rest of the stuff above is just gumph and needs to be refactored away...
    public void load(String... packages) {

        classPaths.clear();
        classNameToClassInfo.clear();
        interfaceNameToInterfaceInfo.clear();
        annotationNameToClassInfo.clear();
        interfaceNameToClassInfo.clear();

        for (String packageName : packages) {
            String path = packageName.replaceAll("\\.", File.separator);
            classPaths.add(path);
        }

        new ClassPathScanner().scan(classPaths, this);
    }

    public ClassInfo getClass(String fqn) {
        return classNameToClassInfo.get(fqn);
    }

    public ClassInfo getClassSimpleName(String simpleClassName) {

        ClassInfo match = null;
        for (String fqn : classNameToClassInfo.keySet()) {
            if (fqn.endsWith("." + simpleClassName)) {
                if (match == null) {
                    match = classNameToClassInfo.get(fqn);
                } else {
                    throw new MappingException("More than one class has simple name: " + simpleClassName);
                }
            }
        }
        return match;
    }

    public ClassInfo getNamedClassWithAnnotation(String annotation, String className) {
        for (ClassInfo classInfo : annotationNameToClassInfo.get(annotation)) {
            if (classInfo.name().equals(className)) {
                return classInfo;
            }
        }
        return null;
    }

    public List<ClassInfo> getClassInfosWithAnnotation(String annotation) {
        return annotationNameToClassInfo.get(annotation);
    }
}

package org.neo4j.ogm.metadata.info;

import org.neo4j.ogm.metadata.ClassPathScanner;
import org.neo4j.ogm.metadata.MappingException;

import java.io.*;
import java.util.*;

public class DomainInfo implements ClassInfoProcessor {

    private final List<String> classPaths = new ArrayList<>();

    private final HashMap<String, ClassInfo> classNameToClassInfo = new HashMap<>();
    private final HashMap<String, InterfaceInfo> interfaceNameToInterfaceInfo = new HashMap<>();
    private final HashMap<String, ArrayList<ClassInfo>> annotationNameToClassInfo = new HashMap<>();
    private final HashMap<String, ArrayList<ClassInfo>> interfaceNameToClassInfo = new HashMap<>();

    public DomainInfo(String... packages) {
        long now = -System.currentTimeMillis();
        load(packages);
        System.out.println(classNameToClassInfo.entrySet().size() + " classes loaded in " + (now + System.currentTimeMillis()) + " milliseconds");
    }

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

    public void finish() {
        buildAnnotationNameToClassInfoMap();
        for (ClassInfo classInfo : classNameToClassInfo.values()) {
            if (classInfo.name() == null || classInfo.name().equals("java.lang.Object")) continue;
            if (classInfo.superclassName() == null || classInfo.superclassName().equals("java.lang.Object")) {
                extend(classInfo, classInfo.directSubclasses());
            }
        }
    }

    private void extend(ClassInfo superclass, List<ClassInfo> subclasses) {
        for (ClassInfo subclass : subclasses) {
            subclass.extend(superclass);
            extend(subclass, subclass.directSubclasses());
        }
    }

    public void process(final InputStream inputStream) throws IOException {

        ClassInfo classInfo = new ClassInfo(inputStream);
        String className = classInfo.name();
        String superclassName = classInfo.superclassName();

        if (className != null) {
            if (classInfo.isInterface()) {
                InterfaceInfo thisInterfaceInfo = interfaceNameToInterfaceInfo.get(className);
                if (thisInterfaceInfo == null) {
                    interfaceNameToInterfaceInfo.put(className, new InterfaceInfo(className));
                }
            } else {
                ClassInfo thisClassInfo = classNameToClassInfo.get(className);
                if (thisClassInfo == null) {
                    thisClassInfo = classInfo;
                    classNameToClassInfo.put(className, thisClassInfo);
                }
                if (!thisClassInfo.hydrated()) {
                    thisClassInfo.hydrate(classInfo);
                    ClassInfo superclassInfo = classNameToClassInfo.get(superclassName);
                    if (superclassInfo == null) {
                        classNameToClassInfo.put(superclassName, new ClassInfo(superclassName, thisClassInfo));
                    } else {
                        superclassInfo.addSubclass(thisClassInfo);
                    }
                }
            }
        }

    }

    private void load(String... packages) {

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

    public ClassInfo getClassSimpleName(String fullOrPartialClassName) {

        ClassInfo match = null;
        for (String fqn : classNameToClassInfo.keySet()) {
            if (fqn.endsWith("." + fullOrPartialClassName) || fqn.equals(fullOrPartialClassName)) {
                if (match == null) {
                    match = classNameToClassInfo.get(fqn);
                } else {
                    throw new MappingException("More than one class has simple name: " + fullOrPartialClassName);
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

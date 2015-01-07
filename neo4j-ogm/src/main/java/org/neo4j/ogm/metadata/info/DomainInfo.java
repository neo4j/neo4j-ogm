package org.neo4j.ogm.metadata.info;

import org.neo4j.ogm.annotation.Transient;
import org.neo4j.ogm.metadata.ClassPathScanner;
import org.neo4j.ogm.metadata.MappingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

public class DomainInfo implements ClassInfoProcessor {

    private static final String dateSignature = "java/util/Date";
    private static final String bigDecimalSignature = "java/math/BigDecimal";
    private static final String bigIntegerSignature = "java/math/BigInteger";

    private final List<String> classPaths = new ArrayList<>();
    private final HashMap<String, ClassInfo> classNameToClassInfo = new HashMap<>();
    private final HashMap<String, InterfaceInfo> interfaceNameToInterfaceInfo = new HashMap<>();
    private final HashMap<String, ArrayList<ClassInfo>> annotationNameToClassInfo = new HashMap<>();
    private final HashMap<String, ArrayList<ClassInfo>> interfaceNameToClassInfo = new HashMap<>();

    private final Set<String> enumTypes = new HashSet<>();

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassInfoProcessor.class);

    public DomainInfo(String... packages) {
        long now = -System.currentTimeMillis();
        load(packages);
        LOGGER.info(classNameToClassInfo.entrySet().size() + " classes loaded in " + (now + System.currentTimeMillis()) + " milliseconds");
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

    private void registerDefaultTypeConverters() {

        for (ClassInfo classInfo : classNameToClassInfo.values()) {
            if (!classInfo.isEnum() && !classInfo.isInterface()) {

                for (FieldInfo fieldInfo : classInfo.fieldsInfo().fields()) {
                    if (!fieldInfo.hasConverter()) {
                        if (fieldInfo.getDescriptor().contains(dateSignature)) {
                            fieldInfo.setConverter(ConvertibleTypes.getDateConverter());
                        }
                        else if (fieldInfo.getDescriptor().contains(bigIntegerSignature)) {
                            fieldInfo.setConverter(ConvertibleTypes.getBigIntegerConverter());
                        }
                        else if (fieldInfo.getDescriptor().contains(bigDecimalSignature)) {
                            fieldInfo.setConverter(ConvertibleTypes.getBigDecimalConverter());
                        }
                        else {
                            for (String enumSignature : enumTypes) {
                                if (fieldInfo.getDescriptor().contains(enumSignature)) {
                                    fieldInfo.setConverter(ConvertibleTypes.getEnumConverter(enumSignature));
                                }
                            }
                        }
                    }
                }

                for (MethodInfo methodInfo : classInfo.methodsInfo().methods()) {
                    if (!methodInfo.hasConverter()) {
                        if (methodInfo.getDescriptor().contains(dateSignature)) {
                            methodInfo.setConverter(ConvertibleTypes.getDateConverter());
                        }
                        else if (methodInfo.getDescriptor().contains(bigIntegerSignature)) {
                            methodInfo.setConverter(ConvertibleTypes.getBigIntegerConverter());
                        }
                        else if (methodInfo.getDescriptor().contains(bigDecimalSignature)) {
                            methodInfo.setConverter(ConvertibleTypes.getBigDecimalConverter());
                        }
                        else {
                            for (String enumSignature : enumTypes) {
                                if (methodInfo.getDescriptor().contains(enumSignature)) {
                                    methodInfo.setConverter(ConvertibleTypes.getEnumConverter(enumSignature));
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void finish() {
        buildAnnotationNameToClassInfoMap();
        registerDefaultTypeConverters();
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
        if (classInfo.annotationsInfo().get(Transient.CLASS) != null) {
            LOGGER.info("Skipping @Transient class: " + classInfo.name());
            return;
        }
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
                if (thisClassInfo.isEnum()) {
                    String enumSignature = thisClassInfo.name().replace(".", "/");
                    LOGGER.info("Registering enum class: " + enumSignature);
                    enumTypes.add(enumSignature);
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

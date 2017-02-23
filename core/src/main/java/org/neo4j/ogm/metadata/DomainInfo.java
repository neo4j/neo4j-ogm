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

package org.neo4j.ogm.metadata;


import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

import org.neo4j.ogm.annotation.typeconversion.Convert;
import org.neo4j.ogm.exception.MappingException;
import org.neo4j.ogm.scanner.ClassPathScanner;
import org.neo4j.ogm.typeconversion.ConversionCallback;
import org.neo4j.ogm.typeconversion.ConversionCallbackRegistry;
import org.neo4j.ogm.typeconversion.ConvertibleTypes;
import org.neo4j.ogm.typeconversion.ProxyAttributeConverter;
import org.neo4j.ogm.utils.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 * @author Mark Angrish
 */
public class DomainInfo implements ClassFileProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassFileProcessor.class);

    private static final String dateSignature = "java/util/Date";
    private static final String bigDecimalSignature = "java/math/BigDecimal";
    private static final String bigIntegerSignature = "java/math/BigInteger";
    private static final String byteArraySignature = "[B";
    private static final String byteArrayWrapperSignature = "[Ljava/lang/Byte";
    private static final String arraySignature = "[L";
    private static final String collectionSignature = "L";

    private final List<String> classPaths = new ArrayList<>();

    private final Map<String, ClassInfo> classNameToClassInfo = new HashMap<>();
    private final Map<String, ArrayList<ClassInfo>> annotationNameToClassInfo = new HashMap<>();
    private final Map<String, ArrayList<ClassInfo>> interfaceNameToClassInfo = new HashMap<>();

    private final Set<Class> enumTypes = new HashSet<>();

    private final ConversionCallbackRegistry conversionCallbackRegistry = new ConversionCallbackRegistry();

    public DomainInfo(String... packages) {
        long startTime = System.nanoTime();
        load(packages);

        LOGGER.info("{} classes loaded in {} nanoseconds", classNameToClassInfo.entrySet().size(), (System.nanoTime() - startTime));
    }

    public DomainInfo(Class... classes) {
        long startTime = System.nanoTime();
        load(classes);

        LOGGER.info("{} classes loaded in {} nanoseconds", classNameToClassInfo.entrySet().size(), (System.nanoTime() - startTime));
    }

    private void buildAnnotationNameToClassInfoMap() {

        LOGGER.info("Building annotation class map");
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

    private void buildInterfaceNameToClassInfoMap() {
        LOGGER.info("Building interface class map for {} classes", classNameToClassInfo.values().size());
        for (ClassInfo classInfo : classNameToClassInfo.values()) {
            LOGGER.debug(" - {} implements {} interfaces", classInfo.simpleName(), classInfo.interfacesInfo().list().size());
            for (InterfaceInfo iface : classInfo.interfacesInfo().list()) {
                ArrayList<ClassInfo> classInfoList = interfaceNameToClassInfo.get(iface.name());
                if (classInfoList == null) {
                    interfaceNameToClassInfo.put(iface.name(), classInfoList = new ArrayList<>());
                }
                LOGGER.debug("   - {}", iface.name());
                classInfoList.add(classInfo);
            }
        }
    }

    public void registerConversionCallback(ConversionCallback conversionCallback) {
        this.conversionCallbackRegistry.registerConversionCallback(conversionCallback);
    }

    @Override
    public void finish() {

        LOGGER.info("Starting Post-processing phase");

        buildAnnotationNameToClassInfoMap();
        buildInterfaceNameToClassInfoMap();

        List<ClassInfo> transientClasses = new ArrayList<>();

        for (ClassInfo classInfo : classNameToClassInfo.values()) {

            if (classInfo.name() == null || classInfo.name().equals("java.lang.Object")) continue;

            LOGGER.debug("Post-processing: {}", classInfo.name());

            if (classInfo.isTransient()) {
                LOGGER.debug(" - Registering @Transient baseclass: {}", classInfo.name());
                transientClasses.add(classInfo);
                continue;
            }

            if (classInfo.superclassName() == null || classInfo.superclassName().equals("java.lang.Object")) {
                extend(classInfo, classInfo.directSubclasses());
            }

            for (InterfaceInfo interfaceInfo : classInfo.interfacesInfo().list()) {
                implement(classInfo, interfaceInfo);
            }
        }

        LOGGER.debug("Checking for @Transient classes....");

        // find transient interfaces
        Collection<ArrayList<ClassInfo>> interfaceInfos = interfaceNameToClassInfo.values();
        for (ArrayList<ClassInfo> classInfos : interfaceInfos) {
            for (ClassInfo classInfo : classInfos) {
                if (classInfo.isTransient()) {
                    LOGGER.debug("Registering @Transient baseclass: {}", classInfo.name());
                    transientClasses.add(classInfo);
                }
            }
        }

        // remove all transient class hierarchies
        Set<Class> transientClassesRemoved = new HashSet<>();
        for (ClassInfo transientClass : transientClasses) {
            transientClassesRemoved.addAll(removeTransientClass(transientClass));
        }

        LOGGER.debug("Registering converters and deregistering transient fields and methods....");
        postProcessFields(transientClassesRemoved);
        postProcessMethods(transientClassesRemoved);

        LOGGER.info("Post-processing complete");
    }

    private void postProcessFields(Set<Class> transientClassesRemoved) {
        for (ClassInfo classInfo : classNameToClassInfo.values()) {
            boolean registerConverters = false;
            if (!classInfo.isEnum() && !classInfo.isInterface()) {
                registerConverters = true;
            }
            Iterator<FieldInfo> fieldInfoIterator = classInfo.fieldsInfo().fields().iterator();
            while (fieldInfoIterator.hasNext()) {
                FieldInfo fieldInfo = fieldInfoIterator.next();
                if (!fieldInfo.persistableAsProperty()) {
                    Class fieldClass = null;
                    try {
                        fieldClass = ClassUtils.getType(fieldInfo.getTypeDescriptor());
                    } catch (Exception e) {
                        LOGGER.debug("Unable to compute class type for " + classInfo.name() + ", field: " + fieldInfo.getName());
                    }
                    if (fieldClass != null && transientClassesRemoved.contains(fieldClass)) {
                        fieldInfoIterator.remove();
                        continue;
                    }
                }
                if (registerConverters) {
                    registerDefaultFieldConverters(classInfo, fieldInfo);
                }
            }
        }
    }

    private void postProcessMethods(Set<Class> transientClassesRemoved) {
        for (ClassInfo classInfo : classNameToClassInfo.values()) {
            boolean registerConverters = false;
            if (!classInfo.isEnum() && !classInfo.isInterface()) {
                registerConverters = true;
            }
            Iterator<MethodInfo> methodInfoIterator = classInfo.methodsInfo().methods().iterator();
            while (methodInfoIterator.hasNext()) {
                MethodInfo methodInfo = methodInfoIterator.next();
                if (!methodInfo.isSimpleGetter() && !methodInfo.isSimpleSetter()) {
                    Class methodClass = null;
                    try {
                        methodClass = ClassUtils.getType(methodInfo.getTypeDescriptor());
                    } catch (Exception e) {
                        LOGGER.debug("Unable to compute class type for " + classInfo.name() + ", method: " + methodInfo.getName());
                    }
                    if (methodClass != null && transientClassesRemoved.contains(methodClass)) {
                        methodInfoIterator.remove();
                        classInfo.methodsInfo().removeGettersAndSetters(methodInfo);
                        continue;
                    }
                }
            }
        }
    }

    private Set<Class> removeTransientClass(ClassInfo transientClass) {
        Set<Class> removed = new HashSet<>();
        if (transientClass != null && !transientClass.name().equals("java.lang.Object")) {
            LOGGER.debug("Removing @Transient class: {}", transientClass.name());
            classNameToClassInfo.remove(transientClass.name());
            removed.add(transientClass.getUnderlyingClass());
            for (ClassInfo transientChild : transientClass.directSubclasses()) {
                removeTransientClass(transientChild);
            }
            for (ClassInfo transientChild : transientClass.directImplementingClasses()) {
                removeTransientClass(transientChild);
            }
        }
        return removed;
    }


    private void extend(ClassInfo superclass, List<ClassInfo> subclasses) {
        for (ClassInfo subclass : subclasses) {
            subclass.extend(superclass);
            extend(subclass, subclass.directSubclasses());
        }
    }

    private void implement(ClassInfo implementingClass, InterfaceInfo interfaceInfo) {

        ClassInfo interfaceClass = classNameToClassInfo.get(interfaceInfo.name());

        if (interfaceClass != null) {
            if (!implementingClass.directInterfaces().contains(interfaceClass)) {
                LOGGER.debug(" - Setting {} implements {}", implementingClass.simpleName(), interfaceClass.simpleName());
                implementingClass.directInterfaces().add(interfaceClass);
            }

            if (!interfaceClass.directImplementingClasses().contains(implementingClass)) {
                interfaceClass.directImplementingClasses().add(implementingClass);
            }

            for (ClassInfo subClassInfo : implementingClass.directSubclasses()) {
                implement(subClassInfo, interfaceInfo);
            }
        } else {
            LOGGER.debug(" - No ClassInfo found for interface class: {}", interfaceInfo.name());
        }
    }

    @Override
    public void process(final InputStream inputStream) throws IOException {

        ClassInfo classInfo = new ClassInfo(inputStream);

        String className = classInfo.name();
        String superclassName = classInfo.superclassName();

        LOGGER.debug("Processing: {} -> {}", className, superclassName);

        if (className != null) {

            ClassInfo thisClassInfo = classNameToClassInfo.computeIfAbsent(className, k -> classInfo);

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
                LOGGER.debug("Registering enum class: {}", thisClassInfo.name());
                enumTypes.add(thisClassInfo.getUnderlyingClass());
            }
        }
    }

    private void load(Class... classes) {
        classPaths.clear();
        classNameToClassInfo.clear();
        annotationNameToClassInfo.clear();
        interfaceNameToClassInfo.clear();

        for (Class clazz : classes) {
            // This can be done as all OGM managed classes must have different "simple names"'s.
            final URL resource = clazz.getResource(clazz.getSimpleName() + ".class");

            try (InputStream inputStream = resource.openStream()) {
                process(inputStream);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void load(String... packages) {
        classPaths.clear();
        classNameToClassInfo.clear();
        annotationNameToClassInfo.clear();
        interfaceNameToClassInfo.clear();

        for (String packageName : packages) {
            String path = packageName.replace(".", "/");
            // ensure classpath entries are complete, to ensure we don't accidentally admit partial matches.
            if (!path.endsWith("/")) {
                path = path.concat("/");
            }
            classPaths.add(path);
        }

        new ClassPathScanner().scan(classPaths, this);
    }

    public ClassInfo getClass(String fqn) {
        return classNameToClassInfo.get(fqn);
    }

    // all classes, including interfaces will be registered in classNameToClassInfo map
    public ClassInfo getClassSimpleName(String fullOrPartialClassName) {
        return getClassInfo(fullOrPartialClassName, classNameToClassInfo);
    }


    public ClassInfo getClassInfoForInterface(String fullOrPartialClassName) {
        ClassInfo classInfo = getClassSimpleName(fullOrPartialClassName);
        if (classInfo != null && classInfo.isInterface()) {
            return classInfo;
        }
        return null;
    }

    private ClassInfo getClassInfo(String fullOrPartialClassName, Map<String, ClassInfo> infos) {
        ClassInfo match = null;
        for (String fqn : infos.keySet()) {
            if (fqn.endsWith("." + fullOrPartialClassName) || fqn.equals(fullOrPartialClassName)) {
                if (match == null) {
                    match = infos.get(fqn);
                } else {
                    throw new MappingException("More than one class has simple name: " + fullOrPartialClassName);
                }
            }
        }
        return match;
    }

    public List<ClassInfo> getClassInfosWithAnnotation(String annotation) {
        return annotationNameToClassInfo.get(annotation);
    }

    private void setEnumMethodConverter(MethodInfo methodInfo, Class enumClass) {
        if (methodInfo.isArray()) {
            methodInfo.setPropertyConverter(ConvertibleTypes.getEnumArrayConverter(enumClass));
        } else if (methodInfo.isIterable()) {
            methodInfo.setPropertyConverter(ConvertibleTypes.getEnumCollectionConverter(enumClass, methodInfo.getCollectionClassname()));
        } else {
            methodInfo.setPropertyConverter(ConvertibleTypes.getEnumConverter(enumClass));
        }
    }

    private void setBigDecimalMethodConverter(MethodInfo methodInfo) {
        if (methodInfo.isArray()) {
            methodInfo.setPropertyConverter(ConvertibleTypes.getBigDecimalArrayConverter());
        } else if (methodInfo.isIterable()) {
            methodInfo.setPropertyConverter(ConvertibleTypes.getBigDecimalCollectionConverter(methodInfo.getCollectionClassname()));
        } else {
            methodInfo.setPropertyConverter(ConvertibleTypes.getBigDecimalConverter());
        }
    }

    private void setBigIntegerMethodConverter(MethodInfo methodInfo) {
        if (methodInfo.isArray()) {
            methodInfo.setPropertyConverter(ConvertibleTypes.getBigIntegerArrayConverter());
        } else if (methodInfo.isIterable()) {
            methodInfo.setPropertyConverter(ConvertibleTypes.getBigIntegerCollectionConverter(methodInfo.getCollectionClassname()));
        } else {
            methodInfo.setPropertyConverter(ConvertibleTypes.getBigIntegerConverter());
        }
    }

    private void setDateMethodConverter(MethodInfo methodInfo) {
        if (methodInfo.isArray()) {
            methodInfo.setPropertyConverter(ConvertibleTypes.getDateArrayConverter());
        } else if (methodInfo.isIterable()) {
            methodInfo.setPropertyConverter(ConvertibleTypes.getDateCollectionConverter(methodInfo.getCollectionClassname()));
        } else {
            methodInfo.setPropertyConverter(ConvertibleTypes.getDateConverter());
        }
    }

    private void registerDefaultFieldConverters(ClassInfo classInfo, FieldInfo fieldInfo) {

        if (!fieldInfo.hasPropertyConverter() && !fieldInfo.hasCompositeConverter()) {

            if (fieldInfo.getTypeDescriptor().contains(dateSignature)) {
                setDateFieldConverter(fieldInfo);
            } else if (fieldInfo.getTypeDescriptor().contains(bigIntegerSignature)) {
                setBigIntegerFieldConverter(fieldInfo);
            } else if (fieldInfo.getTypeDescriptor().contains(bigDecimalSignature)) {
                setBigDecimalConverter(fieldInfo);
            } else if (fieldInfo.getTypeDescriptor().contains(byteArraySignature)) {
                fieldInfo.setPropertyConverter(ConvertibleTypes.getByteArrayBase64Converter());
            } else if (fieldInfo.getTypeDescriptor().contains(byteArrayWrapperSignature)) {
                fieldInfo.setPropertyConverter(ConvertibleTypes.getByteArrayWrapperBase64Converter());
            } else {
                if (fieldInfo.getAnnotations().get(Convert.class) != null) {
                    // no converter's been set but this method is annotated with @Convert so we need to proxy it
                    Class<?> entityAttributeType = ClassUtils.getType(fieldInfo.getTypeDescriptor());
                    String graphTypeDescriptor = fieldInfo.getAnnotations().get(Convert.class).get(Convert.GRAPH_TYPE, null);
                    if (graphTypeDescriptor == null) {
                        throw new MappingException("Found annotation to convert a " + entityAttributeType.getName()
                                + " on " + classInfo.name() + '.' + fieldInfo.getName()
                                + " but no target graph property type or specific AttributeConverter have been specified.");
                    }
                    fieldInfo.setPropertyConverter(new ProxyAttributeConverter(entityAttributeType, ClassUtils.getType(graphTypeDescriptor), this.conversionCallbackRegistry));
                }

                Class fieldType = ClassUtils.getType(fieldInfo.getTypeDescriptor());

                boolean enumConverterSet = false;
                for (Class enumClass : enumTypes) {
                    if (fieldType.equals(enumClass)) {
                        setEnumFieldConverter(fieldInfo, enumClass);
                        enumConverterSet = true;
                        break;
                    }
                }

                if (!enumConverterSet) {
                    if (fieldType.isEnum()) {
                        LOGGER.debug("Setting default enum converter for unscanned class " + classInfo.name() + ", field: " + fieldInfo.getName());
                        setEnumFieldConverter(fieldInfo, fieldType);
                    }
                }
            }
        }
    }


    private void setEnumFieldConverter(FieldInfo fieldInfo, Class enumClass) {
        if (fieldInfo.isArray()) {
            fieldInfo.setPropertyConverter(ConvertibleTypes.getEnumArrayConverter(enumClass));
        } else if (fieldInfo.isIterable()) {
            fieldInfo.setPropertyConverter(ConvertibleTypes.getEnumCollectionConverter(enumClass, fieldInfo.getCollectionClassname()));
        } else {
            fieldInfo.setPropertyConverter(ConvertibleTypes.getEnumConverter(enumClass));
        }
    }

    private void setBigDecimalConverter(FieldInfo fieldInfo) {
        if (fieldInfo.isArray()) {
            fieldInfo.setPropertyConverter(ConvertibleTypes.getBigDecimalArrayConverter());
        } else if (fieldInfo.isIterable()) {
            fieldInfo.setPropertyConverter(ConvertibleTypes.getBigDecimalCollectionConverter(fieldInfo.getCollectionClassname()));
        } else {
            fieldInfo.setPropertyConverter(ConvertibleTypes.getBigDecimalConverter());
        }
    }

    private void setBigIntegerFieldConverter(FieldInfo fieldInfo) {
        if (fieldInfo.isArray()) {
            fieldInfo.setPropertyConverter(ConvertibleTypes.getBigIntegerArrayConverter());
        } else if (fieldInfo.isIterable()) {
            fieldInfo.setPropertyConverter(ConvertibleTypes.getBigIntegerCollectionConverter(fieldInfo.getCollectionClassname()));
        } else {
            fieldInfo.setPropertyConverter(ConvertibleTypes.getBigIntegerConverter());
        }
    }

    private void setDateFieldConverter(FieldInfo fieldInfo) {
        if (fieldInfo.isArray()) {
            fieldInfo.setPropertyConverter(ConvertibleTypes.getDateArrayConverter());
        } else if (fieldInfo.isIterable()) {
            fieldInfo.setPropertyConverter(ConvertibleTypes.getDateCollectionConverter(fieldInfo.getCollectionClassname()));
        } else {
            fieldInfo.setPropertyConverter(ConvertibleTypes.getDateConverter());
        }
    }

    // leaky for spring
    public Map<String, ClassInfo> getClassInfoMap() {
        return classNameToClassInfo;
    }

    public List<ClassInfo> getClassInfos(String interfaceName) {
        return interfaceNameToClassInfo.get(interfaceName);
    }
}

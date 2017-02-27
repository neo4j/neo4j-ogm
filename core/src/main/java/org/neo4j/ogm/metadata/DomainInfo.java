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
import org.neo4j.ogm.classloader.ClassPathScanner;
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

    private final Map<String, ClassMetadata> classNameToClassInfo = new HashMap<>();
    private final Map<String, ArrayList<ClassMetadata>> annotationNameToClassInfo = new HashMap<>();
    private final Map<String, ArrayList<ClassMetadata>> interfaceNameToClassInfo = new HashMap<>();

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
        for (ClassMetadata classInfo : classNameToClassInfo.values()) {
            for (AnnotationInfo annotation : classInfo.annotations()) {
                ArrayList<ClassMetadata> classInfoList = annotationNameToClassInfo.get(annotation.getName());
                if (classInfoList == null) {
                    annotationNameToClassInfo.put(annotation.getName(), classInfoList = new ArrayList<>());
                }
                classInfoList.add(classInfo);
            }
        }
    }

    private void buildInterfaceNameToClassInfoMap() {
        LOGGER.info("Building interface class map for {} classes", classNameToClassInfo.values().size());
        for (ClassMetadata classInfo : classNameToClassInfo.values()) {
            LOGGER.debug(" - {} implements {} interfaces", classInfo.simpleName(), classInfo.interfacesInfo().list().size());
            for (InterfaceInfo iface : classInfo.interfacesInfo().list()) {
                ArrayList<ClassMetadata> classInfoList = interfaceNameToClassInfo.get(iface.name());
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

        List<ClassMetadata> transientClasses = new ArrayList<>();

        for (ClassMetadata classInfo : classNameToClassInfo.values()) {

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
        Collection<ArrayList<ClassMetadata>> interfaceInfos = interfaceNameToClassInfo.values();
        for (ArrayList<ClassMetadata> classInfos : interfaceInfos) {
            for (ClassMetadata classInfo : classInfos) {
                if (classInfo.isTransient()) {
                    LOGGER.debug("Registering @Transient baseclass: {}", classInfo.name());
                    transientClasses.add(classInfo);
                }
            }
        }

        // remove all transient class hierarchies
        Set<Class> transientClassesRemoved = new HashSet<>();
        for (ClassMetadata transientClass : transientClasses) {
            transientClassesRemoved.addAll(removeTransientClass(transientClass));
        }

        LOGGER.debug("Registering converters and deregistering transient fields and methods....");
        postProcessFields(transientClassesRemoved);

        LOGGER.info("Post-processing complete");
    }

    private void postProcessFields(Set<Class> transientClassesRemoved) {
        for (ClassMetadata classInfo : classNameToClassInfo.values()) {
            boolean registerConverters = false;
            if (!classInfo.isEnum() && !classInfo.isInterface()) {
                registerConverters = true;
            }
            Iterator<FieldMetadata> fieldInfoIterator = classInfo.fieldsInfo().fields().iterator();
            while (fieldInfoIterator.hasNext()) {
                FieldMetadata fieldInfo = fieldInfoIterator.next();
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


    private Set<Class> removeTransientClass(ClassMetadata transientClass) {
        Set<Class> removed = new HashSet<>();
        if (transientClass != null && !transientClass.name().equals("java.lang.Object")) {
            LOGGER.debug("Removing @Transient class: {}", transientClass.name());
            classNameToClassInfo.remove(transientClass.name());
            removed.add(transientClass.getUnderlyingClass());
            for (ClassMetadata transientChild : transientClass.directSubclasses()) {
                removeTransientClass(transientChild);
            }
            for (ClassMetadata transientChild : transientClass.directImplementingClasses()) {
                removeTransientClass(transientChild);
            }
        }
        return removed;
    }


    private void extend(ClassMetadata superclass, List<ClassMetadata> subclasses) {
        for (ClassMetadata subclass : subclasses) {
            subclass.extend(superclass);
            extend(subclass, subclass.directSubclasses());
        }
    }

    private void implement(ClassMetadata implementingClass, InterfaceInfo interfaceInfo) {

        ClassMetadata interfaceClass = classNameToClassInfo.get(interfaceInfo.name());

        if (interfaceClass != null) {
            if (!implementingClass.directInterfaces().contains(interfaceClass)) {
                LOGGER.debug(" - Setting {} implements {}", implementingClass.simpleName(), interfaceClass.simpleName());
                implementingClass.directInterfaces().add(interfaceClass);
            }

            if (!interfaceClass.directImplementingClasses().contains(implementingClass)) {
                interfaceClass.directImplementingClasses().add(implementingClass);
            }

            for (ClassMetadata subClassInfo : implementingClass.directSubclasses()) {
                implement(subClassInfo, interfaceInfo);
            }
        } else {
            LOGGER.debug(" - No ClassInfo found for interface class: {}", interfaceInfo.name());
        }
    }

    @Override
    public void process(final InputStream inputStream) throws IOException {

        ClassMetadata classInfo = new ClassMetadata(inputStream);

        String className = classInfo.name();
        String superclassName = classInfo.superclassName();

        LOGGER.debug("Processing: {} -> {}", className, superclassName);

        if (className != null) {

            ClassMetadata thisClassInfo = classNameToClassInfo.computeIfAbsent(className, k -> classInfo);

            if (!thisClassInfo.hydrated()) {

                thisClassInfo.hydrate(classInfo);

                ClassMetadata superclassInfo = classNameToClassInfo.get(superclassName);
                if (superclassInfo == null) {
                    classNameToClassInfo.put(superclassName, new ClassMetadata(superclassName, thisClassInfo));
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

    public ClassMetadata getClass(String fqn) {
        return classNameToClassInfo.get(fqn);
    }

    // all classes, including interfaces will be registered in classNameToClassInfo map
    public ClassMetadata getClassSimpleName(String fullOrPartialClassName) {
        return getClassInfo(fullOrPartialClassName, classNameToClassInfo);
    }


    public ClassMetadata getClassInfoForInterface(String fullOrPartialClassName) {
        ClassMetadata classInfo = getClassSimpleName(fullOrPartialClassName);
        if (classInfo != null && classInfo.isInterface()) {
            return classInfo;
        }
        return null;
    }

    private ClassMetadata getClassInfo(String fullOrPartialClassName, Map<String, ClassMetadata> infos) {
        ClassMetadata match = null;
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

    public List<ClassMetadata> getClassInfosWithAnnotation(String annotation) {
        return annotationNameToClassInfo.get(annotation);
    }

    private void registerDefaultFieldConverters(ClassMetadata classInfo, FieldMetadata fieldInfo) {

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


    private void setEnumFieldConverter(FieldMetadata fieldInfo, Class enumClass) {
        if (fieldInfo.isArray()) {
            fieldInfo.setPropertyConverter(ConvertibleTypes.getEnumArrayConverter(enumClass));
        } else if (fieldInfo.isIterable()) {
            fieldInfo.setPropertyConverter(ConvertibleTypes.getEnumCollectionConverter(enumClass, fieldInfo.getCollectionClassname()));
        } else {
            fieldInfo.setPropertyConverter(ConvertibleTypes.getEnumConverter(enumClass));
        }
    }

    private void setBigDecimalConverter(FieldMetadata fieldInfo) {
        if (fieldInfo.isArray()) {
            fieldInfo.setPropertyConverter(ConvertibleTypes.getBigDecimalArrayConverter());
        } else if (fieldInfo.isIterable()) {
            fieldInfo.setPropertyConverter(ConvertibleTypes.getBigDecimalCollectionConverter(fieldInfo.getCollectionClassname()));
        } else {
            fieldInfo.setPropertyConverter(ConvertibleTypes.getBigDecimalConverter());
        }
    }

    private void setBigIntegerFieldConverter(FieldMetadata fieldInfo) {
        if (fieldInfo.isArray()) {
            fieldInfo.setPropertyConverter(ConvertibleTypes.getBigIntegerArrayConverter());
        } else if (fieldInfo.isIterable()) {
            fieldInfo.setPropertyConverter(ConvertibleTypes.getBigIntegerCollectionConverter(fieldInfo.getCollectionClassname()));
        } else {
            fieldInfo.setPropertyConverter(ConvertibleTypes.getBigIntegerConverter());
        }
    }

    private void setDateFieldConverter(FieldMetadata fieldInfo) {
        if (fieldInfo.isArray()) {
            fieldInfo.setPropertyConverter(ConvertibleTypes.getDateArrayConverter());
        } else if (fieldInfo.isIterable()) {
            fieldInfo.setPropertyConverter(ConvertibleTypes.getDateCollectionConverter(fieldInfo.getCollectionClassname()));
        } else {
            fieldInfo.setPropertyConverter(ConvertibleTypes.getDateConverter());
        }
    }

    // leaky for spring
    public Map<String, ClassMetadata> getClassInfoMap() {
        return classNameToClassInfo;
    }

    public List<ClassMetadata> getClassInfos(String interfaceName) {
        return interfaceNameToClassInfo.get(interfaceName);
    }
}

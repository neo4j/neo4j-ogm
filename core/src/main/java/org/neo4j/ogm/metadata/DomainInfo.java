/*
 * Copyright (c) 2002-2018 "Neo Technology,"
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

import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import io.github.lukehutch.fastclasspathscanner.scanner.ScanResult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.neo4j.ogm.annotation.typeconversion.Convert;
import org.neo4j.ogm.exception.core.MappingException;
import org.neo4j.ogm.typeconversion.AttributeConverter;
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
public class DomainInfo {

    private static final Logger LOGGER = LoggerFactory.getLogger(DomainInfo.class);
    private static final String DATE_SIGNATURE = "java.util.Date";
    private static final String BIG_DECIMAL_SIGNATURE = "java.math.BigDecimal";
    private static final String BIG_INTEGER_SIGNATURE = "java.math.BigInteger";
    private static final String BYTE_ARRAY_SIGNATURE = "byte[]";
    private static final String BYTE_ARRAY_WRAPPER_SIGNATURE = "java.lang.Byte[]";
    private static final String INSTANT_SIGNATURE = "java.time.Instant";
    private static final String LOCAL_DATE_SIGNATURE = "java.time.LocalDate";
    private static final String LOCAL_DATE_TIME_SIGNATURE = "java.time.LocalDateTime";
    private static final String OFFSET_DATE_TIME_SIGNATURE = "java.time.OffsetDateTime";

    private final Map<String, ClassInfo> classNameToClassInfo = new HashMap<>();
    private final Map<String, ArrayList<ClassInfo>> annotationNameToClassInfo = new HashMap<>();
    private final Map<String, ArrayList<ClassInfo>> interfaceNameToClassInfo = new HashMap<>();
    private final Set<Class> enumTypes = new HashSet<>();
    private final ConversionCallbackRegistry conversionCallbackRegistry = new ConversionCallbackRegistry();

    public static DomainInfo create(String... packages) {

        ScanResult scanResult = new FastClasspathScanner(packages)
            .strictWhitelist()
            .scan();

        List<String> allClasses = scanResult.getNamesOfAllClasses();

        DomainInfo domainInfo = new DomainInfo();

        for (String className : allClasses) {
            Class<?> cls = null;
            try {
                cls = Class.forName(className, false, Thread.currentThread().getContextClassLoader());
            } catch (ClassNotFoundException e) {
                LOGGER.warn("Could not load class {}", className);
                continue;
            }

            ClassInfo classInfo = new ClassInfo(cls);

            String superclassName = classInfo.superclassName();

            LOGGER.debug("Processing: {} -> {}", className, superclassName);

            if (className != null) {
                if (cls.isAnnotation() || cls.isAnonymousClass() || cls.equals(Object.class)) {
                    continue;
                }

                ClassInfo thisClassInfo = domainInfo.classNameToClassInfo.computeIfAbsent(className, k -> classInfo);

                if (!thisClassInfo.hydrated()) {

                    thisClassInfo.hydrate(classInfo);

                    ClassInfo superclassInfo = domainInfo.classNameToClassInfo.get(superclassName);
                    if (superclassInfo == null) {

                        if (superclassName != null && !superclassName.equals("java.lang.Object") && !superclassName
                            .equals("java.lang.Enum")) {
                            domainInfo.classNameToClassInfo
                                .put(superclassName, new ClassInfo(superclassName, thisClassInfo));
                        }
                    } else {
                        superclassInfo.addSubclass(thisClassInfo);
                    }
                }

                if (thisClassInfo.isEnum()) {
                    LOGGER.debug("Registering enum class: {}", thisClassInfo.name());
                    domainInfo.enumTypes.add(thisClassInfo.getUnderlyingClass());
                }
            }
        }

        domainInfo.finish();

        return domainInfo;
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
            LOGGER.debug(" - {} implements {} interfaces", classInfo.simpleName(),
                classInfo.interfacesInfo().list().size());
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

    void registerConversionCallback(ConversionCallback conversionCallback) {
        this.conversionCallbackRegistry.registerConversionCallback(conversionCallback);
    }

    private void finish() {

        LOGGER.info("Starting Post-processing phase");

        buildAnnotationNameToClassInfoMap();
        buildInterfaceNameToClassInfoMap();

        List<ClassInfo> transientClasses = new ArrayList<>();

        for (ClassInfo classInfo : classNameToClassInfo.values()) {

            if (classInfo.name() == null || classInfo.name().equals("java.lang.Object"))
                continue;

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

        for (ClassInfo classInfo : classNameToClassInfo.values()) {
            classInfo.primaryIndexField();
            /*if (classInfo.primaryIndexField() == null && classInfo.identityField() == null &&
                    !classInfo.isInterface()) {
                throw new MetadataException("No id field found for " + classInfo.name() + " , provide either "
                        + "natural or native id field.");
            }*/

        }
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
                        LOGGER.debug(
                            "Unable to compute class type for " + classInfo.name() + ", field: " + fieldInfo.getName());
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
                LOGGER
                    .debug(" - Setting {} implements {}", implementingClass.simpleName(), interfaceClass.simpleName());
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

    public ClassInfo getClass(String fqn) {
        return classNameToClassInfo.get(fqn);
    }

    // all classes, including interfaces will be registered in classNameToClassInfo map
    ClassInfo getClassSimpleName(String fullOrPartialClassName) {
        return getClassInfo(fullOrPartialClassName, classNameToClassInfo);
    }

    ClassInfo getClassInfoForInterface(String fullOrPartialClassName) {
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

    List<ClassInfo> getClassInfosWithAnnotation(String annotation) {
        return annotationNameToClassInfo.get(annotation);
    }

    private void registerDefaultFieldConverters(ClassInfo classInfo, FieldInfo fieldInfo) {

        if (!fieldInfo.hasPropertyConverter() && !fieldInfo.hasCompositeConverter()) {

            final String typeDescriptor = fieldInfo.getTypeDescriptor();
            if (typeDescriptor.contains(DATE_SIGNATURE)) {
                setDateFieldConverter(fieldInfo);
            } else if (typeDescriptor.contains(BIG_INTEGER_SIGNATURE)) {
                setBigIntegerFieldConverter(fieldInfo);
            } else if (typeDescriptor.contains(BIG_DECIMAL_SIGNATURE)) {
                setBigDecimalConverter(fieldInfo);
            } else if (typeDescriptor.contains(INSTANT_SIGNATURE)) {
                setInstantConverter(fieldInfo);
            } else if (typeDescriptor.contains(OFFSET_DATE_TIME_SIGNATURE)) {
                setOffsetDateTimeConverter(fieldInfo);
            } else if (typeDescriptor.contains(LOCAL_DATE_TIME_SIGNATURE)) {
                // order for LOCAL_DATE_TIME and LOCAL_DATE is important here - LOCAL_DATE is a substring so we try to
                // match LOCAL_DATE_TIME first
                setLocalDateTimeConverter(fieldInfo);
            } else if (typeDescriptor.contains(LOCAL_DATE_SIGNATURE)) {
                setLocalDateConverter(fieldInfo);
            } else if (typeDescriptor.contains(BYTE_ARRAY_SIGNATURE)) {
                fieldInfo.setPropertyConverter(ConvertibleTypes.getByteArrayBase64Converter());
            } else if (typeDescriptor.contains(BYTE_ARRAY_WRAPPER_SIGNATURE)) {
                fieldInfo.setPropertyConverter(ConvertibleTypes.getByteArrayWrapperBase64Converter());
            } else {
                if (fieldInfo.getAnnotations().get(Convert.class) != null) {
                    // no converter's been set but this method is annotated with @Convert so we need to proxy it
                    Class<?> entityAttributeType = ClassUtils.getType(typeDescriptor);
                    String graphTypeDescriptor = fieldInfo.getAnnotations().get(Convert.class)
                        .get(Convert.GRAPH_TYPE, null);
                    if (graphTypeDescriptor == null) {
                        throw new MappingException("Found annotation to convert a " + (entityAttributeType != null ?
                            entityAttributeType.getName() :
                            " null object ")
                            + " on " + classInfo.name() + '.' + fieldInfo.getName()
                            + " but no target graph property type or specific AttributeConverter have been specified.");
                    }
                    fieldInfo.setPropertyConverter(
                        new ProxyAttributeConverter(entityAttributeType, ClassUtils.getType(graphTypeDescriptor),
                            this.conversionCallbackRegistry));
                }

                Class fieldType = ClassUtils.getType(typeDescriptor);

                if (fieldType == null) {
                    throw new RuntimeException(
                        "Class " + classInfo.name() + " field " + fieldInfo.getName() + " has null field type.");
                }

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
                        LOGGER.debug(
                            "Setting default enum converter for unscanned class " + classInfo.name() + ", field: "
                                + fieldInfo.getName());
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
            fieldInfo.setPropertyConverter(
                ConvertibleTypes.getEnumCollectionConverter(enumClass, fieldInfo.getCollectionClassname()));
        } else {
            fieldInfo.setPropertyConverter(ConvertibleTypes.getEnumConverter(enumClass));
        }
    }

    private void setBigDecimalConverter(FieldInfo fieldInfo) {
        if (fieldInfo.isArray()) {
            fieldInfo.setPropertyConverter(ConvertibleTypes.getBigDecimalArrayConverter());
        } else if (fieldInfo.isIterable()) {
            fieldInfo.setPropertyConverter(
                ConvertibleTypes.getBigDecimalCollectionConverter(fieldInfo.getCollectionClassname()));
        } else {
            fieldInfo.setPropertyConverter(ConvertibleTypes.getBigDecimalConverter());
        }
    }

    private void setBigIntegerFieldConverter(FieldInfo fieldInfo) {
        if (fieldInfo.isArray()) {
            fieldInfo.setPropertyConverter(ConvertibleTypes.getBigIntegerArrayConverter());
        } else if (fieldInfo.isIterable()) {
            fieldInfo.setPropertyConverter(
                ConvertibleTypes.getBigIntegerCollectionConverter(fieldInfo.getCollectionClassname()));
        } else {
            fieldInfo.setPropertyConverter(ConvertibleTypes.getBigIntegerConverter());
        }
    }

    private void setDateFieldConverter(FieldInfo fieldInfo) {
        if (fieldInfo.isArray()) {
            fieldInfo.setPropertyConverter(ConvertibleTypes.getDateArrayConverter());
        } else if (fieldInfo.isIterable()) {
            fieldInfo
                .setPropertyConverter(ConvertibleTypes.getDateCollectionConverter(fieldInfo.getCollectionClassname()));
        } else {
            fieldInfo.setPropertyConverter(ConvertibleTypes.getDateConverter());
        }
    }

    private void setInstantConverter(FieldInfo fieldInfo) {
        fieldInfo.setPropertyConverter(ConvertibleTypes.getInstantConverter());
    }

    private void setLocalDateConverter(FieldInfo fieldInfo) {
        AttributeConverter<?, ?> localDateConverter = ConvertibleTypes.getLocalDateConverter();
        if (fieldInfo.isIterable()) {
            fieldInfo.setPropertyConverter(
                ConvertibleTypes.getConverterBasedCollectionConverter(localDateConverter,
                    fieldInfo.getCollectionClassname())
            );
        } else {
            fieldInfo.setPropertyConverter(localDateConverter);
        }
    }

    private void setLocalDateTimeConverter(FieldInfo fieldInfo) {
        AttributeConverter<?, ?> localDateTimeConverter = ConvertibleTypes.getLocalDateTimeConverter();
        if (fieldInfo.isIterable()) {
            fieldInfo.setPropertyConverter(ConvertibleTypes.getConverterBasedCollectionConverter(
                localDateTimeConverter,
                fieldInfo.getCollectionClassname())
            );
        } else {
            fieldInfo.setPropertyConverter(localDateTimeConverter);
        }
    }

    private void setOffsetDateTimeConverter(FieldInfo fieldInfo) {
        AttributeConverter<?, ?> offsetDateTimeConverter = ConvertibleTypes.getOffsetDateTimeConverter();
        if (fieldInfo.isIterable()) {
            fieldInfo.setPropertyConverter(ConvertibleTypes.getConverterBasedCollectionConverter(
                offsetDateTimeConverter,
                fieldInfo.getCollectionClassname()
            ));
        } else {
            fieldInfo.setPropertyConverter(offsetDateTimeConverter);
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

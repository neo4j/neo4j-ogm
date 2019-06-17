/*
 * Copyright (c) 2002-2019 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neo4j.ogm.metadata;

import static java.util.Comparator.*;

import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import io.github.lukehutch.fastclasspathscanner.scanner.ScanResult;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.neo4j.ogm.annotation.typeconversion.Convert;
import org.neo4j.ogm.driver.TypeSystem;
import org.neo4j.ogm.driver.TypeSystem.NoNativeTypes;
import org.neo4j.ogm.exception.core.MappingException;
import org.neo4j.ogm.typeconversion.AttributeConverter;
import org.neo4j.ogm.typeconversion.AttributeConverters;
import org.neo4j.ogm.typeconversion.ConversionCallback;
import org.neo4j.ogm.typeconversion.ConversionCallbackRegistry;
import org.neo4j.ogm.typeconversion.ConvertibleTypes;
import org.neo4j.ogm.typeconversion.ProxyAttributeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 * @author Mark Angrish
 * @author Michael J. Simons
 */
public class DomainInfo {

    private static final Logger LOGGER = LoggerFactory.getLogger(DomainInfo.class);
    private final TypeSystem typeSystem;

    private final Map<String, ClassInfo> classNameToClassInfo = new HashMap<>();
    private final Map<String, ArrayList<ClassInfo>> annotationNameToClassInfo = new HashMap<>();
    private final Map<String, ArrayList<ClassInfo>> interfaceNameToClassInfo = new HashMap<>();
    private final Set<Class> enumTypes = new HashSet<>();
    private final ConversionCallbackRegistry conversionCallbackRegistry = new ConversionCallbackRegistry();

    public DomainInfo(TypeSystem typeSystem) {
        this.typeSystem = typeSystem;
    }

    public static DomainInfo create(String... packages) {
        return create(NoNativeTypes.INSTANCE, packages);
    }

    public static DomainInfo create(TypeSystem typeSystem, String... packages) {

        ScanResult scanResult = new FastClasspathScanner(packages)
            .strictWhitelist()
            .scan();

        DomainInfo domainInfo = new DomainInfo(typeSystem);

        Predicate<Class<?>> classCouldBeLoaded = clazz -> clazz != null;
        Predicate<Class<?>> classIsMappable = clazz -> !(clazz.isAnnotation() || clazz.isAnonymousClass() || clazz
            .equals(Object.class));

        Map<String, Class<?>> mappableClasses = scanResult.getNamesOfAllClasses()
            .stream()
            .map(DomainInfo::loadClass)
            .filter(classCouldBeLoaded)
            .filter(classIsMappable)
            .collect(Collectors.toMap(Class::getName, Function.identity()));

        mappableClasses.values()
            .forEach(clazz -> prepareClass(domainInfo, mappableClasses, typeSystem, clazz));

        domainInfo.finish();

        return domainInfo;
    }

    /**
     * Prepares and hydrates a class. If the class has super classes that have not been scan, this method modifies loads
     * and prepares the super classes recursively and adds it to {@link DomainInfo#classNameToClassInfo}.
     *
     * @param domainInfo
     * @param mappableClasses
     * @param typeSystem
     * @param clazz
     */
    static void prepareClass(
        DomainInfo domainInfo, Map<String, Class<?>> mappableClasses, TypeSystem typeSystem, Class clazz) {
        ClassInfo newClassInfo = new ClassInfo(clazz, typeSystem);
        String className = newClassInfo.name();
        String superclassName = newClassInfo.superclassName();

        LOGGER.debug("Processing: {} -> {}", className, superclassName);

        ClassInfo classInfo = domainInfo.classNameToClassInfo.computeIfAbsent(className, k -> newClassInfo);
        if (!classInfo.hydrated()) {
            classInfo.hydrate(newClassInfo);
        }

        if (superclassName != null) {
            ClassInfo superclassInfo = domainInfo.classNameToClassInfo.get(superclassName);
            if (superclassInfo != null) {
                superclassInfo.addSubclass(classInfo);
            } else if (!"java.lang.Object".equals(superclassName) && !"java.lang.Enum".equals(superclassName)) {

                Class<?> superClazz = Optional.ofNullable(mappableClasses.get(superclassName))
                    // This is the case when a class outside the scanned packages is referred to
                    .orElseGet(() -> (Class) loadClass(superclassName));

                superclassInfo = new ClassInfo(superClazz, classInfo);
                domainInfo.classNameToClassInfo.put(superclassName, superclassInfo);

                prepareClass(domainInfo, mappableClasses, typeSystem, superClazz);
            }
        }

        if (classInfo.isEnum()) {
            LOGGER.debug("Registering enum class: {}", classInfo.name());
            domainInfo.enumTypes.add(classInfo.getUnderlyingClass());
        }
    }

    static Class<?> loadClass(String className) {

        Class<?> loadedButNotInitalizedClass = null;
        try {
            loadedButNotInitalizedClass = Class
                .forName(className, false, Thread.currentThread().getContextClassLoader());
        } catch (ClassNotFoundException e) {
            LOGGER.warn("Could not load class {}", className);

        }
        return loadedButNotInitalizedClass;
    }

    private void buildAnnotationNameToClassInfoMap() {

        LOGGER.info("Building annotation class map");
        for (ClassInfo classInfo : classNameToClassInfo.values()) {
            for (AnnotationInfo annotation : classInfo.annotations()) {
                ArrayList<ClassInfo> classInfoList = annotationNameToClassInfo.get(annotation.getName());
                if (classInfoList == null) {
                    classInfoList = new ArrayList<>();
                    annotationNameToClassInfo.put(annotation.getName(), classInfoList);
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
                    classInfoList = new ArrayList<>();
                    interfaceNameToClassInfo.put(iface.name(), classInfoList);
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

            if (classInfo.name() == null || classInfo.name().equals("java.lang.Object")) {
                continue;
            }

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

        // TODO 🔥 the "lazy" initialization of the fields seems to be all in vain anyway.
        for (ClassInfo classInfo : classNameToClassInfo.values()) {
            classInfo.primaryIndexField();
            classInfo.getVersionField();
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
                        fieldClass = DescriptorMappings.getType(fieldInfo.getTypeDescriptor());
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

            // Check if there's a registered set of attribute converters for the given field info and if so,
            // select the correct one based on the features of the field
            Function<AttributeConverters, Optional<AttributeConverter<?, ?>>> selectAttributeConverter = ac -> DomainInfo
                .selectAttributeConverterFor(fieldInfo, ac);

            Optional<AttributeConverter<?, ?>> registeredAttributeConverter =
                ConvertibleTypes.REGISTRY.entrySet().stream()
                    .filter(e -> typeDescriptor.contains(e.getKey()))
                    // There are some signatures that are substrings of others, so
                    // we have to sort by descending length to match the longest
                    .sorted(comparingInt((Map.Entry<String, ?> e) -> e.getKey().length()).reversed())
                    .findFirst()
                    .map(Map.Entry::getValue)
                    .flatMap(selectAttributeConverter);

            boolean isSupportedNativeType = typeSystem.supportsAsNativeType(DescriptorMappings.getType(fieldInfo.getTypeDescriptor()));
            // We can use a registered converter
            if (registeredAttributeConverter.isPresent() && !isSupportedNativeType) {
                fieldInfo.setPropertyConverter(registeredAttributeConverter.get());
            } else {
                // Check if the user configured one through the convert annotation
                if (fieldInfo.getAnnotations().get(Convert.class) != null) {
                    // no converter's been set but this method is annotated with @Convert so we need to proxy it
                    Class<?> entityAttributeType = DescriptorMappings.getType(typeDescriptor);
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
                        new ProxyAttributeConverter(entityAttributeType, DescriptorMappings.getType(graphTypeDescriptor),
                            this.conversionCallbackRegistry));
                }

                Class fieldType = DescriptorMappings.getType(typeDescriptor);

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

    // leaky for spring
    public Map<String, ClassInfo> getClassInfoMap() {
        return classNameToClassInfo;
    }

    public List<ClassInfo> getClassInfos(String interfaceName) {
        return interfaceNameToClassInfo.get(interfaceName);
    }

    /**
     * Selects the specialized attribute converter for the given field info, depending wether the field info
     * describes an array, iterable or scalar value.
     *
     * @param source must not be {@literal null}.
     * @param from   The attribute converters to select from, must not be {@literal null}.
     * @return
     */
    private static Optional<AttributeConverter<?, ?>> selectAttributeConverterFor(FieldInfo source,
        AttributeConverters from) {

        FieldInfo fieldInfo = Objects.requireNonNull(source, "Need a field info");
        AttributeConverters attributeConverters = Objects
            .requireNonNull(from, "Need the set of attribute converters for the given field info.");

        AttributeConverter selectedConverter;
        if (fieldInfo.isArray()) {
            selectedConverter = attributeConverters.forArray;
        } else if (fieldInfo.isIterable()) {
            selectedConverter = attributeConverters.forIterable.apply(fieldInfo.getCollectionClassname());
        } else {
            selectedConverter = attributeConverters.forScalar;
        }

        return Optional.ofNullable(selectedConverter);
    }

    private static void setEnumFieldConverter(FieldInfo fieldInfo, Class enumClass) {
        if (fieldInfo.isArray()) {
            fieldInfo.setPropertyConverter(ConvertibleTypes.getEnumArrayConverter(enumClass));
        } else if (fieldInfo.isIterable()) {
            fieldInfo.setPropertyConverter(
                ConvertibleTypes.getEnumCollectionConverter(enumClass, fieldInfo.getCollectionClassname()));
        } else {
            fieldInfo.setPropertyConverter(ConvertibleTypes.getEnumConverter(enumClass));
        }
    }
}

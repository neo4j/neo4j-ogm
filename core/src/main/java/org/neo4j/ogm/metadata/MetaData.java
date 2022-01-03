/*
 * Copyright (c) 2002-2022 "Neo4j,"
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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.driver.TypeSystem;
import org.neo4j.ogm.driver.TypeSystem.NoNativeTypes;
import org.neo4j.ogm.exception.core.AmbiguousBaseClassException;
import org.neo4j.ogm.metadata.schema.DomainInfoSchemaBuilder;
import org.neo4j.ogm.metadata.schema.Schema;
import org.neo4j.ogm.typeconversion.ConversionCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 * @author Michael J. Simons
 */
public class MetaData {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetaData.class);

    private final DomainInfo domainInfo;
    private final Schema schema;

    public MetaData(String... packages) {
        this(NoNativeTypes.INSTANCE, packages);
    }

    public MetaData(TypeSystem typeSystem, String... packages) {

        if (containsRootPackage(packages)) {
            LOGGER.warn(""
                + "You are trying to scan the root package. "
                + "This will take a long time and probably end with an error or "
                + "classes being mapped that are not part of your domain. "
                + "Please have a look at the configuration of your SessionFactory.");
        }

        this.domainInfo = DomainInfo.create(typeSystem, packages);
        this.schema = new DomainInfoSchemaBuilder(domainInfo).build();
    }

    static boolean containsRootPackage(String... packages) {
        return packages == null || packages.length == 0 || Arrays.stream(packages).anyMatch(String::isEmpty);
    }

    public Schema getSchema() {
        return schema;
    }

    /**
     * Finds the ClassInfo for the supplied partial class name or label.
     * The supplied ClassInfo, if found can represent either a Class or an Interface
     *
     * @param name the simple class name or label for a class we want to find
     * @return A ClassInfo matching the supplied name, or null if it doesn't exist
     */
    public ClassInfo classInfo(String name) {

        ClassInfo classInfo = _classInfo(name, NodeEntity.class);
        if (classInfo != null) {
            return classInfo;
        }

        classInfo = _classInfo(name, RelationshipEntity.class);
        if (classInfo != null) {
            return classInfo;
        }

        classInfo = domainInfo.getClassSimpleName(name);
        if (classInfo != null) {
            return classInfo;
        }

        // not found
        return null;
    }

    /**
     * Finds the ClassInfo for the supplied Class throgh the class' name.
     *
     * @param clazz the class whose classInfo we want to find
     * @return A ClassInfo matching the supplied object's class, or null if it doesn't exist
     */
    public ClassInfo classInfo(Class<?> clazz) {
        return classInfo(clazz.getName());
    }

    /**
     * Finds the ClassInfo for the supplied object by looking up its class name
     *
     * @param object the object whose classInfo we want to find
     * @return A ClassInfo matching the supplied object's class, or null if it doesn't exist
     */
    public ClassInfo classInfo(Object object) {
        return classInfo(object.getClass().getName());
    }

    private ClassInfo _classInfo(String name, Class<?> nodeEntityAnnotation) {

        Map<String, List<ClassInfo>> labelledClasses;
        if (nodeEntityAnnotation == NodeEntity.class) {
            labelledClasses = domainInfo.getNodeEntitiesByLabel();
        } else if (nodeEntityAnnotation == RelationshipEntity.class) {
            labelledClasses = domainInfo.getRelationshipEntitiesByType();
        } else {
            throw new IllegalArgumentException(
                "Cannot retrieve class infos for annotation " + nodeEntityAnnotation.toString());
        }

        // Make it explicit that duplicate labels are not dealt with.
        // Usually, the class infos list contains the classes for a specific label in the reverse order of
        // processing. That is: The last class found by class graph is the first one in the list.
        // So returning the first one reassembles the previous behaviour (before working on GH-678).
        List<ClassInfo> classInfos = labelledClasses.getOrDefault(name, Collections.emptyList());
        return classInfos.isEmpty() ? null : classInfos.get(0);
    }

    /**
     * Given an set of names (simple or fully-qualified) that are possibly within a type hierarchy, this function returns the
     * base class from among them.
     *
     * @param taxa the taxa (simple class names or labels)
     * @return The ClassInfo representing the base class among the taxa or <code>null</code> if it cannot be found
     */
    public ClassInfo resolve(String... taxa) {

        if (taxa.length > 0) {

            Set<ClassInfo> resolved = new HashSet<>();

            for (String taxon : taxa) {
                LOGGER.debug("looking for concrete class to resolve label: {}", taxon);
                ClassInfo taxonClassInfo = classInfo(taxon);

                // ignore any foreign labels
                if (taxonClassInfo == null) {
                    LOGGER.debug("This label is not known in the mapping context. Moving on...");
                    continue;
                }

                // if classInfo is an interface or abstract there must be a single concrete implementing class/subclass
                // if there is, use that, otherwise this label cannot be resolved
                if (taxonClassInfo.isInterface()) {
                    LOGGER.debug("label is on an interface. Looking for a single implementing class...");
                    taxonClassInfo = findSingleImplementor(taxonClassInfo);
                } else if (taxonClassInfo.isAbstract()) {
                    LOGGER.debug("label is on an abstract class. Looking for a single concrete subclass...");
                    taxonClassInfo = findFirstSingleConcreteClass(taxonClassInfo, taxonClassInfo.directSubclasses());
                }

                // given we have a qualifying concrete class, check if its a subclass or superclass of one found previously
                // if its a superclass, we discard it.
                // if its a subclass, we replace the previously found class with this one.
                if (taxonClassInfo != null) {
                    LOGGER.debug("concrete class found: {}. comparing with what's already been found previously...",
                        taxonClassInfo);
                    for (ClassInfo found : resolved) {
                        if (taxonClassInfo.isSubclassOf(found)) {
                            LOGGER.debug("{} is a subclass of {} and will replace it.", taxonClassInfo, found);
                            resolved.remove(found);
                            break; // there will only be one
                        }
                        if (found.isSubclassOf(taxonClassInfo)) {
                            LOGGER.debug("{} is a superclass of {} and will be ignored", taxonClassInfo, found);
                            taxonClassInfo = null;  // discard it
                            break; // no need to look further, already discarded
                        }
                    }
                } else {
                    LOGGER.debug("no implementing class or concrete subclass found!");
                }

                // finally, we can add the taxonClassInfo - if it is still valid
                if (taxonClassInfo != null) {
                    LOGGER.debug("{} resolving class: {}", taxon, taxonClassInfo);
                    resolved.add(taxonClassInfo);
                }
            }
            if (resolved.size() > 1) {
                // Sort so we always get the same order
                String[] sorted = Arrays.copyOf(taxa, taxa.length);
                Arrays.sort(sorted);
                throw new AmbiguousBaseClassException(Arrays.toString(sorted));
            }
            if (resolved.iterator().hasNext()) {
                return resolved.iterator().next();
            }
        }
        LOGGER.debug("No resolving class found!!");
        return null;
    }

    /**
     * Finds ClassInfos for the supplied partial class name or label.
     *
     * @param name the simple class name or label for a class we want to find
     * @return A Set of ClassInfo matching the supplied name, or empty if it doesn't exist
     */
    public Set<ClassInfo> classInfoByLabelOrType(String name) {

        Set<ClassInfo> matchingClassInfos = new HashSet<>();

        ClassInfo classInfo = _classInfo(name, NodeEntity.class);
        if (classInfo != null) {
            matchingClassInfos.add(classInfo);
        }

        // Potentially many relationship entities annotated with the same type
        matchingClassInfos.addAll(domainInfo.getRelationshipEntitiesByType().getOrDefault(name, Collections.emptyList()));

        classInfo = domainInfo.getClassSimpleName(name);
        if (classInfo != null) {
            matchingClassInfos.add(classInfo);
        }

        return matchingClassInfos;
    }

    private ClassInfo findFirstSingleConcreteClass(ClassInfo root, List<ClassInfo> classInfoList) {

        // if the root class is concrete, return it.
        if (!root.isInterface() && !root.isAbstract()) {
            return root;
        }

        // if there are no subclasses, we can't look any further. No concrete class exists
        if (classInfoList.isEmpty()) {
            return null;
        }

        // if there are more than one direct subclasses, we won't know which to use
        if (classInfoList.size() > 1) {
            LOGGER.debug("More than one class subclasses {}", root);
            return null;
        }

        // nothing found yet, but exactly one subclass exists. If its an interface,
        // replace with its single implementing class - iff exactly one implementing class exists
        ClassInfo classInfo = classInfoList.iterator().next();
        if (classInfo.isInterface()) {
            classInfo = findSingleImplementor(classInfo);
        }

        // if we have a potential concrete class, keep going!
        return (classInfo == null ? null : findFirstSingleConcreteClass(classInfo, classInfo.directSubclasses()));
    }

    public boolean isRelationshipEntity(String className) {
        ClassInfo classInfo = classInfo(className);
        return classInfo != null && null != classInfo.annotationsInfo().get(RelationshipEntity.class);
    }

    private ClassInfo findSingleImplementor(ClassInfo interfaceInfo) {
        if (interfaceInfo != null && interfaceInfo.directImplementingClasses() != null
            && interfaceInfo.directImplementingClasses().size() == 1) {
            return interfaceInfo.directImplementingClasses().get(0);
        }
        return null;
    }

    public Collection<ClassInfo> persistentEntities() {
        return domainInfo.getClassInfoMap().values();
    }

    public String entityType(String name) {
        ClassInfo classInfo = classInfo(name);
        if (classInfo == null) {
            return null;
        }
        if (classInfo.isRelationshipEntity()) {
            AnnotationInfo annotation = classInfo.annotationsInfo().get(RelationshipEntity.class);
            return annotation.get(RelationshipEntity.TYPE, classInfo.name());
        }
        return classInfo.neo4jName();
    }

    public List<ClassInfo> getImplementingClassInfos(String interfaceName) {
        return domainInfo.getClassInfos(interfaceName);
    }

    public void registerConversionCallback(ConversionCallback conversionCallback) {
        this.domainInfo.registerConversionCallback(conversionCallback);
    }
}

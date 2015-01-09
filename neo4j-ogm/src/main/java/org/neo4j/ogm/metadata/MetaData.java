package org.neo4j.ogm.metadata;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.metadata.info.AnnotationInfo;
import org.neo4j.ogm.metadata.info.ClassInfo;
import org.neo4j.ogm.metadata.info.DomainInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MetaData {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetaData.class);

    private final DomainInfo domainInfo;

    public MetaData(String... packages) {
        domainInfo = new DomainInfo(packages);
    }

    /**
     * Finds the ClassInfo for the supplied partial class name or label
     *
     * @param name the simple class name or label for a class we want to find
     * @return A ClassInfo matching the supplied name, or null if it doesn't exist
     */
    public ClassInfo classInfo(String name) {
        ClassInfo classInfo = _classInfo(name, NodeEntity.class.getName(), "label");
        if (classInfo != null) {
            return classInfo;
        }
        classInfo = _classInfo(name, RelationshipEntity.class.getName(), "type");
        if (classInfo != null) {
            return classInfo;
        }
        return domainInfo.getClassSimpleName(name);
    }

    private ClassInfo _classInfo(String name, String nodeEntityAnnotation, String annotationPropertyName) {
        List<ClassInfo> labelledClasses = domainInfo.getClassInfosWithAnnotation(nodeEntityAnnotation);
        if (labelledClasses != null) {
            for (ClassInfo labelledClass : labelledClasses) {
                AnnotationInfo annotationInfo = labelledClass.annotationsInfo().get(nodeEntityAnnotation);
                String value = annotationInfo.get(annotationPropertyName, labelledClass.name());
                if (value.equals(name)) {
                    return labelledClass;
                }
            }
        }
        return null;
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
            Set<ClassInfo> baseClasses = new HashSet<>();
            for (String taxon : taxa) {
                ClassInfo taxonClassInfo = classInfo(taxon);
                if (taxonClassInfo != null) {
                    ClassInfo baseClassInfo = resolveBaseClass(taxonClassInfo, taxonClassInfo.directSubclasses());
                    if (baseClassInfo != null) {
                        baseClasses.add(baseClassInfo);
                    }
                }
            }
            if (baseClasses.size() > 1) {
                LOGGER.info("Multiple leaf classes found in type hierarchy for specified taxa: " + Arrays.toString(taxa) + ". leaf classes are: " + baseClasses);
                return null;
            }
            if (baseClasses.iterator().hasNext()) {
                return baseClasses.iterator().next();
            }
        }
        return null;
    }

    private ClassInfo resolveBaseClass(ClassInfo fqn, List<ClassInfo> classInfoList) {
        if (classInfoList.isEmpty()) {
            return fqn;
        }
        if (classInfoList.size() > 1) {
            LOGGER.info("More than one class subclasses " + fqn);
            return null;
        }
        ClassInfo classInfo = classInfoList.iterator().next();
        return resolveBaseClass(classInfo, classInfo.directSubclasses());

    }


}

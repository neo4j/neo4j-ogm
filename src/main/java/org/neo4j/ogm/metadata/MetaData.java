package org.neo4j.ogm.metadata;

import org.neo4j.ogm.annotation.Label;
import org.neo4j.ogm.annotation.NodeId;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.metadata.dictionary.ClassDictionary;
import org.neo4j.ogm.metadata.dictionary.FieldDictionary;
import org.neo4j.ogm.metadata.dictionary.MethodDictionary;
import org.neo4j.ogm.metadata.info.AnnotationInfo;
import org.neo4j.ogm.metadata.info.ClassInfo;
import org.neo4j.ogm.metadata.info.DomainInfo;
import org.neo4j.ogm.metadata.info.FieldInfo;
import org.neo4j.ogm.strategy.annotated.AnnotatedClassDictionary;
import org.neo4j.ogm.strategy.annotated.AnnotatedFieldDictionary;
import org.neo4j.ogm.strategy.annotated.AnnotatedMethodDictionary;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MetaData {

    private final DomainInfo domainInfo;

    // todo: injected:
    private ClassDictionary classDictionary;
    private MethodDictionary methodDictionary;
    private FieldDictionary fieldDictionary;

    public MetaData(String... packages) {

        domainInfo = new DomainInfo(packages);

        classDictionary = new AnnotatedClassDictionary(domainInfo);
        methodDictionary = new AnnotatedMethodDictionary(domainInfo);
        fieldDictionary = new AnnotatedFieldDictionary(domainInfo);
    }

    public ClassInfo classInfo(String name) {
        String annotation = Label.class.getName();
        List<ClassInfo> labelledClasses = domainInfo.getClassInfosWithAnnotation(annotation);
        for (ClassInfo labelledClass : labelledClasses) {
            AnnotationInfo annotationInfo = labelledClass.annotationsInfo().get(annotation);
            String value = annotationInfo.get("name", labelledClass.name());
            if (value.equals(name)) {
                return labelledClass;
            }
        }
        return domainInfo.getClass(classDictionary.determineLeafClass(name));
    }

    // always calculates. could cache later
    // the identity field is a field annotated with @NodeId, or if none exists, a field
    // of type Long called 'id'
    public FieldInfo identityField(ClassInfo classInfo) {
        for (FieldInfo fieldInfo : classInfo.fieldsInfo().fields()) {
            AnnotationInfo annotationInfo = fieldInfo.getAnnotations().get(NodeId.class.getName());
            if (annotationInfo != null) {
                return fieldInfo;
            }
        }
        FieldInfo fieldInfo = classInfo.fieldsInfo().get("id");
        if (fieldInfo != null) {
            if (fieldInfo.getDescriptor().equals("Ljava/lang/Long;")) {
                return fieldInfo;
            }
        }
        return null;
    }

    // always calculates. could cache later
    // a property field is any field annotated with @Property, or any field that can be mapped to a
    // node property. The identity field is not a property field.
    public Collection<FieldInfo> propertyFields(ClassInfo classInfo) {
        FieldInfo identityField = identityField(classInfo);
        Set<FieldInfo> fieldInfos = new HashSet<>();
        for (FieldInfo fieldInfo : classInfo.fieldsInfo().fields()) {
            if (!fieldInfo.getName().equals(identityField.getName())) {
                // todo: when building fieldInfos, we must exclude fields annotated @Transient, or with the transient modifier
                if (fieldInfo.getAnnotations().isEmpty()) {
                    if (fieldInfo.isSimple()) {
                        fieldInfos.add(fieldInfo);
                    }
                }
                else {
                    AnnotationInfo annotationInfo = fieldInfo.getAnnotations().get(Property.class.getName());
                    if (annotationInfo != null) {
                        fieldInfos.add(fieldInfo);
                    }
                }
            }
        }
        return fieldInfos;
    }

    // always calculates. could cache later
    // a relationship field is any field annotated with @Relationship, or any field that cannot be mapped to a
    // node property. The identity field is not a relationhip field.
    public Collection<FieldInfo> relationshipFields(ClassInfo classInfo) {
        FieldInfo identityField = identityField(classInfo);
        Set<FieldInfo> fieldInfos = new HashSet<>();
        for (FieldInfo fieldInfo : classInfo.fieldsInfo().fields()) {
            if (fieldInfo != identityField) {
                // todo: when building fieldInfos, we must exclude fields annotated @Transient, or with the transient modifier
                if (fieldInfo.getAnnotations().isEmpty()) {
                    if (!fieldInfo.isSimple()) {
                        fieldInfos.add(fieldInfo);
                    }
                }
                else {
                    AnnotationInfo annotationInfo = fieldInfo.getAnnotations().get(Relationship.class.getName());
                    if (annotationInfo != null) {
                        fieldInfos.add(fieldInfo);
                    }
                }
            }
        }
        return fieldInfos;
    }

    public FieldInfo relationshipField(ClassInfo classInfo, String relationshipName) {
        for (FieldInfo fieldInfo : relationshipFields(classInfo)) {
            if (fieldInfo.property().equals(relationshipName)) {
                return fieldInfo;
            }
        }
        return null;
    }

    public FieldInfo propertyField(ClassInfo classInfo, String propertyName) {
        for (FieldInfo fieldInfo : propertyFields(classInfo)) {
            if (fieldInfo.property().equals(propertyName)) {
                return fieldInfo;
            }
        }
        return null;
    }

}

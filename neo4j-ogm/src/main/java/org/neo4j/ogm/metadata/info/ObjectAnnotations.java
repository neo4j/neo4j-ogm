package org.neo4j.ogm.metadata.info;

import org.neo4j.ogm.annotation.CustomType;
import org.neo4j.ogm.annotation.DateLong;
import org.neo4j.ogm.annotation.DateString;
import org.neo4j.ogm.annotation.EnumString;
import org.neo4j.ogm.typeconversion.AttributeConverter;
import org.neo4j.ogm.typeconversion.DateLongConverter;
import org.neo4j.ogm.typeconversion.DateStringConverter;
import org.neo4j.ogm.typeconversion.EnumStringConverter;

import java.util.HashMap;
import java.util.Map;

public class ObjectAnnotations {

    private String objectName; // fully qualified class, method or field name.
    private final Map<String, AnnotationInfo> annotations = new HashMap<>();

    public String getName() {
        return objectName;
    }

    public void setName(String objectName) {
        this.objectName = objectName;
    }

    public void put(String key, AnnotationInfo value) {
        annotations.put(key, value);
    }

    public AnnotationInfo get(String key) {
        return annotations.get(key);
    }

    public boolean isEmpty() {
        return annotations.isEmpty();
    }

    public AttributeConverter<?, ?> getConverter(String typeDescriptor) {

        // try to get a custom type converter
        AnnotationInfo customType = get(CustomType.CLASS);
        if (customType != null) {
            try {
                String classDescriptor = customType.get(CustomType.CONVERTER, null);
                String className = classDescriptor.replace("/", ".").substring(1, classDescriptor.length()-1);
                Class clazz = Class.forName(className);
                return (AttributeConverter<?, ?>) clazz.newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        // try to find a pre-registered type annotation. this is very clumsy, but at least it is done only once
        AnnotationInfo dateLongConverterInfo = get(DateLong.CLASS);
        if (dateLongConverterInfo != null) {
            return new DateLongConverter();
        }

        AnnotationInfo dateStringConverterInfo = get(DateString.CLASS);
        if (dateStringConverterInfo != null) {
            String format = dateStringConverterInfo.get(DateString.FORMAT, DateString.ISO_8601);
            return new DateStringConverter(format);
        }

        AnnotationInfo enumStringConverterInfo = get(EnumString.CLASS);
        if (enumStringConverterInfo != null) {
            String classDescriptor = enumStringConverterInfo.get(EnumString.ENUM, null);
            String className = classDescriptor.replace("/", ".").substring(1, classDescriptor.length()-1);
            try {
                Class clazz = Class.forName(className);
                return new EnumStringConverter(clazz);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        // no pre-registered types found. select the correct default (if applicable)
        return ConvertibleTypes.getDefaultConverter(typeDescriptor);

    }

}

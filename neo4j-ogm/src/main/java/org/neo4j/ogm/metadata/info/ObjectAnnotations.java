/*
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j-OGM.
 *
 * Neo4j-OGM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.neo4j.ogm.metadata.info;

import org.neo4j.ogm.annotation.typeconversion.*;
import org.neo4j.ogm.typeconversion.*;

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
        AnnotationInfo customType = get(Convert.CLASS);
        if (customType != null) {
            try {
                String classDescriptor = customType.get(Convert.CONVERTER, null);
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
            String classDescriptor = enumStringConverterInfo.get(EnumString.TYPE, null);
            String className = classDescriptor.replace("/", ".").substring(1, classDescriptor.length()-1);
            try {
                Class clazz = Class.forName(className);
                return new EnumStringConverter(clazz);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        AnnotationInfo numberStringConverterInfo = get(NumberString.CLASS);
        if (numberStringConverterInfo != null) {
            String classDescriptor = enumStringConverterInfo.get(NumberString.TYPE, null);
            String className = classDescriptor.replace("/", ".").substring(1, classDescriptor.length()-1);
            try {
                Class clazz = Class.forName(className);
                return new NumberStringConverter(clazz);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return null;
    }

}

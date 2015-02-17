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

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class AnnotationsInfo {

    private final Map<String, AnnotationInfo> classAnnotations = new HashMap<>();

    AnnotationsInfo() {}

    public AnnotationsInfo(DataInputStream dataInputStream, ConstantPool constantPool) throws IOException {
        int attributesCount = dataInputStream.readUnsignedShort();
        for (int i = 0; i < attributesCount; i++) {
            String attributeName = constantPool.lookup(dataInputStream.readUnsignedShort());
            int attributeLength = dataInputStream.readInt();
            if ("RuntimeVisibleAnnotations".equals(attributeName)) {
                int annotationCount = dataInputStream.readUnsignedShort();
                for (int m = 0; m < annotationCount; m++) {
                    AnnotationInfo info = new AnnotationInfo(dataInputStream, constantPool);
                    // todo: maybe register just the annotations we're interested in.
                    classAnnotations.put(info.getName(), info);
                }
            }
            else {
                dataInputStream.skipBytes(attributeLength);
            }
        }
    }

    public Collection<AnnotationInfo> list() {
        return classAnnotations.values();
    }

    /**
     * @param annotationName The fully-qualified class name of the annotation type
     * @return The {@link AnnotationInfo} that matches the given name or <code>null</code> if it's not present
     */
    public AnnotationInfo get(String annotationName) {
        return classAnnotations.get(annotationName);
    }

    void add(AnnotationInfo annotationInfo) {
        classAnnotations.put(annotationInfo.getName(), annotationInfo);
    }

    public void append(AnnotationsInfo annotationsInfo) {
        for (AnnotationInfo annotationInfo : annotationsInfo.list()) {
            add(annotationInfo);
        }
    }
}

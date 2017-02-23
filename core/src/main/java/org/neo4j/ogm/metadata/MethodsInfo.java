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

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.Transient;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 */
public class MethodsInfo {

    private final Map<String, MethodInfo> methods = new HashMap<>();
    private final Map<String, MethodInfo> getters = new HashMap<>();
    private final Map<String, MethodInfo> setters = new HashMap<>();

    MethodsInfo() {}

    public MethodsInfo(String className, DataInputStream dataInputStream, ConstantPool constantPool) throws IOException {
        // get the method information for this class
        int methodCount = dataInputStream.readUnsignedShort();
        for (int i = 0; i < methodCount; i++) {
            dataInputStream.skipBytes(2); // access_flags
            String methodName = constantPool.readString(dataInputStream.readUnsignedShort()); // name_index
            String descriptor = constantPool.readString(dataInputStream.readUnsignedShort()); // descriptor
            ObjectAnnotations objectAnnotations = new ObjectAnnotations();
            int attributesCount = dataInputStream.readUnsignedShort();
            String typeParameterDescriptor = null; // available as an attribute for parameterised collections
            for (int j = 0; j < attributesCount; j++) {
                String attributeName = constantPool.readString(dataInputStream.readUnsignedShort());
                int attributeLength = dataInputStream.readInt();
                if ("RuntimeVisibleAnnotations".equals(attributeName)) {
                    int annotationCount = dataInputStream.readUnsignedShort();
                    for (int m = 0; m < annotationCount; m++) {
                        AnnotationInfo info = new AnnotationInfo(dataInputStream, constantPool);
                        // todo: maybe register just the annotations we're interested in.
                        objectAnnotations.put(info.getName(), info);
                    }
                } else if ("Signature".equals(attributeName)) {
                    String signature = constantPool.readString(dataInputStream.readUnsignedShort());
                    if (signature.contains("<")) {
                        typeParameterDescriptor = signature.substring(signature.indexOf('<') + 1, signature.indexOf('>'));
                    }
                } else {
                    dataInputStream.skipBytes(attributeLength);
                }
            }
            if (!methodName.equals("<init>") && !methodName.equals("<clinit>") && objectAnnotations.get(Transient.class) == null) {
                addMethod(new MethodInfo(className, methodName, descriptor, typeParameterDescriptor, objectAnnotations));
            }
        }
    }

    public Collection<MethodInfo> methods() {
        return methods.values();
    }

    public Collection<MethodInfo> getters() {
        return getters.values();
    }

    public Collection<MethodInfo> setters() {
        return setters.values();
    }

    public MethodInfo get(String methodName) {
        return methods.get(methodName);
    }

    public void append(MethodsInfo methodsInfo) {
        for (MethodInfo methodInfo : methodsInfo.methods()) {
            if (!methods.containsKey(methodInfo.getName())) {
                addMethod(methodInfo);
            }
        }
    }

    void removeGettersAndSetters(MethodInfo methodInfo) {
        getters.remove(methodInfo.getName());
        setters.remove(methodInfo.getName());
    }

    private void addMethod(MethodInfo methodInfo) {
        String methodName = methodInfo.getName();
        methods.put(methodName, methodInfo);
    }
}

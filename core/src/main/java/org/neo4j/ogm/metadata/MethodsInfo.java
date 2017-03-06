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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 */
public class MethodsInfo {

    private final Map<String, MethodInfo> methods;

    public MethodsInfo() {
        this.methods = new HashMap<>();
    }

    public MethodsInfo(Map<String, MethodInfo> methods) {
        this.methods = new HashMap<>(methods);
    }

    public Collection<MethodInfo> methods() {
        return methods.values();
    }

    public MethodInfo get(String methodName) {
        return methods.get(methodName);
    }

    public void append(MethodsInfo methodsInfo) {
        for (MethodInfo methodInfo : methodsInfo.methods()) {
            methods.putIfAbsent(methodInfo.getName(), methodInfo);
        }
    }
}

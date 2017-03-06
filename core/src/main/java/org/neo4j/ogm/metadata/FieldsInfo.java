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

import java.util.*;

/**
 * @author Vince Bickers
 */
public class FieldsInfo {

    private final Map<String, FieldInfo> fields;

    public FieldsInfo() {
        this.fields = new HashMap<>();
    }

    public FieldsInfo(Map<String, FieldInfo> fields) {
        this.fields = new HashMap<>(fields);
    }

    public Collection<FieldInfo> fields() {
        return fields.values();
    }

    public Collection<FieldInfo> compositeFields() {
        List<FieldInfo> fields = new ArrayList<>();
        for (FieldInfo field : fields()) {
            if (field.hasCompositeConverter()) {
                fields.add(field);
            }
        }
        return Collections.unmodifiableList(fields);
    }


    public FieldInfo get(String name) {
        return fields.get(name);
    }

    public void append(FieldsInfo fieldsInfo) {
        for (FieldInfo fieldInfo : fieldsInfo.fields()) {
            if (!fields.containsKey(fieldInfo.getName())) {
                fields.put(fieldInfo.getName(), fieldInfo);
            }
        }
    }
}

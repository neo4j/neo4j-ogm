/*
 * Copyright (c) 2002-2017 "Neo Technology,"
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

package org.neo4j.ogm.context;

import org.neo4j.ogm.metadata.ClassInfo;

import static java.util.Objects.requireNonNull;

/**
 * Pair of label and primary id to use for lookups by primary key in MappingContext and CypherContext
 *
 * The label is needed because primary id is unique for given label. There might be 1 primary id pointing to
 * two different entities having different label.
 *
 * @author Frantisek Hartman
 */
class LabelPrimaryId {

    private final String label;
    private final Object id;

    /**
     * Create LabelPrimaryId
     *
     * @param classInfo class info containign the primary id
     * @param id the value of the id
     */
    public LabelPrimaryId(ClassInfo classInfo, Object id) {
        this.label = classInfo.primaryIndexField().containingClassInfo().neo4jName();
        this.id = requireNonNull(id);
    }

    public String getLabel() {
        return label;
    }

    public Object getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LabelPrimaryId that = (LabelPrimaryId) o;

        if (!label.equals(that.label)) return false;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        int result = label.hashCode();
        result = 31 * result + id.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "LabelPrimaryId{" +
                "label='" + label + '\'' +
                ", id=" + id +
                '}';
    }
}

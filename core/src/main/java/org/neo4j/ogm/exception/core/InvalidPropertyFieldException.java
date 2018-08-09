/*
 * Copyright (c) 2002-2018 "Neo Technology,"
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
package org.neo4j.ogm.exception.core;

import org.neo4j.ogm.metadata.FieldInfo;

/**
 * {@link RuntimeException} that indicates a field that possibly should be persisted as a property
 * but cannot be persisted as such, maybe due to a missing converter.
 *
 * @author Michael J. Simons
 */
public class InvalidPropertyFieldException extends MetadataException {

    private final String domainClassName;
    private final String fieldName;

    public InvalidPropertyFieldException(FieldInfo invalidField) {
        super(String.format("'%s#%s' is not persistable as property but has not been marked as transient.",
            invalidField.containingClassInfo().name(), invalidField.getName()));

        this.domainClassName = invalidField.containingClassInfo().name();
        this.fieldName = invalidField.getName();
    }

    /**
     * @return The fully qualified name of the class containing the field.
     */
    public String getDomainClassName() {
        return domainClassName;
    }

    /**
     * @return The name of the problematic field.
     */
    public String getFieldName() {
        return fieldName;
    }
}

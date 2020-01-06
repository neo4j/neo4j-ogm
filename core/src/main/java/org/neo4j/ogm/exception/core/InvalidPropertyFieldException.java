/*
 * Copyright (c) 2002-2020 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

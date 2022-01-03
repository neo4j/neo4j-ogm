/*
 * Copyright (c) 2002-2022 "Neo4j,"
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
package org.neo4j.ogm.domain.convertible.enums;

import org.neo4j.ogm.annotation.typeconversion.EnumString;

/**
 * @author Vince Bickers
 * @author Gerrit Meier
 */
public class Algebra {

    private Long id;

    private NumberSystem numberSystem;

    @EnumString(value = Operation.class)
    private Operation operation;

    @EnumString(value = Operation.class, lenient = true)
    private Operation operationLenient;

    public NumberSystem getNumberSystem() {
        return numberSystem;
    }

    public void setNumberSystem(NumberSystem numberSystem) {
        this.numberSystem = numberSystem;
    }

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    public Operation getOperationLenient() {
        return operationLenient;
    }

    public void setOperationLenient(Operation operationLenient) {
        this.operationLenient = operationLenient;
    }
}

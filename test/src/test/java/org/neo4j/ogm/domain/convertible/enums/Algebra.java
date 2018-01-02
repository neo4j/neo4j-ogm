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

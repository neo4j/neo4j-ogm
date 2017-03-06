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

package org.neo4j.ogm.metadata.bytecode;

/**
 * @author Vince Bickers
 */
public interface ConstantPoolTags {

    int UTF_8          = 1;
    int INTEGER        = 3;
    int FLOAT          = 4;
    int LONG           = 5;
    int DOUBLE         = 6;
    int CLASS          = 7;
    int STRING         = 8;
    int FIELD_REF      = 9;
    int METHOD_REF     =10;
    int INTERFACE_REF  =11;
    int NAME_AND_TYPE  =12;
    int METHOD_HANDLE  =15;
    int METHOD_TYPE    =16;
    int INVOKE_DYNAMIC =18;

}

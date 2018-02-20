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

package org.neo4j.ogm.typeconversion;

import java.util.Base64;

import org.apache.commons.lang3.ArrayUtils;

/**
 * By default the OGM will map Byte[] wrapped byte[] objects to Base64
 * String values when being stored as a node / relationship property
 * The conversion between the primitive byte[] class and its wrapper
 * Byte[] means that this converter is slightly slower than
 * using the ByteArray64Converter, which works with primitive
 * byte arrays directly.
 *
 * @author Vince Bickers
 */
public class ByteArrayWrapperBase64Converter implements AttributeConverter<Byte[], String> {

    @Override
    public String toGraphProperty(Byte[] value) {
        if (value == null)
            return null;
        return Base64.getEncoder().encodeToString(ArrayUtils.toPrimitive(value));
    }

    @Override
    public Byte[] toEntityAttribute(String value) {
        if (value == null)
            return null;
        byte[] bytes = Base64.getDecoder().decode(value);
        Byte[] wrapper = new Byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            wrapper[i] = bytes[i];  // preferable to new Byte(..) hence not using Apache toObject()
        }
        return wrapper;
    }
}

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
package org.neo4j.ogm.domain.gh492;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;

/**
 *
 * @param <T> Type of the generic value
 * @author Michael J. Simons
 */
public abstract class BaseUser<T> {

    @Id
    @GeneratedValue
    private Long id;

    private String loginName;

    private T genericValue;

    public Long getId() {
        return id;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(final String loginName) {
        this.loginName = loginName;
    }

    public T getGenericValue() {
        return genericValue;
    }

    public void setGenericValue(final T genericValue) {
        this.genericValue = genericValue;
    }

    @NodeEntity
    public static class StringUser extends BaseUser<String[]> {
    }

    @NodeEntity
    public static class ByteUser extends BaseUser<byte[]> {

        private byte[] notGenericValue;

        public byte[] getNotGenericValue() {
            return notGenericValue;
        }

        public void setNotGenericValue(final byte[] notGenericValue) {
            this.notGenericValue = notGenericValue;
        }
    }

    @NodeEntity
    public static class IntUser extends BaseUser<int[]> {
    }

    @NodeEntity
    public static class IntegerUser extends BaseUser<Integer[]> {
    }

    @NodeEntity
    public static class LongUser extends BaseUser<long[]> {
    }
}

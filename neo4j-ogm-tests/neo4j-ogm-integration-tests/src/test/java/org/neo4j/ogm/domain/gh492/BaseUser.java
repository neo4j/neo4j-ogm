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

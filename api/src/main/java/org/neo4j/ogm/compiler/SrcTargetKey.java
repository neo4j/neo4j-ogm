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
package org.neo4j.ogm.compiler;

/**
 * Key for lookup of transient relationship by source and target
 * NOTE: source and target are always sorted so the lookup will ignore the direction
 *
 * @author Frantisek Hartman
 */
public class SrcTargetKey {

    private final long src;
    private final long tgt;

    public SrcTargetKey(long src, long tgt) {
        if (src < tgt) {
            this.src = src;
            this.tgt = tgt;
        } else {
            this.src = tgt;
            this.tgt = src;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SrcTargetKey key = (SrcTargetKey) o;

        if (src != key.src) {
            return false;
        }
        return tgt == key.tgt;
    }

    @Override
    public int hashCode() {
        int result = (int) (src ^ (src >>> 32));
        result = 31 * result + (int) (tgt ^ (tgt >>> 32));
        return result;
    }

}

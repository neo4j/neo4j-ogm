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

package org.neo4j.ogm.metadata;

import java.util.function.Supplier;

/**
 * @author Frantisek Hartman
 */
class LazyInstance<T> {

    private Supplier<T> supplier;
    private T instance;
    private boolean initialized = false;

    public LazyInstance(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    public T get() {
        if (instance == null && !initialized) {
            instance = supplier.get();
            initialized = true;
        }
        return instance;
    }

    public boolean exists() {
        // need to actually execute get() here because this might be a first call to this LazyInstance
        return get() != null;
    }

}

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
package org.neo4j.ogm.domain.gh813;

/**
 * @author Michael J. Simons
 */
public class RowModel {

    private Attribute a;

    private Attribute b;

    private Attribute c;

    public Attribute getA() {
        return a;
    }

    public void setA(Attribute a) {
        this.a = a;
    }

    public Attribute getB() {
        return b;
    }

    public void setB(Attribute b) {
        this.b = b;
    }

    public Attribute getC() {
        return c;
    }

    public void setC(Attribute c) {
        this.c = c;
    }
}

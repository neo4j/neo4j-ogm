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
package org.neo4j.ogm.domain.gh822

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import java.io.IOException
import java.io.Serializable

inline class StringID(val value: String) : Serializable

private class StringIDSerializer : StdSerializer<StringID>(StringID::class.java) {
    @Throws(IOException::class)
    override fun serialize(s: StringID, jsonGenerator: JsonGenerator,
                           serializerProvider: SerializerProvider) {
        jsonGenerator.writeObject(s.value)
    }
}

class IdTypesModule : com.fasterxml.jackson.databind.module.SimpleModule() {

    init {
        addSerializer(StringIDSerializer())
    }
}



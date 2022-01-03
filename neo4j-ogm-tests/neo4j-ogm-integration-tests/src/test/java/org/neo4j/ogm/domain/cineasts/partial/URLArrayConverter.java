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
package org.neo4j.ogm.domain.cineasts.partial;

import java.net.MalformedURLException;
import java.net.URL;

import org.neo4j.ogm.typeconversion.AttributeConverter;

/**
 * @author Luanne Misquitta
 */
public class URLArrayConverter implements AttributeConverter<URL[], String[]> {

    @Override
    public String[] toGraphProperty(URL[] value) {
        if (value == null) {
            return null;
        }

        String[] values = new String[value.length];
        for (int i = 0; i < value.length; i++) {
            values[i] = value[i].toString();
        }
        return values;
    }

    @Override
    public URL[] toEntityAttribute(String[] value) {
        if (value == null) {
            return null;
        }
        URL[] urls = new URL[value.length];
        for (int i = 0; i < value.length; i++) {
            try {
                urls[i] = new URL(value[i]);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        return urls;
    }
}


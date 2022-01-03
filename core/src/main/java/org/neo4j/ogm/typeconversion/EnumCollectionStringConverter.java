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
package org.neo4j.ogm.typeconversion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

/**
 * By default the OGM will map enum collections to and from
 * the string collections containing values returned by enum.name()
 * enum.name() is preferred to enum.ordinal() because it
 * is (slightly) safer: a persisted enum have to be renamed
 * to break its database mapping, whereas if its ordinal
 * was persisted instead, the mapping would be broken
 * simply by changing the declaration order of the enum constants.
 *
 * @author Luanne Misquitta
 * @author Róbert Papp
 */
public class EnumCollectionStringConverter implements AttributeConverter<Collection<Enum>, String[]> {

    private final Class<? extends Enum> enumClass;
    private final Class<? extends Collection> collectionClass;

    public EnumCollectionStringConverter(Class<? extends Enum> enumClass, Class<? extends Collection> collectionClass) {
        this.enumClass = enumClass;
        this.collectionClass = collectionClass;
    }

    @Override
    public String[] toGraphProperty(Collection<Enum> value) {
        if (value == null) {
            return null;
        }
        String[] values = new String[(value.size())];
        int i = 0;
        for (Enum e : value) {
            values[i++] = e.name();
        }
        return values;
    }

    @Override
    public Collection<Enum> toEntityAttribute(String[] stringValues) {
        if (stringValues == null) {
            return null;
        }
        Collection<Enum> values;
        if (List.class.isAssignableFrom(collectionClass)) {
            values = new ArrayList<>(stringValues.length);
        } else if (Vector.class.isAssignableFrom(collectionClass)) {
            values = new Vector<>(stringValues.length);
        } else if (Set.class.isAssignableFrom(collectionClass)) {
            values = new HashSet<>(stringValues.length);
        } else {
            return null;
        }
        for (String value : stringValues) {
            values.add(Enum.valueOf(enumClass, value));
        }
        return values;
    }
}

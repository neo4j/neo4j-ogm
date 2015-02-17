/*
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j-OGM.
 *
 * Neo4j-OGM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.neo4j.ogm.session;

import org.neo4j.ogm.model.Property;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Utils {

    public static final Map<String, Object> map(final Object... keysAndValues) {
        return new HashMap<String, Object>() {
            {
                for (int i = 0; i < keysAndValues.length; i+=2 ) {
                    put(String.valueOf(keysAndValues[i]), keysAndValues[i+1]);
                }
            }
        };
    }

    public static final Map<String, Object> mapCollection(final String collectionName, final Collection<Property<String, Object>> properties) {

        return new HashMap<String, Object>() {
            {
                final Map<String, Object> values = new HashMap<>();
                for (Property<String, Object> property : properties) {
                    String key = property.getKey();
                    Object value = property.asParameter();
                    if (value != null) {
                        values.put(key, value);
                    }
                }
                put(collectionName, values);
            }
        };
    }

    public static int size(Iterable<?> iterable) {
        return (iterable instanceof Collection)
                       ? ((Collection<?>) iterable).size()
                       : size(iterable.iterator());
    }

    public static int size(Iterator<?> iterator) {
        int count = 0;
        while (iterator.hasNext()) {
            iterator.next();
            count++;
        }
        return count;
    }
}

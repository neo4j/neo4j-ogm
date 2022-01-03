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
package org.neo4j.ogm.domain.restaurant;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.typeconversion.CompositeAttributeConverter;

public class LocationConverter implements CompositeAttributeConverter<Location> {

    @Override
    public Map<String, ?> toGraphProperties(Location location) {
        Map<String, Double> properties = new HashMap<>();
        if (location != null) {
            properties.put("latitude", location.getLatitude());
            properties.put("longitude", location.getLongitude());
        }
        return properties;
    }

    @Override
    public Location toEntityAttribute(Map<String, ?> map) {
        Double latitude = (Double) map.get("latitude");
        Double longitude = (Double) map.get("longitude");
        if (latitude != null && longitude != null) {
            return new Location(latitude, longitude);
        }
        return null;
    }
}

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

package org.neo4j.ogm.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NodeModel {

    Long id;
    String[] labels;
    List<Property<String, Object>> properties;

    public List<Property<String, Object>> getPropertyList() {
        return properties;
    }

    public Map<String, Object> getProperties() {
        Map<String, Object> map = new HashMap<>();
        for (Property<String, Object> property : properties) {
            map.put(property.getKey(), property.getValue());
        }
        return map;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = new ArrayList<>();
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            this.properties.add(new Property<String, Object>(entry.getKey(), entry.getValue()));
        }
    }

    public void setPropertyList(List<Property<String, Object>> properties) {
        this.properties = properties;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String[] getLabels() {
        return labels;
    }

    public void setLabels(String[] labels) {
        this.labels = labels;
    }

    public Object property(String key) {
        for (Property property : properties) {
            if (property.getKey().equals(key)) return property.getValue();
        }
        return null;
    }

}
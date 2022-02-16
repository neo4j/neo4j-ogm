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
package org.neo4j.ogm.domain.properties;

import static org.neo4j.ogm.annotation.Properties.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Properties;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.typeconversion.Convert;
import org.neo4j.ogm.typeconversion.CompositeAttributeConverter;

/**
 * @author Frantisek Hartman
 * @author Michael J. Simons
 */
@NodeEntity
public class User {

    public enum EnumA {VALUE_AA}

    public enum EnumB {
        VALUE_BA;

        @Override
        public String toString() {
            return super.name() + " deliberately screw the enum combo toString/name.";
        }
    }

    private Long id;

    private String name;

    @Properties
    private Map<String, Object> myProperties;

    @Properties(prefix = "myPrefix")
    private Map<String, Object> prefixedProperties;

    @Properties(delimiter = "__")
    private Map<String, Object> delimiterProperties;

    @Properties(allowCast = true)
    private Map<String, Integer> integerProperties;

    @Properties(allowCast = true)
    private Map<String, Object> allowCastProperties;

    @Properties
    private Map<EnumA, Object> enumAProperties;

    @Properties
    private Map<EnumB, Object> enumBProperties;

    @Properties
    private Map<String, EnumA> enumAValuesByString;

    @Properties
    private Map<EnumA, EnumB> enumBValuesByEnum;

    @Properties(transformEnumKeysWith = LowerCasePropertiesFilter.class)
    private Map<EnumA, Object> filteredProperties;

    @Convert(NoPrefixPropertiesConverter.class)
    private Map<String, Object> manualProperties;

    @Relationship(type = "VISITED")
    private Set<Visit> visits;

    public User() {
    }

    public User(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, Object> getMyProperties() {
        return myProperties;
    }

    public void setMyProperties(Map<String, Object> myProperties) {
        this.myProperties = myProperties;
    }

    public void putMyProperty(String key, Object value) {
        if (myProperties == null) {
            myProperties = new HashMap<>();
        }
        myProperties.put(key, value);
    }

    public Map<String, Object> getPrefixedProperties() {
        return prefixedProperties;
    }

    public void setPrefixedProperties(Map<String, Object> prefixedProperties) {
        this.prefixedProperties = prefixedProperties;
    }

    public void putPrefixedProperty(String key, Object value) {
        if (prefixedProperties == null) {
            prefixedProperties = new HashMap<>();
        }
        prefixedProperties.put(key, value);
    }

    public Map<String, Object> getDelimiterProperties() {
        return delimiterProperties;
    }

    public void setDelimiterProperties(Map<String, Object> delimiterProperties) {
        this.delimiterProperties = delimiterProperties;
    }

    public void putDelimiterProperty(String key, Object value) {
        if (delimiterProperties == null) {
            delimiterProperties = new HashMap<>();
        }
        delimiterProperties.put(key, value);
    }

    public Map<String, Integer> getIntegerProperties() {
        return integerProperties;
    }

    public void setIntegerProperties(Map<String, Integer> integerProperties) {
        this.integerProperties = integerProperties;
    }

    public void putIntegerProperty(String key, Integer value) {
        if (integerProperties == null) {
            integerProperties = new HashMap<>();
        }
        integerProperties.put(key, value);
    }

    public Map<String, Object> getAllowCastProperties() {
        return allowCastProperties;
    }

    public void setAllowCastProperties(Map<String, Object> allowCastProperties) {
        this.allowCastProperties = allowCastProperties;
    }

    public void putAllowCastProperty(String key, Object value) {
        if (allowCastProperties == null) {
            allowCastProperties = new HashMap<>();
        }
        allowCastProperties.put(key, value);
    }

    public Set<Visit> getVisits() {
        return visits;
    }

    public void setVisits(Set<Visit> visits) {
        this.visits = visits;
    }

    public void addVisit(Visit visit) {
        if (visits == null) {
            visits = new HashSet<>();
        }
        visits.add(visit);
    }

    public Map<EnumA, Object> getEnumAProperties() {
        return enumAProperties;
    }

    public void setEnumAProperties(Map<EnumA, Object> enumAProperties) {
        this.enumAProperties = enumAProperties;
    }

    public Map<EnumB, Object> getEnumBProperties() {
        return enumBProperties;
    }

    public void setEnumBProperties(Map<EnumB, Object> enumBProperties) {
        this.enumBProperties = enumBProperties;
    }

    public Map<EnumA, Object> getFilteredProperties() {
        return filteredProperties;
    }

    public void setFilteredProperties(Map<EnumA, Object> filteredProperties) {
        this.filteredProperties = filteredProperties;
    }

    public Map<String, Object> getManualProperties() {
        return manualProperties;
    }

    public void setManualProperties(Map<String, Object> manualProperties) {
        this.manualProperties = manualProperties;
    }

    public Map<String, EnumA> getEnumAValuesByString() {
        return enumAValuesByString;
    }

    public void setEnumAValuesByString(
        Map<String, EnumA> enumAValuesByString) {
        this.enumAValuesByString = enumAValuesByString;
    }

    public Map<EnumA, EnumB> getEnumBValuesByEnum() {
        return enumBValuesByEnum;
    }

    public void setEnumBValuesByEnum(
        Map<EnumA, EnumB> enumBValuesByEnum) {
        this.enumBValuesByEnum = enumBValuesByEnum;
    }

    public static class LowerCasePropertiesFilter implements BiFunction<Phase, String, String> {

        @Override
        public String apply(Phase phase, String s) {
            if (s == null) {
                return null;
            }

            switch (phase) {
                case TO_GRAPH:
                    return s.toLowerCase(Locale.ENGLISH);
                case TO_ENTITY:
                    return s.toUpperCase(Locale.ENGLISH);
                default:
                    throw new IllegalArgumentException();
            }
        }
    }

    public static class NoPrefixPropertiesConverter implements CompositeAttributeConverter<Map<String, Object>> {

        private static final Set<String> SUPPORTED_PROPERTIES = Collections
            .unmodifiableSet(new HashSet<>(Arrays.asList("a", "b")));

        @Override
        public Map<String, ?> toGraphProperties(Map<String, Object> value) {

            if (value == null) {
                return Collections.emptyMap();
            }
            return value.entrySet()
                .stream()
                .filter(e -> SUPPORTED_PROPERTIES.contains(e.getKey()))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
        }

        @Override
        public Map<String, Object> toEntityAttribute(Map<String, ?> value) {

            if (value == null) {
                return Collections.emptyMap();
            }
            return value.entrySet()
                .stream()
                .filter(e -> SUPPORTED_PROPERTIES.contains(e.getKey()))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
        }
    }
}

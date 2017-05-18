/*
 * Copyright (c) 2002-2017 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 *  conditions of the subcomponent's license, as noted in the LICENSE file.
 */

package org.neo4j.ogm.domain.properties;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Properties;
import org.neo4j.ogm.annotation.Relationship;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Frantisek Hartman
 */
@NodeEntity
public class User {

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

}

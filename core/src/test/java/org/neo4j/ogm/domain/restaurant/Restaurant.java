/*
 * Copyright (c) 2002-2016 "Neo Technology,"
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


package org.neo4j.ogm.domain.restaurant;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.Labels;
import org.neo4j.ogm.annotation.typeconversion.Convert;
import org.neo4j.ogm.annotation.typeconversion.DateString;

public class Restaurant {

    @GraphId
    private Long id;
    private String name;
    private int zip;
    private double score;
    private String description;
    private boolean halal;

    @Convert(LocationConverter.class)
    Location location;

    @DateString
    private Date launchDate;

    @Labels
    public Collection<String> labels = new ArrayList<>();

    public Restaurant() {
    }

    public Restaurant(String name, Location location, int zip) {
        this.name = name;
        this.location = location;
        this.zip = zip;
    }

    public Restaurant(String name, double score) {
        this.name = name;
        this.score = score;
    }

    public Restaurant(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getZip() {
        return zip;
    }

    public void setZip(int zip) {
        this.zip = zip;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public double getScore() { return score; }

    public void setScore(double score) { this.score = score; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    public Date getLaunchDate() { return launchDate; }

    public void setLaunchDate(Date launchDate) { this.launchDate = launchDate; }

    public boolean isHalal() { return halal; }

    public void setHalal(boolean halal) { this.halal = halal; }

}

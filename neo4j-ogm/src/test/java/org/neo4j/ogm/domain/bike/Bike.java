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

package org.neo4j.ogm.domain.bike;

import java.util.List;

public class Bike {

    private String[] colours;
    private Long id;
    private List<Wheel> wheels;
    private Frame frame;
    private Saddle saddle;
    private String brand;

    public String getBrand()
    {
        return brand;
    }

    public void setBrand(String brand)
    {
        this.brand = brand;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String[] getColours() {
        return colours;
    }

    public List<Wheel> getWheels() {
        return wheels;
    }

    public void setWheels(List<Wheel> wheels) {
        this.wheels = wheels;
    }

    public void setSaddle(Saddle saddle)
    {
        this.saddle = saddle;
    }

    public Frame getFrame() {
        return frame;
    }

    public Saddle getSaddle() {
        return saddle;
    }

    public void setColours(String[] colours) {
        this.colours = colours;
    }

    public void setFrame(Frame frame) {
        this.frame = frame;
    }
}

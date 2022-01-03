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
package org.neo4j.ogm.domain.music;

import java.util.Date;

import org.mockito.configuration.IMockitoConfiguration;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.typeconversion.DateLong;

/**
 * @author Luanne Misquitta
 * @author Michael J. Simons
 */
@NodeEntity(label = "l'album")
public class Album {

    private Long id;
    private String name;

    @Relationship(type = "HAS-ALBUM", direction = Relationship.Direction.INCOMING)
    private Artist artist; //none of those compilations allowed

    @Relationship(type = "RECORDED-AT")
    private Recording recording;

    @Relationship(type = "GUEST_ALBUM", direction = Relationship.Direction.INCOMING)
    private Artist guestArtist; //we only tolerate one guest artist

    private Date recordedAt;

    @Property("releasedAt")
    private Date released;

    @DateLong
    private Date enteredChartAt;

    @Property("leftChartAt")
    @DateLong
    private Date leftChart;

    public Album() {
    }

    public Album(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Artist getArtist() {
        return artist;
    }

    public void setArtist(Artist artist) {
        this.artist = artist;
    }

    public Recording getRecording() {
        return recording;
    }

    public void setRecording(Recording recording) {
        this.recording = recording;
    }

    public Artist getGuestArtist() {
        return guestArtist;
    }

    public void setGuestArtist(Artist guestArtist) {
        this.guestArtist = guestArtist;
    }

    public Long getId() {
        return id;
    }

    public Date getReleased() {
        return released;
    }

    public void setReleased(Date released) {
        this.released = released;
    }

    public Date getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(Date recordedAt) {
        this.recordedAt = recordedAt;
    }
}

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

package org.neo4j.ogm.domain.music;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

/**
 * @author Luanne Misquitta
 */
@NodeEntity(label = "l'album")
public class Album {

    private Long id;
    private String name;

    @Relationship(type = "HAS-ALBUM", direction = "INCOMING")
    private Artist artist; //none of those compilations allowed

    @Relationship(type = "RECORDED-AT")
    private Recording recording;

    @Relationship(type = "GUEST_ALBUM", direction = "INCOMING")
    private Artist guestArtist; //we only tolerate one guest artist

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
}

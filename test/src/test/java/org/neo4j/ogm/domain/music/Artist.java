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

package org.neo4j.ogm.domain.music;

import java.util.HashSet;
import java.util.Set;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

/**
 * @author Luanne Misquitta
 */
@NodeEntity(label = "l'artiste")
public class Artist {

    private Long id;
    private String name;

    @Relationship(type = "HAS-ALBUM")
    private Set<Album> albums = new HashSet<>();

    @Relationship(type = "GUEST_ALBUM")
    private Set<Album> guestAlbums = new HashSet<>();

    public Artist() {
    }

    public Artist(String name) {
        this.name = name;
    }

    public Set<Album> getAlbums() {
        return albums;
    }

    public void setAlbums(Set<Album> albums) {
        this.albums = albums;
        for (Album album : albums) {
            album.setArtist(this);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Album> getGuestAlbums() {
        return guestAlbums;
    }

    public void setGuestAlbums(Set<Album> guestAlbums) {
        this.guestAlbums = guestAlbums;
        for (Album album : guestAlbums) {
            album.setGuestArtist(this);
        }
    }

    public void addAlbum(Album album) {
        albums.add(album);
    }

    public Long getId() {
        return id;
    }

    @Override
    public String toString() {
        return "Artist{" +
            "id=" + id +
            ", name='" + name + '\'' +
            '}';
    }
}

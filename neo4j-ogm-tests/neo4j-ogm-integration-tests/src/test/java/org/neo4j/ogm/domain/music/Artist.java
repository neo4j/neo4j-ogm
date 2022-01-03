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

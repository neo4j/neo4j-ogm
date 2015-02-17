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

package org.neo4j.ogm.unit.typeconversion;

import org.junit.Test;
import org.neo4j.ogm.domain.convertible.bytes.Photo;
import org.neo4j.ogm.metadata.MetaData;
import org.neo4j.ogm.metadata.info.ClassInfo;
import org.neo4j.ogm.typeconversion.AttributeConverter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestByteArrayConversion {

    private static final MetaData metaData = new MetaData("org.neo4j.ogm.domain.convertible.bytes");
    private static final ClassInfo photoInfo = metaData.classInfo("Photo");

    @Test
    public void testConvertersLoaded() {

        assertTrue(photoInfo.propertyField("image").hasConverter());
        assertTrue(photoInfo.propertyGetter("image").hasConverter());
        assertTrue(photoInfo.propertySetter("image").hasConverter());

    }

    @Test
    public void setImageAndCheck() {

        Photo photo = new Photo();
        AttributeConverter converter = photoInfo.propertyGetter("image").converter();

        photo.setImage(new byte[] {1,2,3,4});

        assertEquals("AQIDBA==", converter.toGraphProperty(photo.getImage()));
    }

    @Test
    public void getImageAndCheck() {

        Photo photo = new Photo();
        AttributeConverter converter = photoInfo.propertyGetter("image").converter();

        photo.setImage((byte[]) converter.toEntityAttribute("AQIDBA=="));

        byte[] image = photo.getImage();
        assertEquals(4, image.length);
        assertEquals(1, image[0]);
        assertEquals(2, image[1]);
        assertEquals(3, image[2]);
        assertEquals(4, image[3]);

    }


}

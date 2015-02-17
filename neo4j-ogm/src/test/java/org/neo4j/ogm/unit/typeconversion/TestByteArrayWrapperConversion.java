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
import org.neo4j.ogm.domain.convertible.bytes.PhotoWrapper;
import org.neo4j.ogm.metadata.MetaData;
import org.neo4j.ogm.metadata.info.ClassInfo;
import org.neo4j.ogm.typeconversion.AttributeConverter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestByteArrayWrapperConversion {

    private static final MetaData metaData = new MetaData("org.neo4j.ogm.domain.convertible.bytes");
    private static final ClassInfo photoInfo = metaData.classInfo("PhotoWrapper");

    @Test
    public void testConvertersLoaded() {
        assertTrue(photoInfo.propertyField("image").hasConverter());
        assertTrue(photoInfo.propertyGetter("image").hasConverter());
        assertTrue(photoInfo.propertySetter("image").hasConverter());
    }

    @Test
    public void setImageAndCheck() {

        PhotoWrapper photo = new PhotoWrapper();
        AttributeConverter converter = photoInfo.propertyGetter("image").converter();

        photo.setImage(new Byte[] {1,2,3,4});

        assertEquals("AQIDBA==", converter.toGraphProperty(photo.getImage()));
    }

    @Test
    public void getImageAndCheck() {

        PhotoWrapper photo = new PhotoWrapper();
        AttributeConverter converter = photoInfo.propertyGetter("image").converter();

        photo.setImage((Byte[]) converter.toEntityAttribute("AQIDBA=="));

        Byte[] image = photo.getImage();
        assertEquals(4, image.length);
        assertEquals(Byte.decode("1"), image[0]);
        assertEquals(Byte.decode("2"), image[1]);
        assertEquals(Byte.decode("3"), image[2]);
        assertEquals(Byte.decode("4"), image[3]);

    }

}

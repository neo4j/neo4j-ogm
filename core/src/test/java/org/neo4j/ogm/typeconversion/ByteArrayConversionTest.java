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

package org.neo4j.ogm.typeconversion;

import org.junit.Test;
import org.neo4j.ogm.metadata.MetadataMap;
import org.neo4j.ogm.domain.convertible.bytes.Photo;
import org.neo4j.ogm.metadata.ClassMetadata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Vince Bickers
 */
public class ByteArrayConversionTest {

    private static final MetadataMap metaData = new MetadataMap("org.neo4j.ogm.domain.convertible.bytes");
    private static final ClassMetadata photoInfo = metaData.classInfo("Photo");

    @Test
    public void testConvertersLoaded() {

        assertTrue(photoInfo.propertyField("image").hasPropertyConverter());
        assertTrue(photoInfo.propertyField("image").hasPropertyConverter());
        assertTrue(photoInfo.propertyField("image").hasPropertyConverter());

    }

    @Test
    public void setImageAndCheck() {

        Photo photo = new Photo();
        AttributeConverter converter = photoInfo.propertyField("image").getPropertyConverter();

        photo.setImage(new byte[]{1, 2, 3, 4});

        assertEquals("AQIDBA==", converter.toGraphProperty(photo.getImage()));
    }

    @Test
    public void getImageAndCheck() {

        Photo photo = new Photo();
        AttributeConverter converter = photoInfo.propertyField("image").getPropertyConverter();

        photo.setImage((byte[]) converter.toEntityAttribute("AQIDBA=="));

        byte[] image = photo.getImage();
        assertEquals(4, image.length);
        assertEquals(1, image[0]);
        assertEquals(2, image[1]);
        assertEquals(3, image[2]);
        assertEquals(4, image[3]);

    }


}

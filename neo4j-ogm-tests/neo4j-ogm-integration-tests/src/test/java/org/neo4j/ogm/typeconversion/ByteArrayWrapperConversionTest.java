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
package org.neo4j.ogm.typeconversion;

import static org.assertj.core.api.Assertions.*;

import org.junit.Test;
import org.neo4j.ogm.domain.convertible.bytes.PhotoWrapper;
import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.metadata.MetaData;

/**
 * @author Vince Bickers
 */
public class ByteArrayWrapperConversionTest {

    private static final MetaData metaData = new MetaData("org.neo4j.ogm.domain.convertible.bytes");
    private static final ClassInfo photoInfo = metaData.classInfo("PhotoWrapper");

    @Test
    public void testConvertersLoaded() {
        assertThat(photoInfo.propertyField("image").hasPropertyConverter()).isTrue();
    }

    @Test
    public void setImageAndCheck() {

        PhotoWrapper photo = new PhotoWrapper();
        AttributeConverter converter = photoInfo.propertyField("image").getPropertyConverter();

        photo.setImage(new Byte[] { 1, 2, 3, 4 });

        assertThat(converter.toGraphProperty(photo.getImage())).isEqualTo("AQIDBA==");
    }

    @Test
    public void getImageAndCheck() {

        PhotoWrapper photo = new PhotoWrapper();
        AttributeConverter converter = photoInfo.propertyField("image").getPropertyConverter();

        photo.setImage((Byte[]) converter.toEntityAttribute("AQIDBA=="));

        Byte[] image = photo.getImage();
        assertThat(image.length).isEqualTo(4);
        assertThat(image[0]).isEqualTo(Byte.decode("1"));
        assertThat(image[1]).isEqualTo(Byte.decode("2"));
        assertThat(image[2]).isEqualTo(Byte.decode("3"));
        assertThat(image[3]).isEqualTo(Byte.decode("4"));
    }
}

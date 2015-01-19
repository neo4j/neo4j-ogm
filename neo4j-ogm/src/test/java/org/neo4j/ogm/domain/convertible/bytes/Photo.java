package org.neo4j.ogm.domain.convertible.bytes;

import org.neo4j.ogm.annotation.typeconversion.Convert;
import org.neo4j.ogm.typeconversion.ByteArrayBase64Converter;

public class Photo {

    Long id;

    // user-defined converter
    @Convert(ByteArrayBase64Converter.class)
    private byte[] image;


    // should use default converter
    public byte[] getImage() {
        return image;
    }

    // should use default converter
    public void setImage(byte[] image) {
        this.image = image;
    }
}

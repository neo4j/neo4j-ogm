package org.neo4j.ogm.domain.convertible.bytes;

import org.neo4j.ogm.annotation.typeconversion.Convert;
import org.neo4j.ogm.typeconversion.ByteArrayWrapperBase64Converter;

// like a photo but image uses Byte[] instead of byte[]
public class PhotoWrapper {

    Long id;

    // user-defined converter
    @Convert(ByteArrayWrapperBase64Converter.class)
    private Byte[] image;


    // should use default converter
    public Byte[] getImage() {
        return image;
    }

    // should use default converter
    public void setImage(Byte[] image) {
        this.image = image;
    }
}

package org.neo4j.ogm.typeconversion;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.ArrayUtils;

/**
 * By default the OGM will map Byte[] wrapped byte[] objects to Base64
 * String values when being stored as a node / relationship property
 *
 * The conversion between the primitive byte[] class and its wrapper
 * Byte[] means that this converter is slightly slower than
 * using the ByteArray64Converter, which works with primitive
 * byte arrays directly.
 */
public class ByteArrayWrapperBase64Converter implements AttributeConverter<Byte[], String> {

    @Override
    public String toGraphProperty(Byte[] value) {
        if (value == null) return null;
        return Base64.encodeBase64String(ArrayUtils.toPrimitive(value));
    }

    @Override
    public Byte[] toEntityAttribute(String value) {
        if (value == null) return null;
        byte[] bytes = Base64.decodeBase64(value);
        Byte[] wrapper = new Byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            wrapper[i] = Byte.valueOf(bytes[i]);  // preferable to new Byte(..) hence not using Apache toObject()
        }
        return wrapper;
    }

}

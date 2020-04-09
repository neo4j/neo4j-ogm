package org.neo4j.ogm.domain.gh787;

import org.neo4j.ogm.typeconversion.AttributeConverter;

/**
 * @author Gerrit Meier
 */
public class MyVeryOwnIdTypeConverter implements AttributeConverter<MyVeryOwnIdType, String> {

    public String toGraphProperty(MyVeryOwnIdType id) {
        return id.getValue();
    }

    public MyVeryOwnIdType toEntityAttribute(String value) {
        return new MyVeryOwnIdType(value);
    }
}

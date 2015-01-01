package org.neo4j.ogm.domain.convertible.enums;

import org.neo4j.ogm.typeconversion.AttributeConverter;

public class NumberSystemDomainConverter implements AttributeConverter<NumberSystem, String> {

    @Override
    public String toGraphProperty(NumberSystem value) {
        return value.getDomain();
    }

    @Override
    public Enum toEntityAttribute(String value) {
        for (NumberSystem numberSystem : NumberSystem.values()) {
            if (numberSystem.getDomain().equals(value)) {
                return numberSystem;
            }
        }
        throw new RuntimeException("Conversion failed!");
    }

}

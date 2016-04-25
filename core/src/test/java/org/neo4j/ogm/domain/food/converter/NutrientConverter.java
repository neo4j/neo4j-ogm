package org.neo4j.ogm.domain.food.converter;

import org.neo4j.ogm.domain.food.outOfScope.DiabetesRisk;
import org.neo4j.ogm.typeconversion.AttributeConverter;

/**
 * Created by Mihai Raulea on 4/25/2016.
 */
public class NutrientConverter implements AttributeConverter<DiabetesRisk, String> {

@Override
public String toGraphProperty(DiabetesRisk value) {
    return value.toString();
}

@Override
public DiabetesRisk toEntityAttribute(String value) {
    for (DiabetesRisk nutrient: DiabetesRisk.values()) {
        if (nutrient.equals(value)) {
                return nutrient;
        }
    }
    throw new RuntimeException("NutrientConverter failed!");
}

}

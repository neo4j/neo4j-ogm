package org.neo4j.ogm.domain.food.entities.inScope;

import org.neo4j.ogm.annotation.typeconversion.EnumString;

/**
 * Created by Mihai Raulea on 4/25/2016.
 */
public class PizzaWithScannedEnumNoPropertyAnnotationWithConverter extends PizzaScannedEnum {

    @EnumString(value = DiabetesRisk.class)
    public DiabetesRisk diabetesRisk;

}

package org.neo4j.ogm.domain.food.entities;

import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.typeconversion.EnumString;

/**
 * Created by Mihai Raulea on 4/25/2016.
 */
public class PizzaWithScannedEnumWithPropertyAnnotationWithConverter extends PizzaScannedEnum {

    @Property(name="scanned")
    @EnumString(value = DiabetesRisk.class)
    public DiabetesRisk diabetesRisk;

}

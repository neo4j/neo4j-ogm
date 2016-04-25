package org.neo4j.ogm.domain.food.entities;

import org.neo4j.ogm.annotation.Property;

/**
 * Created by Mihai Raulea on 4/25/2016.
 */
public class PizzaWithScannedEnumWithPropertyAnnotationNoConverter extends PizzaScannedEnum {

    public Long id;
    public double noOfCalories;
    @Property(name="scanned")
    public DiabetesRisk diabetesRisk;

}

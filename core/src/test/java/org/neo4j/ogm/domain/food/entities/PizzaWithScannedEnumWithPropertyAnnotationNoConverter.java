package org.neo4j.ogm.domain.food.entities;

import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.domain.food.outOfScope.DiabetesRisk;

/**
 * Created by Mihai Raulea on 4/25/2016.
 */
public class PizzaWithScannedEnumWithPropertyAnnotationNoConverter extends PizzaScannedEnum {

    @Property(name="scanned")
    public DiabetesRisk diabetesRisk;

}

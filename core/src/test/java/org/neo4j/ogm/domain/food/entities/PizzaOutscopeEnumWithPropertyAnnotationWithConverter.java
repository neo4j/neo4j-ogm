package org.neo4j.ogm.domain.food.entities;

import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.typeconversion.EnumString;
import org.neo4j.ogm.domain.food.outOfScope.Nutrient;

/**
 * Created by Mihai Raulea on 4/25/2016.
 */
public class PizzaOutscopeEnumWithPropertyAnnotationWithConverter extends PizzaOutscope {

    @Property(name="scanned")
    @EnumString(value = Nutrient.class)
    public Nutrient outscopeNutrient;

}

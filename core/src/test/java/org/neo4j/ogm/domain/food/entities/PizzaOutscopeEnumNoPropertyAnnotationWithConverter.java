package org.neo4j.ogm.domain.food.entities;

import org.neo4j.ogm.annotation.typeconversion.EnumString;
import org.neo4j.ogm.domain.food.outOfScope.Nutrient;

/**
 * Created by Mihai Raulea on 4/25/2016.
 */
public class PizzaOutscopeEnumNoPropertyAnnotationWithConverter extends PizzaOutscope{

    @EnumString(value = Nutrient.class)
    public Nutrient outscopeNutrient;

}

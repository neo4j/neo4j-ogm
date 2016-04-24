package org.neo4j.ogm.domain.food.entities;

import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.domain.food.outOfScope.Nutrient;

/**
 * Created by Mihai Raulea on 4/21/2016.
 */
public class Pizza {

    Long id;
    public int noOfCaloriesPer100Grams;
    @Property(name="dominantNutrient")
    public Nutrient nutrient = Nutrient.CARBS;

}

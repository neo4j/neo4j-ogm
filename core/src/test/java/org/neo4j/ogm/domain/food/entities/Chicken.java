package org.neo4j.ogm.domain.food.entities;

import org.neo4j.ogm.domain.food.outOfScope.Nutrient;

/**
 * Created by Mihai Raulea on 4/21/2016.
 */
public class Chicken {

    Long id;
    public int noOfCaloriesPer100Grams;
    public Nutrient nutrient = Nutrient.PROTEIN;

}

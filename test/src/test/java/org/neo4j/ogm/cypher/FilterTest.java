/*
 * Copyright (c) 2002-2017 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 *  conditions of the subcomponent's license, as noted in the LICENSE file.
 */

package org.neo4j.ogm.cypher;

import static org.assertj.core.api.Assertions.*;

import java.util.Iterator;

import org.junit.Test;
import org.neo4j.ogm.cypher.function.ContainsAnyComparison;
import org.neo4j.ogm.cypher.function.DistanceComparison;
import org.neo4j.ogm.cypher.function.DistanceFromPoint;

public class FilterTest {

    @Test
    public void toCypher() {
        Filter filter = new Filter("moons", ComparisonOperator.LESS_THAN, 23);
        filter.setBooleanOperator(BooleanOperator.AND);
        assertThat(filter.toCypher("n", true)).isEqualTo("WHERE n.`moons` < { `moons_0` } ");
    }

    @Test
    public void toCypher_function() {
        DistanceComparison function = new DistanceComparison(new DistanceFromPoint(37.4, 112.1, 1000.0));
        Filter filter = new Filter(function, ComparisonOperator.LESS_THAN);
        filter.setBooleanOperator(BooleanOperator.AND);
        filter.setNegated(true);
        assertThat(filter.toCypher("n", true))
            .isEqualTo("WHERE NOT(distance(point(n),point({latitude:{lat}, longitude:{lon}})) < {distance} ) ");
    }

    @Test
    public void inCollectionFilterTest() {
        ContainsAnyComparison filterFunction = new ContainsAnyComparison("test");
        Filter filter = new Filter("property", filterFunction);
        assertThat(filter.toCypher("n", true))
            .isEqualTo("WHERE ANY(collectionFields IN {`property_0`} WHERE collectionFields in n.`property`) ");
        assertThat(filter.parameters().get("property_0")).isEqualTo("test");

    }

    @Test
    public void joinFiltersWithAndMethod() {
        Filter filter1 = new Filter("property1", ComparisonOperator.EQUALS, "value1");
        Filter filter2 = new Filter("property2", ComparisonOperator.EQUALS, "value2");

        Filters andFilter = filter1.and(filter2);

        assertThat(filter2.getBooleanOperator()).isEqualTo(BooleanOperator.AND);
        Iterator<Filter> iterator = andFilter.iterator();
        assertThat(iterator.next()).isEqualTo(filter1);
        assertThat(iterator.next()).isEqualTo(filter2);
    }

    @Test
    public void joinFiltersWithOrMethod() {
        Filter filter1 = new Filter("property1", ComparisonOperator.EQUALS, "value1");
        Filter filter2 = new Filter("property2", ComparisonOperator.EQUALS, "value2");

        Filters andFilter = filter1.or(filter2);

        assertThat(filter2.getBooleanOperator()).isEqualTo(BooleanOperator.OR);
        Iterator<Filter> iterator = andFilter.iterator();
        assertThat(iterator.next()).isEqualTo(filter1);
        assertThat(iterator.next()).isEqualTo(filter2);
    }
}

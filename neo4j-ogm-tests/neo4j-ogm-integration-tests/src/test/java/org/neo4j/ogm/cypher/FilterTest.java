/*
 * Copyright (c) 2002-2019 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neo4j.ogm.cypher;

import static org.assertj.core.api.Assertions.*;

import java.util.Iterator;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.neo4j.ogm.cypher.function.ContainsAnyComparison;
import org.neo4j.ogm.cypher.function.DistanceComparison;
import org.neo4j.ogm.cypher.function.DistanceFromPoint;
import org.neo4j.ogm.cypher.function.PropertyComparison;

/**
 * @author Gerrit Meier
 * @author Michael J. Simons
 */
public class FilterTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

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
            .isEqualTo(
                "WHERE NOT(distance(point({latitude: n.latitude, longitude: n.longitude}),point({latitude:{lat}, longitude:{lon}})) < {distance} ) ");
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

    @Test
    public void isNullComparisionShouldWork() {
        Filter filter = new Filter("thing", ComparisonOperator.IS_NULL);
        filter.setBooleanOperator(BooleanOperator.AND);
        assertThat(filter.toCypher("n", true)).isEqualTo("WHERE n.`thing` IS NULL ");
    }

    @Test
    public void isExistsComparisionShouldWork() {
        Filter filter = new Filter("thing", ComparisonOperator.EXISTS);
        filter.setBooleanOperator(BooleanOperator.AND);
        assertThat(filter.toCypher("n", true)).isEqualTo("WHERE EXISTS(n.`thing`) ");
    }

    @Test
    public void isTrueComparisionShouldWork() {
        Filter filter = new Filter("thing", ComparisonOperator.IS_TRUE);
        filter.setBooleanOperator(BooleanOperator.AND);
        assertThat(filter.toCypher("n", true)).isEqualTo("WHERE n.`thing` = true ");
    }

    @Test
    public void equalComparisionShouldWork() {
        final String value = "someOtherThing";
        Filter filter = new Filter("thing", ComparisonOperator.EQUALS, value);
        filter.setFunction(new PropertyComparison(value));
        assertThat(filter.toCypher("n", true)).isEqualTo("WHERE n.`thing` = { `thing_0` } ");

        filter = new Filter("thing", ComparisonOperator.EQUALS, value);
        filter.ignoreCase();
        assertThat(filter.toCypher("n", true)).isEqualTo("WHERE toLower(n.`thing`) = toLower({ `thing_0` }) ");
    }

    @Test
    public void ignoreCaseShouldNotBeApplicableToComparisonOtherThanEquals() {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("ignoreCase is only supported for EQUALS comparison");

        Filter filter = new Filter("thing", ComparisonOperator.IS_NULL);
        filter.setBooleanOperator(BooleanOperator.AND);
        assertThat(filter.toCypher("n", true)).isEqualTo("WHERE n.`thing` IS NULL ");
        filter.ignoreCase();

        assertThat(filter.toCypher("n", true)).isEqualTo("WHERE n.`thing` IS NULL ");
    }
}

/*
 * Copyright (c) 2002-2022 "Neo4j,"
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

import static org.neo4j.ogm.cypher.ComparisonOperator.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.neo4j.ogm.annotation.Relationship.Direction;
import org.neo4j.ogm.cypher.function.DistanceComparison;
import org.neo4j.ogm.cypher.function.FilterFunction;
import org.neo4j.ogm.cypher.function.PropertyComparison;
import org.neo4j.ogm.support.CollectionUtils;
import org.neo4j.ogm.typeconversion.AttributeConverter;

/**
 * A parameter along with filter information to be added to a query.
 *
 * @author Luanne Misquitta
 * @author Jasper Blues
 * @author Michael J. Simons
 */
public class Filter implements FilterWithRelationship {

    /**
     * Index is used to to ensure unique parameter names when a collection of filters are used.
     *
     * @see Filters
     */
    private int index;

    /**
     * The property name on the entity to be used in the filter
     */
    private String propertyName;

    /**
     * The boolean operator used to append this filter to the previous ones.
     * Mandatory if the filter is not the first and only filter in the list.
     */
    private BooleanOperator booleanOperator = BooleanOperator.NONE;

    /**
     * Determines whether or not this filter condition should be negated when added to the query.
     */
    private boolean negated;

    /**
     * The parent entity which owns this filter
     */
    private Class<?> ownerEntityType;

    /**
     * The label of the entity which contains the nested property
     */
    private String nestedEntityTypeLabel;

    /**
     * The property name of the nested property on the parent entity
     */
    private String nestedPropertyName;

    /**
     * The type of the entity that owns the nested property
     */
    private Class<?> nestedPropertyType;

    /**
     * The relationship type to be used for a nested property
     */
    private String relationshipType;

    /**
     * The relationship direction from the parent entity to the nested property
     */
    private Direction relationshipDirection;

    private AttributeConverter propertyConverter;

    /**
     * Whether the nested property is backed by a relationship entity
     */
    private boolean nestedRelationshipEntity;

    private FilterFunction<?> function;

    private List<NestedPathSegment> nestedPath;

    public Filter(FilterFunction function) {
        this(null, function);
    }

    public Filter(DistanceComparison distanceComparisonFunction, ComparisonOperator comparisonOperator) {
        this(null, distanceComparisonFunction.withOperator(comparisonOperator));
    }

    public Filter(String propertyName, ComparisonOperator comparisonOperator, Object propertyValue) {
        this(propertyName, comparisonOperator.compare(propertyValue));
    }

    public Filter(String propertyName, FilterFunction filterFunction) {
        this.index = 0;
        this.propertyName = propertyName;
        this.function = filterFunction;
    }

    public Filter(String propertyName, ComparisonOperator comparisonOperator) {
        this(propertyName, comparisonOperator.compare(null));
    }

    public static void setNameFromProperty(Filter filter, String propertyName) {
        filter.propertyName = propertyName;
    }

    public Direction getRelationshipDirection() {
        return relationshipDirection;
    }

    public void setRelationshipDirection(Direction relationshipDirection) {
        this.relationshipDirection = relationshipDirection;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public BooleanOperator getBooleanOperator() {
        return booleanOperator;
    }

    public void setBooleanOperator(BooleanOperator booleanOperator) {
        this.booleanOperator = booleanOperator;
    }

    /**
     * Convenience method to chain filters using {@link BooleanOperator#AND}.
     *
     * @param filter to be chained
     * @return new {@link Filters} object containing both filters.
     */

    public Filters and(Filter filter) {
        filter.setBooleanOperator(BooleanOperator.AND);
        return new Filters(this, filter);
    }

    /**
     * Convenience method to chain filters using {@link BooleanOperator#OR}.
     *
     * @param filter to be chained.
     * @return new {@link Filters} object containing both filters.
     */
    public Filters or(Filter filter) {
        filter.setBooleanOperator(BooleanOperator.OR);
        return new Filters(this, filter);
    }

    /**
     * Sets this filter to ignore the case in a property comparison when using the EQUALS operator.
     *
     * @return the same filter instance
     * @throws IllegalStateException if the filters function is null, not a property comparison or the operator of the
     *                               is not an EQUALS-operator.
     * @since 3.1.1
     */
    public Filter ignoreCase() {
        if (!(this.function instanceof PropertyComparison)) {
            throw new IllegalStateException("ignoreCase is only supported for a filter based on property comparison");
        } else {
            PropertyComparison propertyComparision = (PropertyComparison) this.function;
            if (!EnumSet.of(EQUALS, CONTAINING, STARTING_WITH, ENDING_WITH).contains(propertyComparision.getOperator())) {
                throw new IllegalStateException(
                    String.format("ignoreCase is only supported for %s or %s comparison", EQUALS.name(),
                        CONTAINING.name())
                );
            }
            this.function = new PropertyComparison.CaseInsensitiveEqualsComparison(
                propertyComparision.getOperator(), propertyComparision.getValue());
            return this;
        }
    }

    /**
     * @return <code>true</code> if this filter expression is to be negated when it's appended to the query, <code>false</code>
     * if not
     */
    public boolean isNegated() {
        return negated;
    }

    /**
     * @param negated Whether or not the filter expression is to be negated
     */
    public void setNegated(boolean negated) {
        this.negated = negated;
    }

    public Class<?> getOwnerEntityType() {
        return ownerEntityType;
    }

    public void setOwnerEntityType(Class<?> ownerEntityType) {
        this.ownerEntityType = ownerEntityType;
    }

    public String getNestedPropertyName() {
        return nestedPropertyName;
    }

    public void setNestedPropertyName(String nestedPropertyName) {
        this.nestedPropertyName = nestedPropertyName;
    }

    public String getRelationshipType() {
        return relationshipType;
    }

    public void setRelationshipType(String relationshipType) {
        this.relationshipType = relationshipType;
    }

    public boolean isNested() {
        return this.nestedPropertyName != null;
    }

    public Class<?> getNestedPropertyType() {
        return nestedPropertyType;
    }

    public void setNestedPropertyType(Class<?> nestedPropertyType) {
        this.nestedPropertyType = nestedPropertyType;
    }

    public String getNestedEntityTypeLabel() {
        return nestedEntityTypeLabel;
    }

    public void setNestedEntityTypeLabel(String nestedEntityTypeLabel) {
        this.nestedEntityTypeLabel = nestedEntityTypeLabel;
    }

    public void setNestedPath(NestedPathSegment... path) {
        nestedPath = new ArrayList<>(Arrays.asList(path));
    }

    public List<NestedPathSegment> getNestedPath() {
        return nestedPath;
    }

    public static class NestedPathSegment implements FilterWithRelationship {
        private final String propertyName;
        private final Class propertyType;

        private String relationshipType;
        private Direction relationshipDirection;
        private String nestedEntityTypeLabel;
        private boolean nestedRelationshipEntity;

        public NestedPathSegment(String propertyName, Class propertyType) {
            this.propertyName = propertyName;
            this.propertyType = propertyType;
        }

        public String getPropertyName() {
            return propertyName;
        }

        public Class getPropertyType() {
            return propertyType;
        }

        public String getRelationshipType() {
            return relationshipType;
        }

        public void setRelationshipType(String relationshipType) {
            this.relationshipType = relationshipType;
        }

        public void setRelationshipDirection(Direction relationshipDirection) {
            this.relationshipDirection = relationshipDirection;
        }

        public Direction getRelationshipDirection() {
            return relationshipDirection;
        }

        public void setNestedEntityTypeLabel(String nestedEntityTypeLabel) {
            this.nestedEntityTypeLabel = nestedEntityTypeLabel;
        }

        public String getNestedEntityTypeLabel() {
            return nestedEntityTypeLabel;
        }

        public void setNestedRelationshipEntity(boolean nestedRelationshipEntity) {
            this.nestedRelationshipEntity = nestedRelationshipEntity;
        }

        public boolean isNestedRelationshipEntity() {
            return nestedRelationshipEntity;
        }
    }

    public boolean isDeepNested() {
        return getNestedPath() != null && !getNestedPath().isEmpty();
    }

    public boolean isNestedRelationshipEntity() {
        return nestedRelationshipEntity;
    }

    public void setNestedRelationshipEntity(boolean nestedRelationshipEntity) {
        this.nestedRelationshipEntity = nestedRelationshipEntity;
    }

    private String uniqueParameterName(String originalName) {

        // We should maybe include the original name here as well. This changes the generated queries,
        // but would prevent bugs with multiple filters of the same type or filter functions using
        // more than one parameter (as {@link org.neo4j.ogm.cypher.function.NativeDistanceComparison} currently does)
        String format = "%2$d";

        if (isNested()) {
            format = getNestedPropertyName() + "_" + getPropertyName() + "_" + format;
        } else if (getPropertyName() != null) {
            format = getPropertyName() + "_" + format;
        }

        return String.format(format, originalName, index);
    }

    public AttributeConverter getPropertyConverter() {
        return propertyConverter;
    }

    public void setPropertyConverter(AttributeConverter propertyConverter) {
        this.propertyConverter = propertyConverter;
    }

    /**
     * Used by Filters to assign an index, so that unique parameter names are ensured when filters are used in a
     * collection. Should not be called directly.
     *
     * @param index The index
     */
    void setIndex(int index) {
        this.index = index;
    }

    /**
     * @param nodeIdentifier The node identifier
     * @param addWhereClause The add where clause.
     * @return The filter state as a CYPHER fragment.
     */
    public String toCypher(String nodeIdentifier, boolean addWhereClause) {

        String fragment = this.function.expression(nodeIdentifier, propertyName, this::uniqueParameterName);
        String suffix = isNegated() ? negate(fragment) : fragment;
        return cypherPrefix(addWhereClause) + suffix;
    }

    public Map<String, Object> parameters() {

        AttributeConverter applicablePropertyConverter = this.getPropertyConverter();
        PropertyValueTransformer valueTransformer;
        if (applicablePropertyConverter == null) {
            valueTransformer = new NoOpPropertyValueTransformer();
        } else {
            valueTransformer = value -> {
                List<Object> convertedValues = StreamSupport
                    .stream(CollectionUtils.iterableOf(value).spliterator(), false)
                    .map((Function<Object, Object>) applicablePropertyConverter::toGraphProperty)
                    .collect(Collectors.toList());
                if (convertedValues.size() == 1) {
                    return convertedValues.get(0);
                } else {
                    return convertedValues;
                }
            };
        }

        return function.parameters(this::uniqueParameterName, valueTransformer);
    }

    private String cypherPrefix(boolean addWhereClause) {
        StringBuilder cypher = new StringBuilder();
        if (addWhereClause) {
            cypher.append("WHERE ");
        } else {
            if (!getBooleanOperator().equals(BooleanOperator.NONE)) {
                cypher.append(getBooleanOperator().getValue()).append(" ");
            }
        }
        return cypher.toString();
    }

    private String negate(String expression) {
        return String.format("NOT(%s) ", expression);
    }

}

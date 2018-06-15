/*
 * Copyright (c) 2002-2018 "Neo Technology,"
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import org.neo4j.ogm.cypher.function.DistanceComparison;
import org.neo4j.ogm.cypher.function.FilterFunction;
import org.neo4j.ogm.cypher.function.PropertyComparison;
import org.neo4j.ogm.exception.core.MappingException;
import org.neo4j.ogm.typeconversion.AttributeConverter;
import org.neo4j.ogm.typeconversion.CompositeAttributeConverter;

/**
 * A parameter along with filter information to be added to a query.
 *
 * @author Luanne Misquitta
 * @author Jasper Blues
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
     * The comparison operator to use in the property filter
     */
    private ComparisonOperator comparisonOperator;

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
    private String relationshipDirection;

    private AttributeConverter propertyConverter;

    private CompositeAttributeConverter compositeAttributeConverter;

    /**
     * Whether the nested property is backed by a relationship entity
     */
    private boolean nestedRelationshipEntity;

    private FilterFunction function;

    private List<NestedPathSegment> nestedPath;

    //Primary Constructor
    public Filter(FilterFunction function) {
        this.index = 0;
        this.function = function;
        this.function.setFilter(this);
    }

    public Filter(DistanceComparison distanceComparisonFunction, ComparisonOperator comparisonOperator) {
        this.index = 0;
        this.function = distanceComparisonFunction;
        this.function.setFilter(this);
        this.comparisonOperator = comparisonOperator;
    }

    //Convenience Constructor
    public Filter(String propertyName, ComparisonOperator comparisonOperator, Object propertyValue) {
        this(new PropertyComparison(propertyValue));
        this.comparisonOperator = comparisonOperator;
        this.propertyName = propertyName;
    }

    //Convenience Constructor
    public Filter(String propertyName, FilterFunction filterFunction) {
        this(filterFunction);
        this.propertyName = propertyName;
    }

    // TODO: Split Operators up into binary and unary.
    public Filter(String propertyName, ComparisonOperator comparisonOperator) {
        this(new PropertyComparison(null));
        this.propertyName = propertyName;
        if (!EnumSet.of(ComparisonOperator.EXISTS, ComparisonOperator.IS_TRUE, ComparisonOperator.IS_NULL)
            .contains(comparisonOperator)) {
            throw new RuntimeException("This constructor can only be used with Unary comparison operators");
        }
        this.comparisonOperator = comparisonOperator;
    }

    public static void setNameFromProperty(Filter filter, String propertyName) {
        filter.propertyName = propertyName;
    }

    public String getRelationshipDirection() {
        return relationshipDirection;
    }

    public void setRelationshipDirection(String relationshipDirection) {
        this.relationshipDirection = relationshipDirection;
    }

    public String getPropertyName() {
        return propertyName;
    }

    @Deprecated
    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public ComparisonOperator getComparisonOperator() {
        return comparisonOperator;
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
        private String relationshipDirection;
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

        public void setRelationshipDirection(String relationshipDirection) {
            this.relationshipDirection = relationshipDirection;
        }

        public String getRelationshipDirection() {
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

    public String uniqueParameterName() {
        return isNested() ? getNestedPropertyName() + "_" + getPropertyName() + "_" + index :
            getPropertyName() + "_" + index;
    }

    public AttributeConverter getPropertyConverter() {
        return propertyConverter;
    }

    public void setPropertyConverter(AttributeConverter propertyConverter) {
        this.propertyConverter = propertyConverter;
    }

    public CompositeAttributeConverter getCompositeAttributeConverter() {
        return compositeAttributeConverter;
    }

    public void setCompositeConverter(CompositeAttributeConverter compositeAttributeConverter) {
        this.compositeAttributeConverter = compositeAttributeConverter;
    }

    /**
     * Returns the result of passing the property value through the transformer associated with the comparison operator
     * on this {@link Filter}.
     *
     * @return The transformed property value
     */
    public Object getTransformedPropertyValue() {
        Object value = this.function.getValue();
        if (this.getPropertyConverter() != null) {
            value = this.getPropertyConverter().toGraphProperty(value);
        } else if (this.getCompositeAttributeConverter() != null) {
            throw new MappingException("Properties with a CompositeAttributeConverter are not supported by " +
                "Filters in this version of OGM. Consider implementing a custom FilterFunction.");
        }
        return transformPropertyValue(value);
    }

    private Object transformPropertyValue(Object value) {
        if (this.comparisonOperator != null) {
            return this.comparisonOperator.getPropertyValueTransformer().transformPropertyValue(value);
        }

        return new NoOpPropertyValueTransformer().transformPropertyValue(value);
    }

    public FilterFunction getFunction() {
        return function;
    }

    public void setFunction(FilterFunction function) {
        assert function != null;
        this.function = function;
        this.function.setFilter(this);
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
        String fragment = this.function.expression(nodeIdentifier);
        String suffix = isNegated() ? negate(fragment) : fragment;
        return cypherPrefix(addWhereClause) + suffix;
    }

    public Map<String, Object> parameters() {
        return function.parameters();
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

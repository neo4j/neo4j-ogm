/*
 * Copyright (c) 2002-2016 "Neo Technology,"
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

/**
 * A parameter along with filter information to be added to a query.
 *
 * @author Luanne Misquitta
 */
public class Filter {

	/**
	 * The property name on the entity to be used in the filter
	 */
	private String propertyName;

	/**
	 * The value of the property to filter on
	 */
	private Object value;

	/**
	 * @deprecated as of 2.0.4 This is a SDN only concern and has been moved to that project.
	 * The position of the property as specified in a derived finder method
	 */
	@Deprecated
	private Integer propertyPosition;

	/**
	 * The comparison operator to use in the property filter
	 */
	private ComparisonOperator comparisonOperator = ComparisonOperator.EQUALS;

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

	/**
	 * Whether the nested property is backed by a relationship entity
	 */
	private boolean nestedRelationshipEntity;

	private FilterFunction function = FilterFunction.NONE;

	public Filter() {
	}

	public Filter(String propertyName, Object propertyValue) {
		this.propertyName = propertyName;
		this.value = propertyValue;
	}

	public Filter(FilterFunction function, Object propertyValue) {
		this.function = function;
		this.setValue(propertyValue);
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

	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		if (this.function == FilterFunction.DISTANCE && !(value instanceof DistanceComparison)) {
			throw new IllegalArgumentException("Filter function is DISTANCE therefore value must be a type of DistanceComparison");
		}
		this.value = value;
	}

	/**
	 * @deprecated use {@link #getValue()} instead.
	 */
	@Deprecated
	public Object getPropertyValue() {
		return value;
	}

	/**
	 * @deprecated use {@link #setValue(Object)} ()} instead.
	 */
	@Deprecated
	public void setPropertyValue(Object propertyValue) {
		this.value = propertyValue;
	}

	/**
	 * @return
	 * @deprecated as of 2.0.4. This is a SDN only concern and has been moved to that project.
	 */
	@Deprecated
	public Integer getPropertyPosition() {
		return propertyPosition;
	}

	/**
	 * @param propertyPosition
	 * @deprecated as of 2.0.4. This is a SDN only concern and has been moved to that project.
	 */
	@Deprecated
	public void setPropertyPosition(Integer propertyPosition) {
		this.propertyPosition = propertyPosition;
	}

	public ComparisonOperator getComparisonOperator() {
		return comparisonOperator;
	}

	public void setComparisonOperator(ComparisonOperator comparisonOperator) {
		this.comparisonOperator = comparisonOperator;
	}

	public BooleanOperator getBooleanOperator() {
		return booleanOperator;
	}

	public void setBooleanOperator(BooleanOperator booleanOperator) {
		this.booleanOperator = booleanOperator;
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

	public boolean isNestedRelationshipEntity() {
		return nestedRelationshipEntity;
	}

	public void setNestedRelationshipEntity(boolean nestedRelationshipEntity) {
		this.nestedRelationshipEntity = nestedRelationshipEntity;
	}

	/**
	 * Returns the result of passing the property value through the transformer associated with the comparison operator
	 * on this {@link Filter}.
	 *
	 * @return The transformed property value
	 */
	public Object getTransformedPropertyValue() {
		return this.comparisonOperator.getPropertyValueTransformer().transformPropertyValue(this.value);
	}

	public FilterFunction getFunction() {
		return function;
	}

	public void setFunction(FilterFunction function) {
		this.function = function;
	}

	/**
	 * @param nodeIdentifier
	 * @param addWhereClause
	 * @return The filter state as a CYPHER fragment.
	 */
	public String toCypher(String nodeIdentifier, boolean addWhereClause) {
		StringBuilder cypher = new StringBuilder();
		if (this.function == FilterFunction.NONE) {
			String uniquePropertyName = isNested() ?
					getNestedPropertyName() + "_" + getPropertyName() : getPropertyName();
			cypher.append(String.format("%s.`%s` %s { `%s` } ", nodeIdentifier, getPropertyName(),
					getComparisonOperator().getValue(), uniquePropertyName));
		} else if (this.function == FilterFunction.DISTANCE) {
			cypher.append(String.format("distance(point(%s),point({latitude:{lat}, longitude:{lon}})) " +
					"%s {distance} ", nodeIdentifier, getComparisonOperator().getValue()));
		}
		String suffix = isNegated() ? negate(cypher.toString()) : cypher.toString();
		return cypherPrefix(addWhereClause) + suffix;
	}

	public String uniquePropertyName() {
		String uniquePropertyName = getPropertyName();
		if (isNested()) {
			//Nested entities may have the same property name, so we make them unique by qualifying them with the
			// nested property name on the owning entity
			uniquePropertyName = getNestedPropertyName() + "_" + getPropertyName();
		}
		return uniquePropertyName;
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

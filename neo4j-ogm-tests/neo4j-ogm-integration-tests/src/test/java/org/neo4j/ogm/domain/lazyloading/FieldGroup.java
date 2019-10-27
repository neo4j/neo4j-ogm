package org.neo4j.ogm.domain.lazyloading;

import java.util.Objects;
import java.util.Set;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

/**
 * @author Andreas Berger
 */
@SuppressWarnings("DefaultAnnotationParam")
@NodeEntity
public class FieldGroup extends BaseEntity implements Comparable<FieldGroup> {

	private String name;
	private double sort;

	@Relationship(type = "GROUPED_BY", direction = Relationship.OUTGOING)
	private Set<FieldDefinition> fieldDefinitions;

	public String getName() {
		return name;
	}

	public FieldGroup setName(String name) {
		this.name = name;
		return this;
	}

	public double getSort() {
		return sort;
	}

	public FieldGroup setSort(double sort) {
		this.sort = sort;
		return this;
	}

	public Set<FieldDefinition> getFieldDefinitions() {
		read("fieldDefinitions");
		return fieldDefinitions;
	}

	public FieldGroup setFieldDefinitions(Set<FieldDefinition> fieldDefinitions) {
		write("fieldDefinitions", fieldDefinitions);
		this.fieldDefinitions = fieldDefinitions;
		return this;
	}

	@Override
	public int compareTo(FieldGroup that) {
		return Double.compare(this.sort, that.sort);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		FieldGroup that = (FieldGroup) o;
		return Objects.equals(name, that.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name);
	}
}

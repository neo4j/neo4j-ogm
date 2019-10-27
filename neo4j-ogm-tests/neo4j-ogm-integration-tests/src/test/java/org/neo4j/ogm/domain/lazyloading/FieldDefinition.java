package org.neo4j.ogm.domain.lazyloading;

import java.util.Objects;
import java.util.Set;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

/**
 * @author Andreas Berger
 */
@NodeEntity
public class FieldDefinition extends BaseEntity implements Comparable<FieldDefinition>{

    private String fieldKey;

    private double sort;

    @Relationship(type = "WITH_FIELD_VALUE", direction = Relationship.INCOMING)
    private Set<NodeData> nodeData;

    @Relationship(type = "GROUPED_BY", direction = Relationship.INCOMING)
    private FieldGroup fieldGroup;


    public String getFieldKey() {
        return fieldKey;
    }

    public FieldDefinition setFieldKey(String fieldKey) {
        this.fieldKey = fieldKey;
        return this;
    }

    public double getSort() {
        return sort;
    }

    public FieldDefinition setSort(double sort) {
        this.sort = sort;
        return this;
    }

    public FieldGroup getFieldGroup() {
        read("fieldGroup");
        return fieldGroup;
    }

    public FieldDefinition setFieldGroup(FieldGroup fieldGroup) {
        write("fieldGroup", fieldGroup);
        this.fieldGroup = fieldGroup;
        return this;
    }

    public Set<NodeData> getNodeData() {
        read("nodeData");
        return nodeData;
    }

    public FieldDefinition setNodeData(Set<NodeData> withFieldValue) {
        write("nodeData", withFieldValue);
        this.nodeData = withFieldValue;
        return this;
    }

    @Override
    public String toString() {
        return fieldKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FieldDefinition that = (FieldDefinition) o;
        return Objects.equals(fieldKey, that.fieldKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fieldKey);
    }

    @Override
    public int compareTo(FieldDefinition that) {
        return Double.compare(this.sort, that.sort);
    }
}

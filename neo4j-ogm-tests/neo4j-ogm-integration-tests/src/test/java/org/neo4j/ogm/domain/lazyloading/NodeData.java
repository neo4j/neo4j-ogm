package org.neo4j.ogm.domain.lazyloading;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

/**
 * @author Andreas Berger
 */
@RelationshipEntity(type = "WITH_FIELD_VALUE")
public class NodeData extends BaseEntity {

    @StartNode
    private BaseNodeEntity baseNode;

    @EndNode
    private FieldDefinition fieldDefinition;

    private String value;

    public BaseNodeEntity getBaseNode() {
        return baseNode;
    }

    public NodeData setBaseNode(BaseNodeEntity node) {
        this.baseNode = node;
        return this;
    }

    public FieldDefinition getFieldDefinition() {
        return fieldDefinition;
    }

    public NodeData setFieldDefinition(FieldDefinition fieldDefinition) {
        this.fieldDefinition = fieldDefinition;
        return this;
    }

    public String getValue() {
        return value;
    }

    public NodeData setValue(String value) {
        this.value = value;
        return this;
    }
}

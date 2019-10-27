package org.neo4j.ogm.domain.lazyloading;

import java.util.Objects;
import java.util.Set;

import org.neo4j.ogm.annotation.Index;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

/**
 * @author Andreas Berger
 */
@NodeEntity
public class LabelGroup extends BaseEntity {

    @Index(unique = true)
    private String key;


    @Relationship(type = "BELONGS_TO", direction = Relationship.INCOMING)
    private Set<Label> labels;

    @Relationship(type = "CAN_HAVE_LABEL_OF", direction = Relationship.INCOMING)
    private Set<NodeType> nodeTypes;

    public String getKey() {
        return key;
    }

    public LabelGroup setKey(String key) {
        this.key = key;
        return this;
    }

    public Set<Label> getLabels() {
        read("labels");
        return labels;
    }

    public LabelGroup setLabels(Set<Label> labels) {
        write("labels", labels);
        this.labels = labels;
        return this;
    }

    public Set<NodeType> getNodeTypes() {
        read("nodeTypes");
        return nodeTypes;
    }

    public LabelGroup setNodeTypes(Set<NodeType> nodeTypes) {
        write("nodeTypes", this.nodeTypes);
        this.nodeTypes = nodeTypes;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LabelGroup that = (LabelGroup) o;
        return Objects.equals(key, that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }
}

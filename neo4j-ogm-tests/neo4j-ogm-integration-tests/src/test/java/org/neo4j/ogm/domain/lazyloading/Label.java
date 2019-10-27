package org.neo4j.ogm.domain.lazyloading;

import java.util.Objects;

import org.neo4j.ogm.annotation.Index;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

/**
 * @author Andreas Berger
 */
@NodeEntity
public class Label extends BaseEntity {

    @Index
    private String key;

    @Relationship(type = "BELONGS_TO", direction = Relationship.OUTGOING)
    private LabelGroup belongsTo;

    public String getKey() {
        return key;
    }

    public Label setKey(String key) {
        this.key = key;
        return this;
    }

    public LabelGroup getLabelGroup() {
        read("belongsTo");
        return belongsTo;
    }

    public Label setLabelGroup(LabelGroup labelGroup) {
        write("belongsTo", labelGroup);
        this.belongsTo = labelGroup;
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
        Label label = (Label) o;
        return Objects.equals(key, label.key) &&
            Objects.equals(belongsTo, label.belongsTo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, belongsTo);
    }
}

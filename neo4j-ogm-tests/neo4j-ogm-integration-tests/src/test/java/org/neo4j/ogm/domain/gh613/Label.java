package org.neo4j.ogm.domain.gh613;

import java.util.Objects;

import org.neo4j.ogm.annotation.Index;
import org.neo4j.ogm.annotation.NodeEntity;

/**
 * @author Andreas Berger
 */
@NodeEntity
public class Label extends BaseEntity {

    @Index
    private String key;

    public String getKey() {
        return key;
    }

    public Label setKey(String key) {
        this.key = key;
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
        return Objects.equals(key, label.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }

    @Override
    public String toString() {
        return "Label{" +
            "key='" + key + '\'' +
            '}';
    }
}

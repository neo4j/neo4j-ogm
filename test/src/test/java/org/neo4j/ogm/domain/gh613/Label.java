package org.neo4j.ogm.domain.gh613;

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

    @Override public String toString() {
        return "Label{" +
            "key='" + key + '\'' +
            '}';
    }
}

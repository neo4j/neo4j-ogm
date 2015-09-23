package org.neo4j.ogm.domain.annotations;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.neo4j.ogm.annotation.NodeEntity;

/**
 * @author vince
 */
@NodeEntity
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = SimpleNode.class, name="simple"),
        @JsonSubTypes.Type(value = OtherNode.class, name="other")
})
public interface Node {
}

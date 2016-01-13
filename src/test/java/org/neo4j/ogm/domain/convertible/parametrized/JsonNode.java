package org.neo4j.ogm.domain.convertible.parametrized;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.typeconversion.Convert;

import java.util.Map;

/**
 * @author vince
 */
@NodeEntity
public class JsonNode {

    @GraphId
    public Long id;

    @Convert(MapJson.class)
    public Map<String,Object> payload;

}
package org.neo4j.ogm.domain.convertible.parametrized;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.neo4j.ogm.typeconversion.AttributeConverter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author vince
 */
public class MapJson implements AttributeConverter<Map<String, Object>, String> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String toGraphProperty(Map<String, Object> value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, Object> toEntityAttribute(String value) {
        try {
            return objectMapper.readValue(value, new HashMap<String, Object>().getClass());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
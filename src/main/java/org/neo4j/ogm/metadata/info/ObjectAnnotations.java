package org.neo4j.ogm.metadata.info;

import java.util.HashMap;
import java.util.Map;

public class ObjectAnnotations {

    private String objectName;
    private Map<String, AnnotationInfo> annotations = new HashMap<>();

    public String getName() {
        return objectName;
    }

    public void setName(String objectName) {
        this.objectName = objectName;
    }

    public void put(String key, AnnotationInfo value) {
        annotations.put(key, value);
    }

    public AnnotationInfo get(String key) {
        return annotations.get(key);
    }
}

package org.neo4j.ogm.metadata.info;

import java.util.HashMap;
import java.util.Map;

public class AnnotationInfo {

    private String annotationName;
    private Map<String, String> elements = new HashMap<>();

    public String getName() {
        return annotationName;
    }

    public void setName(String annotationName) {
        this.annotationName = annotationName;
    }

    public void put(String key, String value) {
        elements.put(key, value);
    }

    public String get(String key) {
        return elements.get(key);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(annotationName);
        sb.append(": ");
        for (String key : elements.keySet()) {
            sb.append(key);
            sb.append(":'");
            sb.append(get(key));
            sb.append("'");
            sb.append(" ");
        }
        return sb.toString();
    }
}

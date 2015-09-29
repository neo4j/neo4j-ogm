package org.neo4j.ogm.driver;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.neo4j.graphdb.Label;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author vince
 */
public abstract class JsonAdapter {

    protected static final ObjectMapper mapper = new ObjectMapper();

    protected static final String QUOTE = "\"";
    protected static final String OPEN_BRACE = "{";
    protected static final String CLOSE_BRACE = "}";
    protected static final String OPEN_BRACKET = "[";
    protected static final String CLOSE_BRACKET = "]";
    protected static final String COMMA = ",";
    protected static final String COLON = ":";
    protected static final String SPACE = " ";

    protected static void OPEN_OBJECT(StringBuilder sb) {
        sb.append(OPEN_BRACE);
    }

    protected static void OPEN_OBJECT(String name, StringBuilder sb) {
        sb.append(KEY(name));
        sb.append(OPEN_BRACE);
    }

    protected static void CLOSE_OBJECT(StringBuilder sb) {
        sb.append(CLOSE_BRACE);
    }

    protected static void OPEN_ARRAY(String name, StringBuilder sb) {
        sb.append(KEY(name));
        sb.append(OPEN_BRACKET);
    }

    protected static void CLOSE_ARRAY(StringBuilder sb) {
        sb.append(CLOSE_BRACKET);
    }

    protected static final String quoted(String s) {
        return QUOTE.concat(s).concat(QUOTE);
    }

    protected static final String KEY(String k) {
        return quoted(k).concat(COLON).concat(SPACE);
    }

    protected static final String VALUE(Object v) {
        if (v instanceof String || v instanceof Label) {
            return quoted(v.toString());
        } else {
            return v.toString();
        }
    }

    protected static final String KEY_VALUE(String k, Object v) {
        return KEY(k).concat(VALUE(v));
    }

    protected static final String KEY_VALUES(String k, Iterable i) {

        String r = KEY(k).concat(OPEN_BRACKET);
        Iterator iter = i.iterator();

        while (iter.hasNext()) {
            Object v = iter.next();
            String s = VALUE(v);
            r = r.concat(s);
            if (iter.hasNext()) {
                r = r.concat(COMMA);
            }
        }
        r = r.concat(CLOSE_BRACKET);
        return r;
    }

    protected static Iterable<Object> convertToIterable(Object array) {
        List ar = new ArrayList();
        int length = Array.getLength(array);
        for (int i = 0; i < length; i++) {
            ar.add(Array.get(array, i));
        }
        return ar;
    }


}

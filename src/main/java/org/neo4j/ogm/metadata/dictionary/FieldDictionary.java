package org.neo4j.ogm.metadata.dictionary;

import java.lang.reflect.Field;

public interface FieldDictionary {

    Field findField(String property, Object parameter, Object instance) throws Exception;

}

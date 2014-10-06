package org.neo4j.ogm.metadata;

import org.graphaware.graphmodel.neo4j.Property;
import org.junit.Test;
import org.neo4j.ogm.metadata.dictionary.DefaultPersistentFieldDictionary;
import org.neo4j.ogm.metadata.dictionary.PersistentFieldDictionary;

import java.util.Arrays;

import static org.junit.Assert.*;

public class SimplePersistentFieldDictionaryTest {

    @Test
    public void shouldReturnNullForPropertiesThatDoNotExist() {
        Property<String, Object> propertyNotOnClass = new Property("favouriteColour", "Orange");

        PersistentFieldDictionary dictionary = createPersonPersistentFieldDictionary();
        PersistentField persistentField = dictionary.lookUpPersistentFieldForProperty(propertyNotOnClass.getKey());
        assertNull(persistentField);
        //TODO: fix to use null object pattern when we have property migrated EntityAccess over to use PersistentFields
//        Setter.forProperty(persistentField).set(new Person(), propertyNotOnClass.getValue());
    }

    @Test
    public void shouldFindPropertyMapperInformationForGraphProperty() {
        Property<String, Object> arbitraryNodeProperty = new Property("age", Integer.valueOf(42));

        PersistentFieldDictionary dictionary = createPersonPersistentFieldDictionary();

        RegularPersistentField field = (RegularPersistentField) dictionary.lookUpPersistentFieldForProperty(arbitraryNodeProperty.getKey());
        assertNotNull(field);
        assertEquals(arbitraryNodeProperty.getKey(), field.getJavaObjectFieldName());
    }

    private static PersistentFieldDictionary createPersonPersistentFieldDictionary() {
        return new DefaultPersistentFieldDictionary(Arrays.asList(new RegularPersistentField("name"), new RegularPersistentField("age")));
    }

}

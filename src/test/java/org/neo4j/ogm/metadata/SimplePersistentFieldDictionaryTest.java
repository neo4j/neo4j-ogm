package org.neo4j.ogm.metadata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Arrays;

import org.graphaware.graphmodel.Property;
import org.graphaware.graphmodel.impl.StringProperty;
import org.junit.Test;

public class SimplePersistentFieldDictionaryTest {

    @Test
    public void shouldReturnNullForPropertiesThatDoNotExist() {
        Property<String, Object> propertyNotOnClass = new StringProperty("favouriteColour", "Orange");

        PersistentFieldDictionary dictionary = createPersonPersistentFieldDictionary();
        PersistentField persistentField = dictionary.lookUpPersistentFieldForProperty(propertyNotOnClass);
        assertNull(persistentField);
        //TODO: fix to use null object pattern when we have property migrated EntityAccess over to use PersistentFields
//        Setter.forProperty(persistentField).set(new Person(), propertyNotOnClass.getValue());
    }

    @Test
    public void shouldFindPropertyMapperInformationForGraphProperty() {
        Property<String, Object> arbitraryNodeProperty = new StringProperty("age", Integer.valueOf(42));

        PersistentFieldDictionary dictionary = createPersonPersistentFieldDictionary();

        RegularPersistentField field = (RegularPersistentField) dictionary.lookUpPersistentFieldForProperty(arbitraryNodeProperty);
        assertNotNull(field);
        assertEquals(arbitraryNodeProperty.getKey(), field.getJavaObjectFieldName());
    }

    private static PersistentFieldDictionary createPersonPersistentFieldDictionary() {
        return new SimplePersistentFieldDictionary(Arrays.asList(new RegularPersistentField("name"), new RegularPersistentField("age")));
    }

}

package org.neo4j.ogm.metadata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import org.graphaware.graphmodel.Property;
import org.graphaware.graphmodel.impl.StringProperty;
import org.junit.Test;
import org.neo4j.ogm.testmodel.Person;

public class MappingMetadataTest {

    @Test
    public void shouldFollowNullObjectPatternForPropertiesThatDoNotExist() {
        Property<String, Object> propertyNotOnClass = new StringProperty("favouriteColour", "Orange");

        MappingMetadata mappingMetadata = personMappingMetadata();
        PropertyMapping propertyMapping = mappingMetadata.getPropertyMapping(propertyNotOnClass);
        assertNotNull(propertyMapping);
        propertyMapping.writeToObject(new Person());
    }

    @Test
    public void shouldFindPropertyMappingInformationForGraphProperty() {
        Property<String, Object> arbitraryNodeProperty = new StringProperty("age", Integer.valueOf(42));

        MappingMetadata mappingMetadata = personMappingMetadata();

        PropertyMapping propertyMapping = mappingMetadata.getPropertyMapping(arbitraryNodeProperty);
        assertNotNull(propertyMapping);
        assertEquals(arbitraryNodeProperty.getKey(), propertyMapping.getPropertyName());
    }

    private MappingMetadata personMappingMetadata() {
        Map<String, PersistentField> persistentFields = new HashMap<>();
        persistentFields.put("name", new RegularPersistentField("name"));
        persistentFields.put("age", new RegularPersistentField("age"));
        return new MappingMetadata(Person.class, persistentFields);
    }

}

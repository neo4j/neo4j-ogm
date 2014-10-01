package org.neo4j.ogm.metadata;

import org.junit.Test;
import org.neo4j.ogm.entityaccess.SetterEntityAccess;
import org.neo4j.ogm.mapper.domain.social.Person;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

public class SetterEntityAccessTest {

    /**
     * The throwing of Exceptions here is by design. We expect that
     * the {@link PersistentField} objects will contain the definitive mappings
     * of property names to and from node & edge properties. In the event that
     * this mapping is wrong, we should throw an Exception rather than
     * consuming the error and continuing, because an incorrect map is
     * a bug.
     *
     * @throws Exception
     */
    @Test(expected = NullPointerException.class)
    public void shouldThrowNPEWhenMappingToNullPropertyName() throws Exception {
        Person peter = new Person();
        SetterEntityAccess.forProperty(null).set(peter, "Peter");
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowNPEWhenMappingToEmptyPropertyName() throws Exception {
        Person peter = new Person();
        SetterEntityAccess.forProperty("").set(peter, "Peter");
    }

    @Test(expected = NoSuchMethodException.class)
    public void shouldThrowNSMEWhenMappingToNonExistentPropertyName() throws Exception {
        Person peter = new Person();
        SetterEntityAccess.forProperty("height").set(peter, 183);
    }

    @Test
    public void shouldWriteAllValidPropertyValues() throws Exception {

        Person peter = new Person();
        Person paul = new Person();
        Person mary = new Person();
        List<Person> friends = new ArrayList<>();

        friends.add(paul);
        friends.add(mary);

        SetterEntityAccess.forProperty("name").set(peter, "Peter");
        SetterEntityAccess.forProperty("age").set(peter, 34);
        SetterEntityAccess.forProperty("friends").set(peter, friends);
        SetterEntityAccess.forProperty("primitiveIntArray").set(peter, new int[] { 0, 1, 2, 3, 4, 5 });

        assertEquals("Peter", peter.getName());
        assertEquals(34, peter.getAge());
        assertTrue(peter.getFriends().contains(mary));
        assertTrue(peter.getFriends().contains(paul));

        for (int i = 0; i < 6; i++) {
            assertEquals(i, peter.getPrimitiveIntArray()[i]);
        }
    }

}

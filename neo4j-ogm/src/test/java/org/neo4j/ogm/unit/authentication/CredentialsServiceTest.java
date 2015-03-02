package org.neo4j.ogm.unit.authentication;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.authentication.CredentialsService;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;

public class CredentialsServiceTest {

    @Before
    public void setUp() {
        System.setProperty("username", "neo4j");
        System.setProperty("password", "password");
    }

    @After
    public void tearDown() {
        System.getProperties().remove("username");
        System.getProperties().remove("password");
    }

    @Test
    public void testUserNameAndPassword() {
        assertEquals("bmVvNGo6cGFzc3dvcmQ=", CredentialsService.userNameAndPassword().credentials());

    }


    @Test
    public void testUserNameNoPassword() {

        System.getProperties().remove("password");
        assertNull(CredentialsService.userNameAndPassword());

    }


    @Test
    public void testNoUserNamePassword() {

        System.getProperties().remove("username");
        assertNull(CredentialsService.userNameAndPassword());

    }

}

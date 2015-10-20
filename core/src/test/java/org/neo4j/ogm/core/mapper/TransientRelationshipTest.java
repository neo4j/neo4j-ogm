package org.neo4j.ogm.core.mapper;

import org.junit.Assert;
import org.junit.Test;
import org.neo4j.ogm.mapper.TransientRelationship;

import java.util.HashMap;
import java.util.Map;

/**
 * @author nils.droege@c3.co
 * @since 2015-07-23
 */
public class TransientRelationshipTest
{
    @Test
    public void convertShouldNotThowNullpointerException()
    {
        TransientRelationship tr = new TransientRelationship("123", "_1", "", "456", null, null);
        Map<String, Long> refMap = new HashMap<>();

        try {
            tr.convert(refMap);
        } catch (NullPointerException ex) {
            Assert.fail("NullPointerException is unexpected");
        } catch (RuntimeException re) {
        }
    }
}

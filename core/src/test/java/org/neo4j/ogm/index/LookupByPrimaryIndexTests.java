package org.neo4j.ogm.index;

import org.junit.Test;

/**
 * Created by markangrish on 17/11/2016.
 */
public class LookupByPrimaryIndexTests {

    @Test
    public void loadUsesPrimaryIndexWhenPresent() {

    }

    @Test
    public void loadUsesGraphIdWhenPrimaryIndexNotPresent() {

    }

    @Test
    public void exceptionRaisedWhenLookupIsDoneWithGraphIdAndThereIsAPrimaryIndexPresent() {

    }

    /**
     * This test makes sure that if the primary key is a Long, it isn't mixed up with the Graph Id.
     */
    @Test
    public void loadUsesPrimaryIndexWhenPresentEvenIfTypeIsLong() {

    }
}

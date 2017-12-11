/*
 * Copyright (c) 2002-2018 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 *  conditions of the subcomponent's license, as noted in the LICENSE file.
 */

package org.neo4j.ogm.persistence.examples.locking;

import org.junit.Test;
import org.neo4j.ogm.domain.lockinginvalid.MultipleVersionFields;
import org.neo4j.ogm.exception.core.MetadataException;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.MultiDriverTestClass;

/**
 * @author Frantisek Hartman
 */
public class InvalidOptimisticLockingTest extends MultiDriverTestClass {

    @Test(expected = MetadataException.class)
    public void multipleVersionFields() {

        SessionFactory sf = new SessionFactory(driver, MultipleVersionFields.class.getName().toString());
        Session session = sf.openSession();

        session.save(new MultipleVersionFields());
    }
}

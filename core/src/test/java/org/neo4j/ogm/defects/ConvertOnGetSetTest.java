/*
 * Copyright (c) 2002-2016 "Neo Technology,"
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

package org.neo4j.ogm.defects;


import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.domain.convertible.date.DateNumericStringConverter;
import org.neo4j.ogm.domain.convertible.date.Memo;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.GraphTestUtils;
import org.neo4j.ogm.testutil.MultiDriverTestClass;
import org.neo4j.ogm.typeconversion.DateStringConverter;

import java.io.IOException;
import java.util.Date;

public class ConvertOnGetSetTest extends MultiDriverTestClass {

    private static Session session;

    @BeforeClass
    public static void init() throws IOException {
        session = new SessionFactory("org.neo4j.ogm.domain.convertible").openSession();
    }

    @After
    public void teardown() {
        session.purgeDatabase();
    }


    @Test
    public void shouldAllowConverterOnSetter() {

        Date date = new Date(0);
        DateNumericStringConverter converter = new DateNumericStringConverter();
        String expected = converter.toGraphProperty(date);

        DateStringConverter dateStringConverter = new DateStringConverter("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        String actual = dateStringConverter.toGraphProperty(date);

        Memo memo = new Memo();
        memo.setMemo("theMemo");
        memo.setApproved(date);
        session.save(memo);

        //This is what we actually have
        GraphTestUtils.assertSameGraph(getGraphDatabaseService(),
                String.format("CREATE (n:`Memo` {memo: 'theMemo', approved: '%s'})", actual));

        //This is what we want
        GraphTestUtils.assertSameGraph(getGraphDatabaseService(),
                String.format("CREATE (n:`Memo` {memo: 'theMemo', approved: '%s'})", expected));

    }

}

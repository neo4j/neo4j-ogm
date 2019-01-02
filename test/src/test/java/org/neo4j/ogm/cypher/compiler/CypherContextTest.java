/*
 * Copyright (c) 2002-2019 "Neo Technology,"
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
package org.neo4j.ogm.cypher.compiler;

import static java.util.stream.Collectors.*;
import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.neo4j.ogm.cypher.ComparisonOperator;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.domain.gh576.DataItem;
import org.neo4j.ogm.domain.gh576.FormulaItem;
import org.neo4j.ogm.domain.gh576.Variable;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.MultiDriverTestClass;
import org.neo4j.ogm.testutil.TestUtils;

/**
 * @author Andreas Berger
 * @author Michael J. Simons
 */
@RunWith(Parameterized.class)
public class CypherContextTest extends MultiDriverTestClass {

    private static SessionFactory sessionFactory;

    private static String createTestDataStatement = TestUtils.readCQLFile("org/neo4j/ogm/cql/nodes.cql").toString();

    private Session session;

    @Parameterized.Parameters
    public static List<Integer> data() {
        return IntStream.range(0, 10)
            .boxed().collect(toList());
    }

    @BeforeClass
    public static void initSesssionFactory() {
        sessionFactory = new SessionFactory(driver, "org.neo4j.ogm.domain.gh576");
    }

    public CypherContextTest(@SuppressWarnings("unused") Integer iterations) {
    }

    @Before
    public void init() throws IOException {
        session = sessionFactory.openSession();
        session.purgeDatabase();
        session.clear();

        session.query(createTestDataStatement, Collections.emptyMap());
    }

    @Test // GH-576
    public void shouldDeregisterRelationshipEntities() {
        Collection<DataItem> dataItems;
        FormulaItem formulaItem;

        Filter filter = new Filter("nodeId", ComparisonOperator.EQUALS, "m1");

        dataItems = session.loadAll(DataItem.class, filter);
        assertThat(dataItems).hasSize(1);

        formulaItem = (FormulaItem) dataItems.iterator().next();
        assertThat(formulaItem.getVariables()).hasSize(3);

        Predicate<Variable> isVariableAWithDataItemM2 = v -> v.getVariable().equals("A") && v.getDataItem().getNodeId()
            .equals("m2");
        formulaItem.getVariables().removeIf(isVariableAWithDataItemM2);
        assertThat(formulaItem.getVariables()).hasSize(2);

        session.save(formulaItem);

        dataItems = session.loadAll(DataItem.class, filter);
        assertThat(dataItems).hasSize(1);

        formulaItem = (FormulaItem) dataItems.iterator().next();
        assertThat(formulaItem.getVariables()).hasSize(2);
    }

    @After
    public void tearDown() {
        session.purgeDatabase();
    }
}

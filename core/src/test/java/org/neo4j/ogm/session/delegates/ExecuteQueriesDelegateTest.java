/*
 * Copyright (c) 2002-2022 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neo4j.ogm.session.delegates;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * Test for new regular expression to determine write queries
 *
 * @author Torsten Kuhnhenne
 * @author Michael J. Simons
 */
@RunWith(Parameterized.class)
public class ExecuteQueriesDelegateTest {

    @Parameterized.Parameters
    public static Collection<Object[]> parameters() {
        return Arrays.asList(new Object[][] {
            { "CREATE (a:Actor) RETURN a", true },
            { "create (a:Actor) return a", true },
            // CREATE
            { "CREATE (a:Actor) RETURN a", true }, { "create (a:Actor) return a", true },
            // MERGE
            { "MERGE (a:Actor {id : 10}) ON CREATE SET a.created = timestamp() ON MATCH SET a.accessTime = timestamp()",
                true },
            { "merge (a:Actor {id : 10}) ON CREATE SET a.created = timestamp() ON MATCH SET a.accessTime = timestamp()",
                true },
            // SET
            { "MATCH (a:Actor) SET a.age = 45", true },
            { "match (a:Actor) set a.age = 45", true },
            // DELETE
            { "MATCH (a:Actor) DELETE a", true },
            { "match (a:Actor) delete a", true },
            { "MATCH (a:Actor) DETACH DELETE a", true },
            { "match (a:Actor) detach delete a", true },
            // REMOVE
            { "MATCH (a:Actor) REMOVE a.age", true }, { "match (a:Actor) remove a.age", true },
            // DROP
            { "DROP USER test", true },
            // CALL
            { "call sp.doSomething()", true },
            { "CALL sp.doSomething()", true },
            { "MATCH (a:Actor) WITH a CALL sp.doSomething(a)", true },
            // CALL with misspelled OGM READ_ONLY hint
            { "MATCH (a:Actor) WITH a CALL /*+ OGM_READ_ONLY */ sp.doSomething(a)", true },
            { "MATCH (a:Actor) WITH a CALL /*+ OGM_READ_ONLY */ sp.doSomething(a)", true },
            { "MATCH (a:Actor) WITH a CALL /*+ OGM READ ONLY */ sp.doSomething(a)", true },
            { "MATCH (a:Actor) WITH a CALL /*+ OGM READ _ONLY */ sp.doSomething(a)", true },
            { "MATCH (a:Actor) WITH a CALL /*+OGM READ_ONLY */ sp.doSomething(a)", true },
            { "MATCH (a:Actor) WITH a CALL /*+ OGM READ_ONLY*/ sp.doSomething(a)", true },
            { "MATCH (a:Actor) WITH a CALL /*+OGM READ_ONLY*/ sp.doSomething(a)", true },
            { "call sp.doSomething() yield x \nWITH x MATCH (f:Foo) \nWHERE f.x = x RETURN f ", true },
            // Simple match
            { "MATCH (a:Actor) RETURN a", false }, { "match (a:Actor) return a", false },
            // CALL with OGM READ_ONLY-hint
            { "call /*+ OGM READ_ONLY */ sp.doSomething()", false },
            { "/*+ OGM READ_ONLY */ call sp.doSomething()", false },
            { "call sp.doSomething() /*+ OGM READ_ONLY */", false }, {
            "MATCH (a:Actor) WITH a CALL /*+ OGM READ_ONLY */ sp.doSomething(a)", false },
            { "call sp.doSomething() /*+ OGM READ_ONLY */ yield x \nWITH x MATCH (f:Foo) \nWHERE f.x = x RETURN f ",
                false },
            { "call sp.doSomething() yield x \nWITH x MATCH (f:Foo) \nWHERE f.x = x RETURN f /*+ OGM READ_ONLY */",
                false },
        });
    }

    private String query;

    private boolean isWriteQuery;

    public ExecuteQueriesDelegateTest(String query, boolean isWriteQuery) {
        this.query = query;
        this.isWriteQuery = isWriteQuery;
    }

    @Test
    public void test() {
        boolean mayBeReadWrite = ExecuteQueriesDelegate.mayBeReadWrite(query);
        if (isWriteQuery) {
            Assert.assertTrue(mayBeReadWrite);
        } else {
            Assert.assertFalse(mayBeReadWrite);
        }
    }
}

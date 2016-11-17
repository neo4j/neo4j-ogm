package org.neo4j.ogm.index;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.MetaData;
import org.neo4j.ogm.compiler.CompileContext;
import org.neo4j.ogm.compiler.Compiler;
import org.neo4j.ogm.context.EntityGraphMapper;
import org.neo4j.ogm.context.EntityMapper;
import org.neo4j.ogm.context.MappingContext;
import org.neo4j.ogm.domain.cineasts.annotated.User;
import org.neo4j.ogm.domain.pizza.Pizza;
import org.neo4j.ogm.index.domain.BadClass;
import org.neo4j.ogm.session.Neo4jException;
import org.neo4j.ogm.session.request.RowStatementFactory;

/**
 * @author Mark Angrish
 */
public class MergeWithPrimaryIndexTests {

    private static MetaData mappingMetadata;
    private static MappingContext mappingContext;
    private EntityMapper mapper;

    @BeforeClass
    public static void setUpTestDatabase() {
        mappingMetadata = new MetaData("org.neo4j.ogm.index.domain", "org.neo4j.ogm.domain.cineasts.annotated", "org.neo4j.ogm.domain.pizza");
        mappingContext = new MappingContext(mappingMetadata);
    }

    @Before
    public void setUpMapper() {
        mappingContext = new MappingContext(mappingMetadata);
        this.mapper = new EntityGraphMapper(mappingMetadata, mappingContext);
    }

    @After
    public void cleanGraph() {
        mappingContext.clear();
    }

    @Test
    public void newNodeUsesGraphIdWhenPrimaryIndexNotPresent() {
        Pizza pizza = new Pizza("Plain");
        assertNull(pizza.getId());
        Compiler compiler = mapAndCompile(pizza);
        assertFalse(compiler.hasStatementsDependentOnNewNodes());
        assertEquals("UNWIND {rows} as row CREATE (n:`Pizza`) SET n=row.props RETURN row.nodeRef as ref, ID(n) as id, row.type as type",
                compiler.createNodesStatements().get(0).getStatement());
    }

    @Test
    public void newNodeUsesPrimaryIndexWhenPresent() {
        User newUser = new User("bachmania", "Michal Bachman", "password");
        assertNull(newUser.getId());
        Compiler compiler = mapAndCompile(newUser);
        assertFalse(compiler.hasStatementsDependentOnNewNodes());
        assertEquals("UNWIND {rows} as row MERGE (n:`User`{login: row.props.login}) SET n=row.props RETURN row.nodeRef as ref, ID(n) as id, row.type as type",
                compiler.createNodesStatements().get(0).getStatement());
    }

    @Test(expected = Neo4jException.class)
    public void exceptionRaisedWhenMoreThanOnePrimaryIndexDefinedInSameClass() {
        BadClass invoice = new BadClass(223L, "Company", 100000L);
        assertNull(invoice.getId());
        mapAndCompile(invoice);
    }


    private Compiler mapAndCompile(Object object) {
        CompileContext context = this.mapper.map(object);
        Compiler compiler = context.getCompiler();
        compiler.useStatementFactory(new RowStatementFactory());
        return compiler;
    }
}

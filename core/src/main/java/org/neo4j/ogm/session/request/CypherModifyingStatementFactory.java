package org.neo4j.ogm.session.request;

import java.util.Map;

import org.neo4j.ogm.request.OptimisticLockingConfig;
import org.neo4j.ogm.request.Statement;
import org.neo4j.ogm.request.StatementFactory;

/**
 * Statement factory that is used to modify cypher statements before they are sent to the cypher compiler.
 *
 * @author Michael J. Simons
 */
class CypherModifyingStatementFactory implements StatementFactory {
    private final RowStatementFactory delegate;

    public CypherModifyingStatementFactory(RowStatementFactory delegate) {
        this.delegate = delegate;
    }

    @Override
    public Statement statement(String statement, Map<String, Object> parameters) {
        return delegate.statement(applyExtensions(statement), parameters);
    }

    @Override
    public RowDataStatement statement(String statement, Map<String, Object> parameters,
        OptimisticLockingConfig config) {
        return delegate.statement(applyExtensions(statement), parameters, config);
    }

    String applyExtensions(String statement) {
        return "schwupp";
    }
}

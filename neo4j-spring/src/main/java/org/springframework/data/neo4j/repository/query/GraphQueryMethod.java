package org.springframework.data.neo4j.repository.query;

import org.neo4j.ogm.session.Session;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.repository.core.NamedQueries;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.EvaluationContextProvider;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;

/**
 * Created by markangrish on 13/01/2015.
 */
public class GraphQueryMethod extends QueryMethod
{
    private final Session session;
    private final Method method;
    private final Query queryAnnotation;

    public GraphQueryMethod(Method method, RepositoryMetadata metadata, Session session)
    {
        super(method, metadata);
        this.method = method;
        this.session = session;
        this.queryAnnotation = method.getAnnotation(Query.class);
    }

    public String getQueryString() {
        return queryAnnotation.value();
    }

    public Class<?> getReturnType() {
        return method.getReturnType();
    }

    @Override
    public String getNamedQueryName() {
        throw new UnsupportedOperationException("OGM does not currently support named queries.");
    }

    public RepositoryQuery createQuery()
    {
        return new GraphRepositoryQuery(this, session);
    }
}

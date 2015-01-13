package org.springframework.data.neo4j.repository.query;

import org.springframework.data.repository.query.QueryMethod;
import org.springframework.data.repository.query.RepositoryQuery;

/**
 * Created by markangrish on 13/01/2015.
 */
public class CypherRepositoryQuery implements RepositoryQuery
{
    @Override
    public Object execute(Object[] objects)
    {
        return null;
    }

    @Override
    public QueryMethod getQueryMethod()
    {
        return null;
    }
}

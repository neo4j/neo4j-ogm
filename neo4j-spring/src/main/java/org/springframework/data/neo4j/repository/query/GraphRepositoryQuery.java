package org.springframework.data.neo4j.repository.query;

import org.neo4j.ogm.session.Session;
import org.springframework.data.repository.query.RepositoryQuery;

import java.util.Collection;
import java.util.HashMap;

/**
 * Created by markangrish on 13/01/2015.
 */
public class GraphRepositoryQuery implements RepositoryQuery
{
    private final GraphQueryMethod graphQueryMethod;

    private final Session session;

    public GraphRepositoryQuery(GraphQueryMethod graphQueryMethod, Session session)
    {
        this.graphQueryMethod = graphQueryMethod;
        this.session = session;
    }

    @Override
    public Object execute(Object[] parameters)
    {
        if (graphQueryMethod.getReturnType().equals(Void.class))
        {
            session.execute(graphQueryMethod.getQueryString(),  new HashMap<String, Object>());
            return null;
        }
        else if (Collection.class.isAssignableFrom(graphQueryMethod.getReturnType()))
        {
            return session.query(graphQueryMethod.getReturnType(), graphQueryMethod.getQueryString(),  new HashMap<String, Object>());
        }
        else
        {
            return session.queryForObject(graphQueryMethod.getReturnType(), graphQueryMethod.getQueryString(), new HashMap<String, Object>());
        }
    }

    @Override
    public GraphQueryMethod getQueryMethod()
    {
        return graphQueryMethod;
    }
}

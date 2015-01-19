package org.springframework.data.neo4j.repository.query;

import org.neo4j.ogm.session.Session;
import org.springframework.data.repository.query.RepositoryQuery;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

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
        Class<?> returnType = graphQueryMethod.getMethod().getReturnType();
        Class<?> concreteType = resolveConcreteType(graphQueryMethod.getMethod().getReturnType(),
                                                    graphQueryMethod.getMethod().getGenericReturnType());

        if (returnType.equals(Void.class))
        {
            session.execute(graphQueryMethod.getQueryString(), new HashMap<String, Object>());
            return null;
        }
        else if (Iterable.class.isAssignableFrom(returnType))
        {
            // Special method to handle SDN Iterable<Map<String, Object>> behaviour.
            // TODO: Do we really want this method in an OGM? It's a little too low level and/or doesn't really fit.
            if (Map.class.isAssignableFrom(concreteType)) {
                return session.query(graphQueryMethod.getQueryString(), new HashMap<String, Object>());
            }
            return session.query(concreteType, graphQueryMethod.getQueryString(), new HashMap<String, Object>());
        }
        else
        {
            return session.queryForObject(returnType, graphQueryMethod.getQueryString(), new HashMap<String, Object>());
        }
    }

    public static Class<?> resolveConcreteType(Class<?> type, final Type genericType)
    {
        if (Iterable.class.isAssignableFrom(type))
        {
            if (genericType instanceof ParameterizedType)
            {
                ParameterizedType returnType = (ParameterizedType) genericType;
                Type componentType = returnType.getActualTypeArguments()[0];

                return componentType instanceof ParameterizedType ?
                               (Class<?>) ((ParameterizedType) componentType).getRawType() :
                               (Class<?>) componentType;
            }
            else
            {
                return Object.class;
            }
        }

        return type;
    }

    @Override
    public GraphQueryMethod getQueryMethod()
    {
        return graphQueryMethod;
    }
}

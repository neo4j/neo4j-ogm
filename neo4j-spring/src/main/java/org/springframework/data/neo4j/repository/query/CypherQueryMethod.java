package org.springframework.data.neo4j.repository.query;

import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.QueryMethod;

import java.lang.reflect.Method;

/**
 * Created by markangrish on 13/01/2015.
 */
public class CypherQueryMethod extends QueryMethod
{
    public CypherQueryMethod(Method method, RepositoryMetadata metadata)
    {
        super(method, metadata);
    }
}

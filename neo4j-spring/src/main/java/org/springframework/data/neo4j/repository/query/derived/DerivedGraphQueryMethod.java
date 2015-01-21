package org.springframework.data.neo4j.repository.query.derived;

import org.neo4j.ogm.session.Session;
import org.springframework.data.neo4j.repository.query.GraphQueryMethod;
import org.springframework.data.repository.core.EntityMetadata;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.parser.PartTree;

import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by markangrish on 20/01/2015.
 */
public class DerivedGraphQueryMethod extends GraphQueryMethod
{
    private static final Pattern PREFIX_TEMPLATE = Pattern.compile("^(find|read|get|query)((\\p{Lu}.*?))??By");


    private String query;

    public DerivedGraphQueryMethod(Method method, RepositoryMetadata metadata, Session session)
    {
        super(method, metadata, session);

        this.query = buildQuery(method, metadata.getDomainType());
    }

    // FIXME: The hackiest thing i could do to get something working.
    private String buildQuery(Method method, Class<?> type)
    {
        String methodName = method.getName();
        Matcher matcher = PREFIX_TEMPLATE.matcher(methodName);
        if(!matcher.find())
        {
            throw new RuntimeException("Could not derive query for method: " + methodName + ". Check spelling or use @Query.");
        }

        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("MATCH (o:");
        queryBuilder.append(type.getSimpleName());
        queryBuilder.append(") WHERE ");
        String predicates = methodName.substring(matcher.group().length());
        queryBuilder.append("o.");
        queryBuilder.append(predicates.toLowerCase());
        queryBuilder.append(" = ");
        queryBuilder.append("{0}");
        queryBuilder.append(" RETURN o");
        return queryBuilder.toString();
    }

    public String getQuery() {
        return query;
    }
}

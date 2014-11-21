package org.neo4j.ogm.session;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.impl.client.CloseableHttpClient;
import org.neo4j.graphmodel.GraphModel;
import org.neo4j.ogm.entityaccess.FieldAccess;
import org.neo4j.ogm.mapper.MappingContext;
import org.neo4j.ogm.mapper.MetaDataDrivenObjectToCypherMapper;
import org.neo4j.ogm.mapper.ObjectGraphMapper;
import org.neo4j.ogm.mapper.cypher.*;
import org.neo4j.ogm.metadata.MappingException;
import org.neo4j.ogm.metadata.MetaData;
import org.neo4j.ogm.metadata.info.ClassInfo;
import org.neo4j.ogm.session.request.DefaultRequestHandler;
import org.neo4j.ogm.session.request.Neo4jRequestHandler;
import org.neo4j.ogm.session.response.GraphModelResponseHandler;
import org.neo4j.ogm.session.response.Neo4jResponseHandler;
import org.neo4j.ogm.session.response.RowModelResponseHandler;
import org.neo4j.ogm.session.result.RowModel;
import org.neo4j.ogm.session.strategy.DepthOneStrategy;
import org.neo4j.ogm.session.strategy.VariableDepthReadStrategy;

import java.lang.reflect.Field;
import java.util.*;

public class DefaultSessionImpl implements Session {

    private final MetaData metaData;
    private final MappingContext mappingContext;
    private final ObjectMapper mapper;

    private final String url;

    private Neo4jRequestHandler<String> requestHandler;

    public DefaultSessionImpl(MetaData metaData, String url, CloseableHttpClient client, ObjectMapper mapper) {

        this.metaData = metaData;
        this.mappingContext = new MappingContext();
        this.mapper = mapper;
        this.requestHandler = new DefaultRequestHandler(client);

        this.url = transformUrl(url);
    }

    public void setRequestHandler(Neo4jRequestHandler<String> requestHandler) {
        this.requestHandler = requestHandler;
    }

    private Neo4jResponseHandler<GraphModel> executeGraphModelQuery(GraphModelQuery query) {
        List<ParameterisedStatement> list = new ArrayList<>();
        list.add(query);
        try {
            String json = mapper.writeValueAsString(new ParameterisedStatements(list));
            Neo4jResponseHandler<String> responseHandler = requestHandler.execute(url, json);
            return new GraphModelResponseHandler(responseHandler, mapper);
        } catch (JsonProcessingException jpe) {
            throw new MappingException(jpe.getLocalizedMessage());
        }
    }

    private Neo4jResponseHandler<RowModel> executeRowModelQuery(RowModelQuery query) {
        List<ParameterisedStatement> list = new ArrayList<>();
        list.add(query);
        try {
            String json = mapper.writeValueAsString(new ParameterisedStatements(list));
            Neo4jResponseHandler<String> responseHandler = requestHandler.execute(url, json);
            return new RowModelResponseHandler(responseHandler, mapper);
        } catch (JsonProcessingException jpe) {
            throw new MappingException(jpe.getLocalizedMessage());
        }
    }

    private Neo4jResponseHandler<String> executeStatement(ParameterisedStatement statement) {
        List<ParameterisedStatement> list = new ArrayList<>();
        list.add(statement);
        try {
            String json = mapper.writeValueAsString(new ParameterisedStatements(list));
            return requestHandler.execute(url, json);
        } catch (JsonProcessingException jpe) {
            throw new MappingException(jpe.getLocalizedMessage());
        }
    }

    @Override
    public <T> T load(Class<T> type, Long id) {
        return load(type, id, 1);
    }

    @Override
    public <T> T load(Class<T> type, Long id, int depth) {
        return loadOne(type, executeGraphModelQuery(new VariableDepthReadStrategy().findOne(id, depth)));
    }

    @Override
    public <T> Collection<T> loadAll(Class<T> type, Collection<Long> ids) {
        return loadAll(type, ids, 1);
    }

    @Override
    public <T> Collection<T> loadAll(Class<T> type, Collection<Long> ids, int depth) {
        return loadAll(type, executeGraphModelQuery(new VariableDepthReadStrategy().findAll(ids, depth)));
    }

    @Override
    public <T> Collection<T> loadAll(Class<T> type) {
        return loadAll(type, 1);
    }

    @Override
    public <T> Collection<T> loadAll(Class<T> type, int depth) {
        ClassInfo classInfo = metaData.classInfo(type.getName());
        return loadAll(type, executeGraphModelQuery(new VariableDepthReadStrategy().findByLabel(classInfo.label(), depth)));
    }

    @Override
    public <T> Collection<T> loadAll(Collection<T> objects) {
        return loadAll(objects, 1);
    }

    @Override
    public <T> Collection<T> loadAll(Collection<T> objects, int depth) {

        if (objects == null || objects.isEmpty()) {
            return objects;
        }

        Set<Long> ids = new HashSet<>();
        Class type = objects.iterator().next().getClass();
        ClassInfo classInfo = metaData.classInfo(type.getName());
        Field identityField = classInfo.getField(classInfo.identityField());
        for (Object o: objects) {
            ids.add((Long) FieldAccess.read(identityField, o));
        }
        return loadAll(type, ids, depth);
    }

    @Override
    public <T> void deleteAll(Class<T> type) {
        ClassInfo classInfo = metaData.classInfo(type.getName());
        executeStatement(new DepthOneStrategy().deleteByLabel(classInfo.label())).close();

    }

    @Override
    public void execute(String statement) {
        ParameterisedStatement parameterisedStatement = new ParameterisedStatement(statement, Utils.map());
        executeStatement(parameterisedStatement).close();
    }

    @Override
    public void purge() {
        executeStatement(new DepthOneStrategy().purge()).close();
    }

    @Override
    public <T> void save(T object) {
        save(object, -1); // default : full tree of changed objects
    }

    @Override
    public <T> void save(T object, int depth) {

        CypherContext context = new MetaDataDrivenObjectToCypherMapper(metaData, null).mapToCypher(object, depth);

        try {
            List<ParameterisedStatement> statements = context.getStatements();
            String json = mapper.writeValueAsString(new ParameterisedStatements(statements));
            RowModelResponseHandler responseHandler = new RowModelResponseHandler(requestHandler.execute(url, json), mapper);
            String[] variables = responseHandler.columns();
            RowModel rowModel;
            while ((rowModel = responseHandler.next()) != null) {
                Object[] results = rowModel.getValues();
                for (int i = 0; i < variables.length; i++) {
                    String variable = variables[i];
                    Object persisted = context.getNewObject(variable);
                    Long identity = Long.parseLong(results[i].toString());
                    ClassInfo classInfo = metaData.classInfo(persisted.getClass().getName());
                    Field identityField = classInfo.getField(classInfo.identityField());
                    FieldAccess.write(identityField, persisted, identity);
                }
            }
            responseHandler.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private <T> T loadOne(Class<T> type, Neo4jResponseHandler<GraphModel> stream) {
        GraphModel graphModel = stream.next();
        if (graphModel != null) {
            ObjectGraphMapper ogm = new ObjectGraphMapper(metaData, mappingContext);
            stream.close();
            return ogm.load(type, graphModel);
        }
        return null;
    }

    private <T> Collection<T> loadAll(Class<T> type, Neo4jResponseHandler<GraphModel> stream) {
        Set<T> objects = new HashSet<>();
        ObjectGraphMapper ogm = new ObjectGraphMapper(metaData, mappingContext);
        GraphModel graphModel;
        while ((graphModel = stream.next()) != null) {
            objects.addAll(ogm.loadAll(type, graphModel));
        }
        stream.close();
        return objects;
    }

    private String transformUrl(String url) {
        if (url == null) {
            return url;
        }

        if (!url.endsWith("/")) {
            url = url + "/";
        }

        return url + "db/data/transaction/commit";
    }

}

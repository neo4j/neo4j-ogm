package org.neo4j.ogm.session;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.impl.client.CloseableHttpClient;
import org.neo4j.graphmodel.GraphModel;
import org.neo4j.graphmodel.Property;
import org.neo4j.ogm.entityaccess.FieldAccess;
import org.neo4j.ogm.mapper.MappingContext;
import org.neo4j.ogm.mapper.ObjectGraphMapper;
import org.neo4j.ogm.mapper.cypher.GraphModelQuery;
import org.neo4j.ogm.mapper.cypher.ParameterisedStatement;
import org.neo4j.ogm.mapper.cypher.ParameterisedStatements;
import org.neo4j.ogm.mapper.cypher.RowModelQuery;
import org.neo4j.ogm.metadata.MappingException;
import org.neo4j.ogm.metadata.MetaData;
import org.neo4j.ogm.metadata.info.ClassInfo;
import org.neo4j.ogm.metadata.info.FieldInfo;
import org.neo4j.ogm.session.querystrategy.DepthOneStrategy;
import org.neo4j.ogm.session.request.DefaultRequestHandler;
import org.neo4j.ogm.session.request.Neo4jRequestHandler;
import org.neo4j.ogm.session.response.GraphModelResponseHandler;
import org.neo4j.ogm.session.response.Neo4jResponseHandler;
import org.neo4j.ogm.session.response.RowModelResponseHandler;
import org.neo4j.ogm.session.result.RowModel;

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
        return loadOne(type, executeGraphModelQuery(new DepthOneStrategy().findOne(id)));
    }

    @Override
    public <T> Collection<T> loadAll(Class<T> type, Collection<Long> ids) {
        return loadAll(type, executeGraphModelQuery(new DepthOneStrategy().findAll(ids)));
    }

    @Override
    public <T> Collection<T> loadAll(Class<T> type) {
        ClassInfo classInfo = metaData.classInfo(type.getName());
        return loadAll(type, executeGraphModelQuery(new DepthOneStrategy().findByLabel(classInfo.label())));
    }

    @Override
    public <T> Collection<T> loadAll(Collection<T> objects) {
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
        return loadAll(type, ids);
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

        ClassInfo classInfo = metaData.classInfo(object.getClass().getName());

        // get the object identity (will be null for new objects)
        Field identityField = classInfo.getField(classInfo.identityField());
        Long identity = (Long) FieldAccess.read(identityField, object);

        // collect the node properties
        Collection<FieldInfo> properties = classInfo.propertyFields();
        List<Property<String, Object>> propertyList = new ArrayList<>();
        for (FieldInfo fieldInfo : properties) {
            Field field = classInfo.getField(fieldInfo);
            String key = fieldInfo.property();
            Object value = FieldAccess.read(field, object);
            propertyList.add(new Property(key, value));
        }

        //String command;
        if (identity != null) {
            executeStatement(new DepthOneStrategy().updateProperties(identity, propertyList)).close();
        } else {
            Collection<String> labels = classInfo.labels();
            setIdentity(identityField, object, executeRowModelQuery(new DepthOneStrategy().createNode(propertyList, labels)));
        }
    }

    private <T> void setIdentity(Field identityField, T object, Neo4jResponseHandler<RowModel> response) {
        Long identity = Long.parseLong(response.next().getValues()[0].toString());
        response.close();
        FieldAccess.write(identityField, object, identity);

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
            objects.add(ogm.load(type, graphModel));
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

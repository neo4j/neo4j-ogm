package org.neo4j.ogm.session;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.impl.client.CloseableHttpClient;
import org.graphaware.graphmodel.neo4j.GraphModel;
import org.graphaware.graphmodel.neo4j.Property;
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

    private Neo4jResponseHandler<GraphModel> executeGraphModelQuery(String cypher, Map<String, Object> parameters) {
        List<ParameterisedStatement> list = new ArrayList<>();
        GraphModelQuery query = new GraphModelQuery(cypher, parameters);
        list.add(query);
        try {
            String json = mapper.writeValueAsString(new ParameterisedStatements(list));
            Neo4jResponseHandler<String> responseHandler = requestHandler.execute(url, json);
            return new GraphModelResponseHandler(responseHandler, mapper);
        } catch (JsonProcessingException jpe) {
            throw new MappingException(jpe.getLocalizedMessage());
        }
    }

    private Neo4jResponseHandler<RowModel> executeRowModelQuery(String cypher, Map<String, Object> parameters) {
        List<ParameterisedStatement> list = new ArrayList<>();
        RowModelQuery query = new RowModelQuery(cypher, parameters);
        list.add(query);
        try {
            String json = mapper.writeValueAsString(new ParameterisedStatements(list));
            Neo4jResponseHandler<String> responseHandler = requestHandler.execute(url, json);
            return new RowModelResponseHandler(responseHandler, mapper);
        } catch (JsonProcessingException jpe) {
            throw new MappingException(jpe.getLocalizedMessage());
        }
    }

    private Neo4jResponseHandler<String> executeStatement(String cypher, Map<String, Object> parameters) {
        List<ParameterisedStatement> list = new ArrayList<>();
        list.add(new ParameterisedStatement(cypher, parameters));
        try {
            String json = mapper.writeValueAsString(new ParameterisedStatements(list));
            return requestHandler.execute(url, json);
        } catch (JsonProcessingException jpe) {
            throw new MappingException(jpe.getLocalizedMessage());
        }
    }

    @Override
    public <T> T load(Class<T> type, Long id) {
        String cypher = new DepthOneStrategy().findOne(id);
        // todo: parameterise!
        return loadOne(type, executeGraphModelQuery(cypher, new HashMap<String, Object>()));
    }

    @Override
    public <T> Collection<T> loadAll(Class<T> type, Collection<Long> ids) {
        String cypher = new DepthOneStrategy().findAll(ids);
        // todo: parameterise!
        return loadAll(type, executeGraphModelQuery(cypher, new HashMap<String, Object>()));
    }

    @Override
    public <T> Collection<T> loadAll(Class<T> type) {
        ClassInfo classInfo = metaData.classInfo(type.getName());
        String cypher = new DepthOneStrategy().findByLabel(classInfo.label());
        // todo: parameterise!
        return loadAll(type, executeGraphModelQuery(cypher, new HashMap<String, Object>()));
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
        // todo: parameterise!
        executeRowModelQuery(new DepthOneStrategy().deleteByLabel(classInfo.label()), new HashMap<String, Object>()).close();

    }

    @Override
    public void execute(String statement) {
        executeStatement(statement, new HashMap<String, Object>()).close();
    }

    @Override
    public void purge() {
        executeStatement(new DepthOneStrategy().purge(), new HashMap<String, Object>()).close();
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

        String command;
        if (identity != null) {
            command = new DepthOneStrategy().updateProperties(identity, propertyList);
            // todo: parameterise!
            executeStatement(command, new HashMap<String, Object>()).close();
        } else {
            Collection<String> labels = classInfo.labels();
            command = new DepthOneStrategy().createNode(propertyList, labels);
            // todo: parameterise!
            setIdentity(identityField, object, executeRowModelQuery(command, new HashMap<String, Object>()));
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

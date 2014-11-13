package org.neo4j.ogm.session;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.impl.client.CloseableHttpClient;
import org.graphaware.graphmodel.neo4j.GraphModel;
import org.graphaware.graphmodel.neo4j.Property;
import org.neo4j.ogm.entityaccess.FieldAccess;
import org.neo4j.ogm.mapper.MappingContext;
import org.neo4j.ogm.mapper.ObjectGraphMapper;
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

    @Override
    public <T> T load(Class<T> type, Long id) {
        Neo4jResponseHandler<String> responseHandler = requestHandler.execute(url, new DepthOneStrategy().findOne(id));
        Neo4jResponseHandler<GraphModel> graphModelResponseHandler = new GraphModelResponseHandler(responseHandler, mapper);
        return loadOne(type, graphModelResponseHandler);
    }

    @Override
    public <T> Collection<T> loadAll(Class<T> type, Collection<Long> ids) {
        Neo4jResponseHandler<String> responseHandler = requestHandler.execute(url, new DepthOneStrategy().findAll(ids));
        Neo4jResponseHandler<GraphModel> graphModelResponseHandler = new GraphModelResponseHandler(responseHandler, mapper);
        return loadAll(type, graphModelResponseHandler);
    }

    @Override
    public <T> Collection<T> loadAll(Class<T> type) {
        ClassInfo classInfo = metaData.classInfo(type.getName());
        Neo4jResponseHandler<String> responseHandler = requestHandler.execute(url, new DepthOneStrategy().findByLabel(classInfo.label()));
        Neo4jResponseHandler<GraphModel> graphModelResponseHandler = new GraphModelResponseHandler(responseHandler, mapper);
        return loadAll(type, graphModelResponseHandler);
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
        requestHandler.execute(url, new DepthOneStrategy().deleteByLabel(classInfo.label())).close();

    }

    @Override
    public void execute(String... statements) {
        requestHandler.execute(url, statements).close();
    }

    @Override
    public void purge() {
        requestHandler.execute(url, new DepthOneStrategy().purge()).close();
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
            requestHandler.execute(url, command).close();
        } else {
            Collection<String> labels = classInfo.labels();
            command = new DepthOneStrategy().createNode(propertyList, labels);
            setIdentity(identityField, object, new RowModelResponseHandler(requestHandler.execute(url, command), mapper));
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

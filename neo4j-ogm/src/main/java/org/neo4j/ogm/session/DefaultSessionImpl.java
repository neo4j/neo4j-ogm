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

import java.lang.reflect.Field;
import java.util.*;

public class DefaultSessionImpl implements Session {

    private final MetaData metaData;
    private final MappingContext mappingContext;
    private final String url;

    private Neo4jRequestHandler<GraphModel> requestHandler;
    private Neo4jRequestHandler<RowModel> commandHandler;

    public DefaultSessionImpl(MetaData metaData, String url, CloseableHttpClient client, ObjectMapper mapper) {
        this.metaData = metaData;
        this.mappingContext = new MappingContext();
        this.requestHandler = new GraphModelRequestHandler(client, mapper);
        this.commandHandler = new RowModelRequestHandler(client, mapper);

        this.url = transformUrl(url);
    }

    @Override
    public void setRequestHandler(Neo4jRequestHandler request) {
        this.requestHandler = request;
    }

    @Override
    public <T> T load(Class<T> type, Long id) {
        return loadOne(type, requestHandler.execute(url, new CypherQuery().findOne(id)));
    }

    @Override
    public <T> Collection<T> loadAll(Class<T> type, Collection<Long> ids) {
        return loadAll(type, requestHandler.execute(url, new CypherQuery().findAll(ids)));
    }

    @Override
    public <T> Collection<T> loadAll(Class<T> type) {
        ClassInfo classInfo = metaData.classInfo(type.getName());
        Neo4jResponseHandler<GraphModel> stream = requestHandler.execute(url, new CypherQuery().findByLabel(classInfo.label()));
        return loadAll(type, stream);
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
        requestHandler.execute(url, new CypherQuery().deleteByLabel(classInfo.label()));

    }

    @Override
    public void execute(String... statements) {
        requestHandler.execute(url, statements);
    }

    @Override
    public void purge() {
        requestHandler.execute(url, new CypherQuery().purge());
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
            command = new CypherQuery().updateProperties(identity, propertyList);
            System.out.println(command);
            commandHandler.execute(url, command);
        } else {
            Collection<String> labels = classInfo.labels();
            command = new CypherQuery().createNode(propertyList, labels);
            setIdentity(identityField, object, commandHandler.execute(url, command));
        }
    }

    private <T> void setIdentity(Field identityField, T object, Neo4jResponseHandler<RowModel> response) {
        Long identity = Long.parseLong(response.next().getValues()[0].toString());
        FieldAccess.write(identityField, object, identity);

    }

    private <T> T loadOne(Class<T> type, Neo4jResponseHandler<GraphModel> stream) {
        GraphModel graphModel = stream.next();
        if (graphModel != null) {
            ObjectGraphMapper ogm = new ObjectGraphMapper(metaData, mappingContext);
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

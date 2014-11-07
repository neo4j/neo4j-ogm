package org.neo4j.ogm.session;

import org.apache.http.impl.client.CloseableHttpClient;
import org.graphaware.graphmodel.neo4j.GraphModel;
import org.neo4j.ogm.mapper.MappingContext;
import org.neo4j.ogm.mapper.ObjectGraphMapper;
import org.neo4j.ogm.metadata.MetaData;
import org.neo4j.ogm.metadata.info.ClassInfo;

import java.util.ArrayList;
import java.util.Collection;

public class DefaultSessionImpl implements Session {

    private final MetaData metaData;
    private final MappingContext mappingContext;
    private final String url;

    private Neo4jRequestHandler<GraphModel> requestHandler;

    public DefaultSessionImpl(MetaData metaData, String url, CloseableHttpClient client) {
        this.metaData = metaData;
        this.mappingContext = new MappingContext();
        this.requestHandler = new GraphModelRequestHandler(client);
        this.url = url;
    }

    public void setRequestHandler(Neo4jRequestHandler request) {
        this.requestHandler = request;
    }

    public <T> T load(Class<T> type, Long id) {
        return loadOne(type, requestHandler.execute(url, new CypherQuery().findOne(id)));
    }

    public <T> Collection<T> loadAll(Class<T> type, Collection<Long> ids) {
        return loadAll(type, requestHandler.execute(url, new CypherQuery().findAll(ids)));
    }

    public <T> Collection<T> loadAll(Class<T> type) {
        ClassInfo classInfo = metaData.classInfo(type.getName());
        Neo4jResponseHandler<GraphModel> stream = requestHandler.execute(url, new CypherQuery().findByLabel(classInfo.labels()));
        return loadAll(type, stream);
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
        Collection<T> objects = new ArrayList();
        ObjectGraphMapper ogm = new ObjectGraphMapper(metaData, mappingContext);
        GraphModel graphModel;
        while ((graphModel = stream.next()) != null) {
            objects.add(ogm.load(type, graphModel));
        }
        return objects;
    }
}

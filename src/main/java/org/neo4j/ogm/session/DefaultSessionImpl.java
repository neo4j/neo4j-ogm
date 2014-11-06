package org.neo4j.ogm.session;

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

    private RequestHandler<GraphModel> requestHandler;

    public DefaultSessionImpl(MetaData metaData) {
        this.metaData = metaData;
        this.mappingContext = new MappingContext();
        this.requestHandler = new GraphModelRequestHandler();
    }

    public void setRequestHandler(RequestHandler request) {
        this.requestHandler = request;
    }

    public <T> T load(Class<T> type, Long id) {
        return loadOne(type, requestHandler.execute(new CypherQuery().findOne(id)));
    }

    public <T> Collection<T> loadAll(Class<T> type, Collection<Long> ids) {
        return loadAll(type, requestHandler.execute(new CypherQuery().findAll(ids)));
    }

    public <T> Collection<T> loadAll(Class<T> type) {
        ClassInfo classInfo = metaData.classInfo(type.getName());
        ResponseStream<GraphModel> stream = requestHandler.execute(new CypherQuery().findByLabel(classInfo.labels()));
        return loadAll(type, stream);
    }

    private <T> T loadOne(Class<T> type, ResponseStream<GraphModel> stream) {
        // todo: what happens if the stream returns more than one object? can we prevent this earlier?
        if (stream.hasNext()) {
            ObjectGraphMapper ogm = new ObjectGraphMapper(metaData, mappingContext);
            return ogm.load(type, stream.next());
        }
        // todo: who closes the stream
        return null;
    }

    private <T> Collection<T> loadAll(Class<T> type, ResponseStream<GraphModel> stream) {
        Collection<T> objects = new ArrayList();
        if (stream.hasNext()) {
            GraphModel graphModel;
            while ((graphModel = stream.next()) != null) {
                ObjectGraphMapper ogm = new ObjectGraphMapper(metaData, mappingContext);
                objects.add(ogm.load(type, graphModel));
            }
        }
        // todo: who closes the stream?
        return objects;
    }
}

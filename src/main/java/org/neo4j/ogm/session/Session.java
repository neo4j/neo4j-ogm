package org.neo4j.ogm.session;

import org.graphaware.graphmodel.neo4j.GraphModel;
import org.graphaware.graphmodel.neo4j.Property;
import org.neo4j.ogm.mapper.MappingContext;
import org.neo4j.ogm.mapper.MetaDataDrivenObjectToCypherMapper;
import org.neo4j.ogm.mapper.ObjectGraphMapper;
import org.neo4j.ogm.mapper.ObjectToCypherMapper;
import org.neo4j.ogm.mapper.cypher.CypherQuery;
import org.neo4j.ogm.mapper.cypher.ResponseStream;
import org.neo4j.ogm.metadata.MetaData;
import org.neo4j.ogm.metadata.info.ClassInfo;

import java.util.ArrayList;
import java.util.Collection;

public class Session {

    private final MetaData metaData;
    private final MappingContext mappingContext;
    private final CypherQuery query;

    Session(MetaData metaData, CypherQuery query) {
        this.metaData = metaData;
        this.query = query;
        this.mappingContext = new MappingContext();
    }

    public CypherQuery query() {
        return query;
    }

    public <T> T load(Class<T> type, Long id) {
        return loadOne(type, query.queryById(id));
    }

    public <T> Collection<T> loadByProperty(Class<T> type, Property property) {
        ClassInfo classInfo = metaData.classInfo(type.getName());
        return loadAll(type, query.queryByProperty(classInfo.labels(), property));
    }

    public <T> T save(T object) {
        ObjectToCypherMapper ocm = new MetaDataDrivenObjectToCypherMapper(metaData);
        Collection<String> cypherStatements = ocm.mapToCypher(object);
        return object;
    }

    public <T> Collection<T> load(Class<T> type) {
        ClassInfo classInfo = metaData.classInfo(type.getName());
        ResponseStream<GraphModel> stream = query.queryByLabel(classInfo.labels());
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

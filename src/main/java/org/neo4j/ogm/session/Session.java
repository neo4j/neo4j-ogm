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

    private MetaData metaData;
    private MappingContext mappingContext;

    private CypherQuery cypherQuery;

    Session(MetaData metaData) {
        this.metaData = metaData;
        this.mappingContext = new MappingContext();
    }

    public <T> T load(Class<T> type, Long id) {
        ObjectGraphMapper ogm = new ObjectGraphMapper(metaData, mappingContext);
        ResponseStream<GraphModel> stream = cypherQuery.queryById(id);

        return loadOne(type, stream);
    }

    public <T> Collection<T> loadByProperty(Class<T> type, Property property) {

        ClassInfo classInfo = metaData.classInfo(type.getName());
        ResponseStream<GraphModel> stream = cypherQuery.queryByProperty(classInfo.labels(), property);

        return loadAll(type, stream);
    }

    public <T> T save(T object) {
        ObjectToCypherMapper ocm = new MetaDataDrivenObjectToCypherMapper(metaData);
        Collection<String> cypherStatements = ocm.mapToCypher(object);
        return object;
    }

    public <T> Collection<T> load(Class<T> type) {
        ClassInfo classInfo = metaData.classInfo(type.getName());
        ResponseStream<GraphModel> stream = cypherQuery.queryByLabel(classInfo.labels());
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
            ObjectGraphMapper ogm = new ObjectGraphMapper(metaData, mappingContext);
            GraphModel graphModel;
            while ((graphModel = stream.next()) != null) {
                objects.add(ogm.load(type, graphModel));
            }
        }
        // todo: who closes the stream?
        return objects;
    }
}

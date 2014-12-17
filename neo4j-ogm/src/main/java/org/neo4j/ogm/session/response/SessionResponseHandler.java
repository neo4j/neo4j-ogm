package org.neo4j.ogm.session.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.neo4j.ogm.cypher.compiler.CypherContext;
import org.neo4j.ogm.entityaccess.FieldAccess;
import org.neo4j.ogm.mapper.GraphObjectMapper;
import org.neo4j.ogm.mapper.MappingContext;
import org.neo4j.ogm.metadata.MetaData;
import org.neo4j.ogm.metadata.info.ClassInfo;
import org.neo4j.ogm.model.GraphModel;
import org.neo4j.ogm.model.NodeModel;
import org.neo4j.ogm.model.Property;
import org.neo4j.ogm.session.result.RowModel;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class SessionResponseHandler implements ResponseHandler {

    private final MetaData metaData;
    private final MappingContext mappingContext;

    public SessionResponseHandler(MetaData metaData, MappingContext mappingContext) {
        this.metaData = metaData;
        this.mappingContext = mappingContext;
    }

    @Override
    public <T> Set<T> loadByProperty(Class<T> type, Neo4jResponse<GraphModel> response, Property<String, Object> filter) {

        GraphObjectMapper ogm = new GraphObjectMapper(metaData, mappingContext);
        Set<T> objects = new HashSet<>();

        GraphModel graphModel;
        while ((graphModel = response.next()) != null) {
            ogm.load(type, graphModel);
            for (NodeModel nodeModel : graphModel.getNodes()) {
                if (nodeModel.getPropertyList().contains(filter)) {
                    objects.add((T) mappingContext.get(nodeModel.getId()));
                }
            }
        }
        response.close();

        return objects;
    }

    @Override
    public void updateObjects(CypherContext context, Neo4jResponse<String> response, ObjectMapper mapper) {
        RowModelResponse rowModelResponse = new RowModelResponse(response, mapper);
        String[] variables = rowModelResponse.columns();
        RowModel rowModel;

        while ((rowModel = rowModelResponse.next()) != null) {
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
        rowModelResponse.close();
    }

    @Override
    public <T> T loadById(Class<T> type, Neo4jResponse<GraphModel> response, Long id) {
        GraphObjectMapper ogm = new GraphObjectMapper(metaData, mappingContext);
        GraphModel graphModel;
        while ((graphModel = response.next()) != null) {
            ogm.load(type, graphModel);
        }
        response.close();
        return (T) mappingContext.get(id);
    }

    @Override
    public <T> Collection<T> loadAll(Class<T> type, Neo4jResponse<GraphModel> response) {
        Set<T> objects = new HashSet<>();
        GraphObjectMapper ogm = new GraphObjectMapper(metaData, mappingContext);
        GraphModel graphModel;
        while ((graphModel = response.next()) != null) {
            objects.addAll(ogm.load(type, graphModel));
        }
        response.close();
        return objects;
    }

}

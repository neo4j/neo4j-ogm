package org.neo4j.ogm.mapper;

import org.neo4j.ogm.MetaData;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.model.GraphRowListModel;
import org.neo4j.ogm.model.GraphRowModel;
import org.neo4j.ogm.response.Response;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author vince
 */
public class GraphRowListModelMapper implements ResponseMapper<GraphRowListModel> {

    private final MetaData metaData;
    private final MappingContext mappingContext;

    public GraphRowListModelMapper(MetaData metaData, MappingContext mappingContext) {
        this.metaData = metaData;
        this.mappingContext = mappingContext;
    }

    public <T> Iterable<T> map(Class<T> type, Response<GraphRowListModel> response) {

        Set<T> result = new LinkedHashSet<>();
        Set<Long> resultEntityIds = new LinkedHashSet<>();
        ClassInfo classInfo = metaData.classInfo(type.getName());

        GraphEntityMapper ogm = new GraphEntityMapper(metaData, mappingContext);

        GraphRowListModel graphRowsModel;

        while ((graphRowsModel = response.next()) != null) {
            for (GraphRowModel graphRowModel : graphRowsModel.model()) {
                //Load the GraphModel into the ogm
                ogm.map(type, graphRowModel.getGraph());
                //Extract the id's of filtered nodes from the rowData and return them
                Object[] rowData = graphRowModel.getRow();
                for (Object data : rowData) {
                    if (data instanceof Number) {
                        resultEntityIds.add(((Number) data).longValue());
                    }
                }
            }
        }

        if (classInfo.annotationsInfo().get(RelationshipEntity.CLASS) == null) {
            for(Long resultEntityId : resultEntityIds) {
                result.add((T) mappingContext.getNodeEntity(resultEntityId));
            }

        }
        else {
            for(Long resultEntityId : resultEntityIds) {
                result.add((T) mappingContext.getRelationshipEntity(resultEntityId));
            }
        }
        return result;
    }
}

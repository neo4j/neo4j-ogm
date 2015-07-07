package org.neo4j.ogm.domain.mappings;

import org.neo4j.ogm.annotation.GraphId;

/**
 * @author Nils Dröge
 */
public abstract class Entity
{
    @GraphId
    private Long nodeId;

    public Long getNodeId()
    {
        return nodeId;
    }

    public void setNodeId(Long nodeId)
    {
        this.nodeId = nodeId;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || nodeId == null || getClass() != o.getClass()) return false;

        Entity entity = (Entity) o;

        if (!nodeId.equals(entity.nodeId)) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        return (nodeId == null) ? -1 : nodeId.hashCode();
    }
}

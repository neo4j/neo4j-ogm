package org.neo4j.ogm.domain.mappings;

import org.neo4j.ogm.annotation.Relationship;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Nils Dröge
 */
public class Tag extends Entity
{
    private String name;

    @Relationship(type = "HAS", direction = Relationship.INCOMING)
    private Set<Entity> entities = new HashSet<Entity>();

    public Tag() {}

    public Tag(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Set<Entity> getEntities()
    {
        return entities;
    }

    public void setEntities(Set<Entity> entities)
    {
        this.entities = entities;
    }

    @Override
    public String toString()
    {
        return "Tag{" +
            "id:" + getNodeId() +
            ", name:'" + name + "'" +
            '}';
    }
}

package org.neo4j.ogm.domain.mappings;

import org.neo4j.ogm.annotation.Relationship;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Nils Dröge
 */
public class Category extends Entity
{
    private String name;

    @Relationship(type = "HAS", direction = Relationship.INCOMING)
    private Set<Event> events = new HashSet<Event>();;

    public Category()
    {
    }

    public Category(String name)
    {
        this();
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

    @Override
    public String toString()
    {
        return "Category{" +
            "id:" + getNodeId() +
            ", name:'" + name + "'" +
            '}';
    }
}

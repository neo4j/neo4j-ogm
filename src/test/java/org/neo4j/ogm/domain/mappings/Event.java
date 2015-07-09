package org.neo4j.ogm.domain.mappings;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.neo4j.ogm.annotation.Relationship;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Nils Dröge
 */
public class Event extends Entity
{
    private String title;

    @Relationship(type = "HAS")
    private Category category;

    @JsonIgnore
    @Relationship(type = "HAS")
    private Set<Tag> tags = new HashSet<Tag>();

    public Event() {}

    public Event(String title)
    {
        this.title = title;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public Category getCategory()
    {
        return category;
    }

    public void setCategory(Category category)
    {
        this.category = category;
    }

    public Set<Tag> getTags()
    {
        return tags;
    }

    public void setTags(Set<Tag> tags)
    {
        this.tags = tags;
    }

    @Override
    public String toString()
    {
        return "Event{" +
            "id:" + getNodeId() +
            ", title:'" + title + "'" +
            '}';
    }
}

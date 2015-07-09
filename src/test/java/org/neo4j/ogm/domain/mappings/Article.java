package org.neo4j.ogm.domain.mappings;

import org.neo4j.ogm.annotation.Relationship;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Nils Dröge
 */
public class Article extends Entity
{
    private String title;

    @Relationship(type = "LIKE", direction = Relationship.INCOMING)
    private Set<Person> likes = new HashSet<Person>();

    public Article() {}

    public Set<Person> getLikes()
    {
        return likes;
    }

    public void setLikes(Set<Person> likes)
    {
        this.likes = likes;
    }

    @Override
    public String toString()
    {
        return "Article{" +
            "id:" + getNodeId() +
            ", title:'" + title + "'" +
            '}';
    }
}

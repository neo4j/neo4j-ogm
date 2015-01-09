package org.neo4j.ogm.domain.forum;

import java.util.List;

import org.neo4j.ogm.annotation.NodeEntity;

/**
 * Represents a forum that contains a number of topics.
 */
@NodeEntity
public class Forum {

    private List<ForumTopicLink> topicsInForum;
    private String name;


    public List<ForumTopicLink> getTopicsInForum() {
        return topicsInForum;
    }

    public void setTopicsInForum(List<ForumTopicLink> topicsInForum) {
        this.topicsInForum = topicsInForum;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}

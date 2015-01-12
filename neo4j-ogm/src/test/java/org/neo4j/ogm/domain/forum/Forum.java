package org.neo4j.ogm.domain.forum;

import java.util.List;

/**
 * Represents a forum that contains a number of topics.
 */
public class Forum {

    private Long id;
    private List<ForumTopicLink> topicsInForum;
    private String name;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

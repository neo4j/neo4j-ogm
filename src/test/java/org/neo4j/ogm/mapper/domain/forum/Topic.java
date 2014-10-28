package org.neo4j.ogm.mapper.domain.forum;

import org.neo4j.ogm.annotation.NodeId;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.mapper.domain.forum.activity.Post;

import java.util.List;

public class Topic {

    @Relationship(name="HAS_POSTS")
    private List<Post> posts;
    private Boolean inActive;

    @NodeId
    private Long topicId;

    public List<Post> getPosts() {
        return posts;
    }

    public void setPosts(List<Post> posts) {
        setInActive(posts.isEmpty());
        this.posts = posts;
    }

    public Boolean getInActive() {
        return inActive;
    }

    public void setInActive(Boolean inActive) {
        this.inActive = inActive;
    }
}

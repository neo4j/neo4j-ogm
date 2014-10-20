package org.neo4j.ogm.mapper.domain.forum;

import org.neo4j.ogm.mapper.domain.forum.activity.Post;

import java.util.List;

public class Topic {

    private List<Post> posts;
    private Boolean open;

    public List<Post> getPosts() {
        return posts;
    }

    public void setPosts(List<Post> posts) {
        this.posts = posts;
    }

    public Boolean getOpen() {
        return open;
    }

    public void setOpen(Boolean open) {
        this.open = open;
    }
}

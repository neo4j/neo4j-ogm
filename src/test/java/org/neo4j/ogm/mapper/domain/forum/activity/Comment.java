package org.neo4j.ogm.mapper.domain.forum.activity;

public class Comment extends Activity {

    private String comment;

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}

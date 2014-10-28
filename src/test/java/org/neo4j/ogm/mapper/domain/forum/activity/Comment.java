package org.neo4j.ogm.mapper.domain.forum.activity;

import org.neo4j.ogm.annotation.Property;

public class Comment extends Activity {

    private String comment;

    @Property(name="remark")
    public String getComment() {
        return comment;
    }

    @Property(name="remark")
    public void setComment(String comment) {
        this.comment = comment;
    }
}

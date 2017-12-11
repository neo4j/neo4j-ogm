package org.neo4j.ogm.domain.locking;

import java.util.Date;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;
import org.neo4j.ogm.annotation.Version;

/**
 * @author Frantisek Hartman
 */
@RelationshipEntity(type = "FRIEND_OF")
public class FriendOf {

    private Long id;

    @StartNode
    private User from;

    @EndNode
    private User to;

    @Version
    private Long version;

    private Date since;

    private String description;

    public FriendOf() {
    }

    public FriendOf(User from, User to) {
        this.from = from;
        this.to = to;
        this.since = new Date();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getFrom() {
        return from;
    }

    public void setFrom(User from) {
        this.from = from;
    }

    public User getTo() {
        return to;
    }

    public void setTo(User to) {
        this.to = to;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Date getSince() {
        return since;
    }

    public void setSince(Date since) {
        this.since = since;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

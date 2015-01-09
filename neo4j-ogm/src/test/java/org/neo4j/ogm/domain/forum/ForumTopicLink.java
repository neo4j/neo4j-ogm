package org.neo4j.ogm.domain.forum;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

/**
 * Completely pointless class that does nothing but exercise {@link RelationshipEntity}.
 */
@RelationshipEntity(type = "HAS_TOPIC")
public class ForumTopicLink {

    @GraphId
    private Long id;
    @StartNode
    private Forum forum;
    @EndNode
    private Topic topic;
    private long timestamp;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Forum getForum() {
        return forum;
    }

    public void setForum(Forum forum) {
        this.forum = forum;
    }

    public Topic getTopic() {
        return topic;
    }

    public void setTopic(Topic topic) {
        this.topic = topic;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

}

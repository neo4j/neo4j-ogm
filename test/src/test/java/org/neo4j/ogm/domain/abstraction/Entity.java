package org.neo4j.ogm.domain.abstraction;

import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.Labels;
import org.neo4j.ogm.annotation.PostLoad;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static java.util.UUID.randomUUID;

public abstract class Entity {

    @Id
    protected String uuid;

    @Labels
    protected Set<String> labels = new HashSet<>();

    public Entity() {
        uuid = randomUUID().toString();
    }

    public Entity(String uuid) {
        this.uuid = uuid;
    }

    @PostLoad
    public void postLoad() {
    }

    public void addLabel(Object... labels) {
        for (Object label : labels) {
            this.labels.add(label.toString());
        }
    }

    public void removeLabel(Object... labels) {
        for (Object label : labels) {
            this.labels.remove(label.toString());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Entity entity = (Entity) o;
        return Objects.equals(uuid, entity.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Set<String> getLabels() {
        return labels;
    }

    public void setLabels(Set<String> labels) {
        this.labels = labels;
    }

}

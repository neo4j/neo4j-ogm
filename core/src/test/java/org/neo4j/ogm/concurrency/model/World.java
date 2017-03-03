package org.neo4j.ogm.concurrency.model;

import java.util.HashSet;
import java.util.Set;

import org.neo4j.ogm.annotation.Relationship;

/**
 * Created by markangrish on 13/01/2017.
 */
public class World {

    private final static String REACHABLE_BY_ROCKET = "REACHABLE_BY_ROCKET";

    private Long id;

    private String name;

    private int moons;

    private Float radius;

    private Long updated;

    @Relationship(type=REACHABLE_BY_ROCKET, direction = "UNDIRECTED")
    private Set<World> reachableByRocket = new HashSet<>();

    public Long getUpdated() {
        return updated;
    }

    public void setUpdated(long updated) {
        this.updated = updated;
    }




    public World(String name, int moons) {
        this.name = name;
        this.moons = moons;
    }

    public World() {
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getMoons() {
        return moons;
    }

    public void addRocketRouteTo(World otherWorld) {
        reachableByRocket.add(otherWorld);
        // symmetric relationship.
        otherWorld.reachableByRocket.add(this); // bi-directional in domain.
    }

    public Set<World> getReachableByRocket() {
        return this.reachableByRocket;
    }

    public void setReachableByRocket(Set<World> reachableByRocket) {
        this.reachableByRocket.clear();
        this.reachableByRocket = reachableByRocket;
    }

    public boolean canBeReachedFrom(World otherWorld) {
        for(World world : reachableByRocket) {
            if(world.equals(otherWorld)) {
                return true;
            }
        }
        return false;
    }

    public Float getRadius() {
        return radius;
    }

    public void setRadius(Float radius) {
        this.radius = radius;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        World world = (World) o;

        if (moons != world.moons) return false;
        if (id != null ? !id.equals(world.id) : world.id != null) return false;
        return !(name != null ? !name.equals(world.name) : world.name != null);
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + moons;
        return result;
    }

    @Override
    public String toString() {
        return String.format("World{name='%s', moons=%d}", name, moons);
    }
}

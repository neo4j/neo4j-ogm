package org.neo4j.ogm.domain.bike;

public class Wheel {
    private Long id;
    private Integer spokes;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getSpokes() {
        return spokes;
    }

    public void setSpokes(Integer spokes) {
        this.spokes = spokes;
    }
}

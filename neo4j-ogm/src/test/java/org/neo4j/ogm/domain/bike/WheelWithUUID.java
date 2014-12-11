package org.neo4j.ogm.domain.bike;

public class WheelWithUUID {

    private Long id;
    private String uuid;
    private Integer spokes;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Integer getSpokes() {
        return spokes;
    }

    public void setSpokes(Integer spokes) {
        this.spokes = spokes;
    }
}

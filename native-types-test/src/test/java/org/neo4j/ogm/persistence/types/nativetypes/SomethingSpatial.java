package org.neo4j.ogm.persistence.types.nativetypes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Properties;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.types.spatial.CartesianPoint2d;
import org.neo4j.ogm.types.spatial.CartesianPoint3d;
import org.neo4j.ogm.types.spatial.GeographicPoint2d;
import org.neo4j.ogm.types.spatial.GeographicPoint3d;

@NodeEntity
public class SomethingSpatial {

    private Long id;

    // Convert to native types
    private GeographicPoint2d geographicPoint2d;
    private GeographicPoint3d geographicPoint3d;

    private CartesianPoint2d cartesianPoint2d;
    private CartesianPoint3d cartesianPoint3d;

    // Do not try to convert
    private List<String> stringList = new ArrayList<>();

    @Properties
    private Map<String, String> properties = new HashMap<>();

    @Relationship("REF")
    private Collection<SomethingRelationship> rels = new ArrayList<>();

    public SomethingSpatial() {
        stringList.add("a");
        stringList.add("b");

        properties.put("a", "a");
        properties.put("b", "b");
    }

    public Long getId() {
        return id;
    }

    public GeographicPoint2d getGeographicPoint2d() {
        return geographicPoint2d;
    }

    public void setGeographicPoint2d(GeographicPoint2d geographicPoint2d) {
        this.geographicPoint2d = geographicPoint2d;
    }

    public GeographicPoint3d getGeographicPoint3d() {
        return geographicPoint3d;
    }

    public void setGeographicPoint3d(GeographicPoint3d geographicPoint3d) {
        this.geographicPoint3d = geographicPoint3d;
    }

    public CartesianPoint2d getCartesianPoint2d() {
        return cartesianPoint2d;
    }

    public void setCartesianPoint2d(CartesianPoint2d cartesianPoint2d) {
        this.cartesianPoint2d = cartesianPoint2d;
    }

    public CartesianPoint3d getCartesianPoint3d() {
        return cartesianPoint3d;
    }

    public void setCartesianPoint3d(CartesianPoint3d cartesianPoint3d) {
        this.cartesianPoint3d = cartesianPoint3d;
    }

    public List<String> getStringList() {
        return stringList;
    }

    public void setStringList(List<String> stringList) {
        this.stringList = stringList;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public Collection<SomethingRelationship> getRels() {
        return rels;
    }

    public void setRels(
        Collection<SomethingRelationship> rels) {
        this.rels = rels;
    }
}

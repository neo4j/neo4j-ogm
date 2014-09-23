package org.neo4j.ogm.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.graphaware.graphmodel.Property;
import org.graphaware.graphmodel.neo4j.EdgeModel;
import org.graphaware.graphmodel.neo4j.GraphModel;
import org.graphaware.graphmodel.neo4j.NodeModel;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;

/**
 * Test a simple mapping strategy, where the domain model matches the graph model.
 */
public class DomainTest {

    @Test
    public void testDefaultBikeMapping() throws Exception {

        /*
         * example REST response from cypher query: "MATCH p=(b:Bike)-->(component) WHERE id(b) = 15 RETURN p"
         */
        final String json =
                "{\"graph\": { " +
                    "\"nodes\" :[ " +
                        "{\"id\" : \"15\",\"labels\" : [ \"Bike\"], \"properties\" : {} }, " +
                        "{\"id\" : \"16\",\"labels\" : [ \"Wheel\", \"FrontWheel\" ],\"properties\" : {\"spokes\" : 3 } }, " +
                        "{\"id\" : \"17\",\"labels\" : [ \"Wheel\", \"BackWheel\" ],\"properties\" : {\"spokes\" : 5 } }, " +
                        "{\"id\" : \"18\",\"labels\" : [ \"Frame\" ],\"properties\" : {\"size\" : 27 } }, " +
                        "{\"id\" : \"19\",\"labels\" : [ \"Saddle\" ],\"properties\" : {\"price\" : 42.99, \"material\" : \"plastic\" } } " +
                    "], " +
                    "\"relationships\": [" +
                        "{\"id\":\"141\",\"type\":\"HAS_WHEEL\",\"startNode\":\"15\",\"endNode\":\"16\",\"properties\":{ \"purchased\" : 20130917 }}, " +
                        "{\"id\":\"142\",\"type\":\"HAS_WHEEL\",\"startNode\":\"15\",\"endNode\":\"17\",\"properties\":{ \"purchased\" : 20130917 }}," +
                        "{\"id\":\"143\",\"type\":\"HAS_FRAME\",\"startNode\":\"15\",\"endNode\":\"18\",\"properties\":{ \"purchased\" : 20130917 }}," +
                        "{\"id\":\"144\",\"type\":\"HAS_SADDLE\",\"startNode\":\"15\",\"endNode\":\"19\",\"properties\":{\"purchased\" : 20130922 }} " +
                    "] " +
                "} }";

        /*
         * 1. create the graphmodel
         */
        GraphModel graphModel = GraphBuilder.build(json);

        /*
         * 2. Build our domain objects from the graph
         */
        Map<Long, Object> objectMap = new HashMap();
        Map<Class, List<Object>> typeMap = new HashMap();
        List<EdgeModel> nonScalar = new ArrayList<EdgeModel>();

        for (NodeModel node : graphModel.getNodes()) {
            String baseClass = node.getLabels()[0]; // by convention :)
            Object object = instantiate(baseClass);
            setId(object, node.getId());
            objectMap.put(node.getId(), object);
            List<Object> objectList = typeMap.get(object.getClass());
            if (objectList == null) {
                objectList = new ArrayList<Object>();
                typeMap.put(object.getClass(), objectList);
            }
            objectList.add(object);
            setAttributes(node, object);
        }

        // 3. scalar types
        for (EdgeModel edge : graphModel.getRelationships()) {
            if (!attachChild(objectMap.get(edge.getStartNode()), objectMap.get(edge.getEndNode()), typeMap)) {
                nonScalar.add(edge);
            }
        }

        // 4. vector types
        for (EdgeModel edge : nonScalar) {

            Object parent = objectMap.get(edge.getStartNode());
            Object child = objectMap.get(edge.getEndNode());

            if (typeMap.get(child.getClass()) != null) {
                Method method = findParameterisedSetter(parent, child);
                if (method == null) {
                    throw new RuntimeException("can't finder any setter for " + child.getClass().getName() + " in " + parent.getClass().getName());
                }

                // basic vector types we will handle: List<T>, Set<T>, Vector<T>
                Class collectionType = method.getParameterTypes()[0];
                if (collectionType == List.class) {
                    ArrayList arrayList = new ArrayList();
                    arrayList.addAll(typeMap.get(child.getClass()));
                    method.invoke(parent, arrayList);
                    typeMap.remove(child.getClass()); // we've added them all, no point in doing this for each one.
                } else {
                    throw new RuntimeException("Unsupported: " + collectionType.getName());
                }
            }
        }
        /*
         * finally, get the root object (how do we automatically figure this out?),
         * cast to an instance of bike, then test...
         */

        Bike bike = (Bike) typeMap.get(Class.forName(fqn("Bike"))).get(0);

        assertNotNull(bike);
        assertEquals(15, (long) bike.getId());

        // check the frame
        assertEquals(18, (long) bike.getFrame().getId());
        assertEquals(27, (int) bike.getFrame().getSize());

        // check the saddle
        assertEquals(19, (long) bike.getSaddle().getId());
        assertEquals(42.99, (double) bike.getSaddle().getPrice(), 0.00);
        assertEquals("plastic", bike.getSaddle().getMaterial());

        // check the wheels
        assertEquals(2, bike.getWheels().size());
        for (Wheel wheel : bike.getWheels()) {
            if (wheel.getId().equals(16)) {
                assertEquals(3, (int) wheel.getSpokes());
            }
            if (wheel.getId().equals(17)) {
                assertEquals(5, (int) wheel.getSpokes());
            }
        }
    }

    private void setId(Object object, Long id) throws Exception {
        Method method = object.getClass().getMethod("setId", Long.class);
        method.invoke(object, id);
    }

    private void setAttributes(NodeModel nodeModel, Object o) throws Exception {
        for (Property<String, Object> property : nodeModel.getAttributes()) {
            Object parameter = property.getValue();
            String methodName = setter(property.getKey());
            Method method = o.getClass().getMethod(methodName, new Class[] { parameter.getClass() } );
            method.invoke(o, parameter);
        }
    }

    private boolean attachChild(Object parent, Object child, Map<Class, List<Object>> typeMap) throws Exception {
        String methodName = setter(child.getClass().getSimpleName());
        try {
            Method method = parent.getClass().getMethod(methodName, new Class[] { child.getClass() } );
            method.invoke(parent, child);
            typeMap.remove(child.getClass());
            return true;
        } catch (NoSuchMethodException me) {
            return false;
        }
    }

    private Object instantiate(String baseClass) throws Exception {
        return Class.forName(fqn(baseClass)).newInstance();
    }

    private String fqn(String simpleName) {
        return "org.neo4j.ogm.mapper.DomainTest$" + simpleName;
    }

    private String setter(String property) {
        StringBuilder sb = new StringBuilder();
        sb.append("set");
        sb.append(property.substring(0,1).toUpperCase());
        sb.append(property.substring(1));
        return sb.toString();
    }

    private Method findParameterisedSetter(Object parent, Object child) {
        for (Method m : parent.getClass().getMethods()) {
            if (m.getGenericParameterTypes().length == 1) {
                Type t = m.getGenericParameterTypes()[0];
                if (t.toString().contains(child.getClass().getName())) {
                    if (m.getName().startsWith("set")) {
                        return m;
                    }
                }
            }
        }
        return null;
    }

    /*
    * The domain (Bike) under test
    */
    static class Wheel {

        private Long id;
        private Integer spokes;

        public Wheel() {}

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

    static class Frame {

        private Long id;
        private Integer size;

        public Frame() {}

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Integer getSize() {
            return size;
        }

        public void setSize(Integer size) {
            this.size = size;
        }
    }

    static class Saddle {

        private Long id;
        private Double price;
        private String material;

        public Saddle() {}

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Double getPrice() {
            return price;
        }

        public void setPrice(Double price) {
            this.price = price;
        }

        public String getMaterial() {
            return material;
        }

        public void setMaterial(String material) {
            this.material = material;
        }
    }

    static class Bike {

        private Long id;
        private List<Wheel> wheels;
        private Frame frame;
        private Saddle saddle;

        public Bike() {}

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public List<Wheel> getWheels() {
            return wheels;
        }

        public void setWheels(List<Wheel> wheels) {
            this.wheels = wheels;
        }

        public Frame getFrame() {
            return frame;
        }

        public void setFrame(Frame frame) {
            this.frame = frame;
        }

        public Saddle getSaddle() {
            return saddle;
        }

        public void setSaddle(Saddle saddle) {
            this.saddle = saddle;
        }
    }

    static class GraphBuilder {

        private GraphModel graph;

        GraphModel getGraph() {
            return graph;
        }

        void setGraph(GraphModel graph) {
            this.graph = graph;
        }

        public static GraphModel build(String json) throws IOException {
            GraphBuilder instance = new ObjectMapper().readValue(json, GraphBuilder.class);
            return instance.getGraph();
        }
    }

}

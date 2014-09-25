package org.neo4j.ogm.mapper;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.graphaware.graphmodel.Property;
import org.graphaware.graphmodel.Taxon;
import org.graphaware.graphmodel.neo4j.EdgeModel;
import org.graphaware.graphmodel.neo4j.GraphModel;
import org.graphaware.graphmodel.neo4j.NodeModel;
import org.junit.Test;
import org.neo4j.ogm.metadata.ClassDictionary;
import org.neo4j.ogm.metadata.DefaultConstructorObjectCreator;
import org.neo4j.ogm.metadata.ObjectCreator;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Test a simple mapping strategy, where the domain model matches the graph model.
 */
public class BikeTest {

    private final Map<Long, Object> objectMap = new HashMap<>();
    private final Map<Class, List<Object>> typeMap = new HashMap<>();
    private final List<EdgeModel> vectorRelationships = new ArrayList<>();

    private ObjectCreator objectCreator = new DefaultConstructorObjectCreator(new ClassDictionary() {
            @Override
            public String determineFqnFromTaxa(List<Taxon> taxa) {
                return fqn(taxa.get(0).getName().toString());
            }
        });

    @Test
    public void testDefaultBikeMapping() throws Exception {

        GraphModel graphModel = GraphBuilder.build(getCypherResponse());

        createDomainObjects(graphModel);
        createScalarRelationships(graphModel);
        createIterableRelationships();

        Bike bike = (Bike) getRootObject();

        assertNotNull(bike);
        assertEquals(15, (long) bike.getId());
        assertEquals(2, bike.getColours().length);

        // check the frame
        assertEquals(18, (long) bike.getFrame().getId());
        assertEquals(27, (int) bike.getFrame().getSize());

        // check the saddle
        assertEquals(19, (long) bike.getSaddle().getId());
        assertEquals(42.99, bike.getSaddle().getPrice(), 0.00);
        assertEquals("plastic", bike.getSaddle().getMaterial());

        // check the wheels
        assertEquals(2, bike.getWheels().size());
        for (Wheel wheel : bike.getWheels()) {
            if (wheel.getId().equals(16L)) {
                assertEquals(3, (int) wheel.getSpokes());
            }
            if (wheel.getId().equals(17L)) {
                assertEquals(5, (int) wheel.getSpokes());
            }
        }
    }

    private Object getRootObject() throws Exception {
        // TODO:
        // there should be only one object in the typeMap when this is called
        // otherwise the object mapping has failed.
        // we could use that fact to drive this method, rather than
        // relying a known name.
        return typeMap.get(Class.forName(fqn("Bike"))).get(0);
    }

    private void createIterableRelationships() throws Exception {

        for (EdgeModel edge : vectorRelationships) {

            Object instance = objectMap.get(edge.getStartNode());
            Object parameter = objectMap.get(edge.getEndNode());

            if (typeMap.get(parameter.getClass()) != null) {
                Method method = findParameterisedSetter(instance, parameter, setterName(parameter.getClass().getSimpleName()));
                setIterableParameter(instance, method, typeMap.get(parameter.getClass()));
                typeMap.remove(parameter.getClass()); // we've added all instances of parameter, no point in repeating the effort.
            }
        }
    }

    private void setIterableParameter(Object instance, Method method, Collection<?> collection) throws Exception {

        // basic "collection" types we will handle: List<T>, Set<T>, Vector<T>, T[]
        Class parameterType = method.getParameterTypes()[0];

        if (parameterType == List.class) {
            List<Object> list = new ArrayList<>();
            list.addAll(collection);
            method.invoke(instance, list);
        }

        else if (parameterType == Set.class) {
            Set<Object> set = new HashSet<>();
            set.addAll(collection);
            method.invoke(instance, set);
        }

        else if (parameterType == Vector.class) {
            Vector<Object> v = new Vector<>();
            v.addAll(collection);
            method.invoke(instance, v);
        }

        else if (parameterType.isArray()) {
            Class type = parameterType.getComponentType();
            Object array = Array.newInstance(type, collection.size());
            List<Object> objects = new ArrayList<>();
            objects.addAll(collection);
            for (int i = 0; i < objects.size(); i++) {
                Array.set(array, i, objects.get(i));
            }
            method.invoke(instance, array );
        }

        else {
            throw new RuntimeException("Unsupported: " + parameterType.getName());
        }

    }

    @SuppressWarnings("unchecked")
    private static <T> T[] newArray(Class<T> type, int length) {
        return (T[]) Array.newInstance(type, length);
    }

    private void createScalarRelationships(GraphModel graphModel) throws Exception {
        for (EdgeModel edge : graphModel.getRelationships()) {
            Object parent = objectMap.get(edge.getStartNode());
            Object child  = objectMap.get(edge.getEndNode());
            if (setScalarParameter(parent, child)) {
                typeMap.remove(child.getClass());
            } else {
                vectorRelationships.add(edge);
            }
        }
    }

    private void createDomainObjects(GraphModel graphModel) throws Exception {
        for (NodeModel node : graphModel.getNodes()) {
            Object object = objectCreator.instantiateObjectMappedTo(node);
            setId(object, node.getId());
            objectMap.put(node.getId(), object);
            List<Object> objectList = typeMap.get(object.getClass());
            if (objectList == null) {
                objectList = new ArrayList<>();
                typeMap.put(object.getClass(), objectList);
            }
            objectList.add(object);
            setJavaLangParameter(node, object);
        }
    }

    /*
     * example REST response from cypher query: "MATCH p=(b:Bike)-->(component) WHERE id(b) = 15 RETURN p"
    */
    private String getCypherResponse() {
        return
                "{\"graph\": { " +
                        "\"nodes\" :[ " +
                        "{\"id\" : \"15\",\"labels\" : [ \"Bike\"], \"properties\" : { \"colours\" :[\"red\", \"black\"] } }, " +
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
    }

    private void setId(Object object, Long id) throws Exception {
        object.getClass().getMethod("setId", Long.class).invoke(object, id);
    }

    private void setJavaLangParameter(NodeModel nodeModel, Object instance) throws Exception {
        for (Property property : nodeModel.getAttributes()) {
            Method method;
            try {
                method = findSetter(instance, property.getValue(), setterName((String) property.getKey()));
                method.invoke(instance, property.getValue());
            } catch (NoSuchMethodException nsm) {
                if (Iterable.class.isAssignableFrom(property.getValue().getClass())) {
                    Object typeInstance = ((Iterable)property.getValue()).iterator().next();
                    if (typeInstance != null) {
                        method = findParameterisedSetter(instance, typeInstance, setterName((String) property.getKey()));
                        setIterableParameter(instance, method, (Collection) property.getValue());
                    } else {
                        // what do we do here?
                        throw nsm;
                    }
                } else {
                    throw nsm;
                }
            }

        }
    }

    /**
     * sets a scalar parameter on an object instance.
     * @param instance
     * @param parameter
     * @return
     * @throws Exception
     */
    private boolean setScalarParameter(Object instance, Object parameter) throws Exception {
        try {
            findSetter(instance, parameter, setterName(parameter.getClass().getSimpleName())).invoke(instance, parameter);
            return true;
        } catch (NoSuchMethodException me) {
            return false;
        }
    }

    /*
     * obtain the fully qualified name of the domain class whose simple name is represented by simpleName
     * this implementation relies on the domain classes being static members of the test class
     */
    private String fqn(String simpleName) {
        return this.getClass().getName() + "$" + simpleName;
    }

    private String setterName(String property) {
        StringBuilder sb = new StringBuilder();
        sb.append("set");
        sb.append(property.substring(0,1).toUpperCase());
        sb.append(property.substring(1));
        return sb.toString();
    }

    private Method findSetter(Object instance, Object parameter, String methodName) throws NoSuchMethodException {
        //System.out.println("looking for setter " + methodName + " taking parameter of type " + parameter.getClass().getName());
        for (Method method : instance.getClass().getMethods()) {
            if( Modifier.isPublic(method.getModifiers()) &&
                    method.getReturnType().equals(void.class) &&
                    method.getName().startsWith(methodName) &&
                    method.getParameterTypes().length == 1 &&
                    method.getParameterTypes()[0] == parameter.getClass())
                return method;
        }
        throw new NoSuchMethodException("Cannot find setter for " + parameter.getClass().getName());
    }

    private Method findParameterisedSetter(Object instance, Object type, String methodName) throws NoSuchMethodException {
        //System.out.println("Looking for method " + methodName + "* with type parameter assignable from Iterable<" + type.getClass().getSimpleName() + ">");
        for (Method m : instance.getClass().getMethods()) {
            if (Modifier.isPublic(m.getModifiers()) &&
              m.getReturnType().equals(void.class) &&
              m.getName().startsWith(methodName) &&
              m.getParameterTypes().length == 1 &&
              m.getGenericParameterTypes().length == 1) {
                Type t = m.getGenericParameterTypes()[0];
                if (t.toString().contains(type.getClass().getName())) {
                    return m;
                }
            }
        }
        throw new NoSuchMethodException("Cannot find method " + methodName + "* with type parameter assignable from Iterable<" + type.getClass().getSimpleName() + ">");
    }

    @SuppressWarnings("UnusedDeclaration")
    public static class Wheel {

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

    @SuppressWarnings("UnusedDeclaration")
    public static class Frame {

        private Long id;
        private Integer size;

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

    @SuppressWarnings("UnusedDeclaration")
    public static class Saddle {

        private Long id;
        private Double price;
        private String material;

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

    @SuppressWarnings("UnusedDeclaration")
    public static class Bike {

        private String[] colours;
        private Long id;
        private List<Wheel> wheels;
        private Frame frame;
        private Saddle saddle;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String[] getColours() {
            return colours;
        }

        public void setColours(String[] colours) {
            this.colours = colours;
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

        @SuppressWarnings("UnusedDeclaration")
        void setGraph(GraphModel graph) {
            this.graph = graph;
        }

        public static GraphModel build(String json) throws IOException {
            GraphBuilder instance = new ObjectMapper().readValue(json, GraphBuilder.class);
            return instance.getGraph();
        }
    }

}

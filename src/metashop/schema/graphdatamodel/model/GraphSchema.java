package metashop.schema.graphdatamodel.model;

import org.neo4j.driver.*;
import org.neo4j.driver.Record;

import java.util.ArrayList;

public class GraphSchema implements AutoCloseable{

    private final String name;
    private ArrayList<EntityType> entities;
    private ArrayList<RelationshipType> relationships;
    private static Driver driver;
    private final static String uri = "bolt://localhost:7687";
    private final static String user = "neo4j";
    private final static String password = "12345678";

    public GraphSchema(String name) {
        driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
        this.name = name;
        this.entities = addEntities();
        this.relationships = addRelationships();
    }

    public ArrayList<EntityType> addEntities(){
        ArrayList<EntityType> entityTypes = new ArrayList<>();
        ArrayList<Record> nodes = getNodes();
        for (Record node : nodes) {
            EntityType entityType = new EntityType(node);
            if (!entityTypes.contains(entityType))
                entityTypes.add(entityType);
        }
        return entityTypes;
    }

    public ArrayList<RelationshipType> addRelationships(){
        ArrayList<RelationshipType> relationshipTypes = new ArrayList<>();
        ArrayList<Record> relationships = getRelationships();
        for (Record relationship : relationships) {
            RelationshipType relationshipType = new RelationshipType(relationship);
            if (!relationshipTypes.contains(relationshipType))
                relationshipTypes.add(relationshipType);
        }
        return relationshipTypes;
    }

    /**
     * Método para la obtención de todas las etiquetas existentes en la BBDD
     * @return ArrayList de nodos
     */
    public static ArrayList<Record> getNodes() {
        try (Session session = driver.session()) {
            ArrayList<Record> nodes = session.executeWrite(tx -> {
                Query query = new Query("MATCH (n) RETURN n");
                Result result = tx.run(query);
                return new ArrayList<>(result.list());
            });
            return nodes;
        }
    }

    /**
     * Método para la obtención de todas las relaciones existentes en la BBDD
     * @return ArrayList de relaciones
     */
    public static  ArrayList<Record> getRelationships(){
        try (Session session = driver.session()) {
            ArrayList<Record> relationships = session.executeWrite(tx -> {
                Query query = new Query("MATCH (n)-[r]->() RETURN r");
                Result result = tx.run(query);
                return new ArrayList<>(result.list());
            });
            return relationships;
        }
    }

    @Override
    public void close() throws Exception {
        driver.close();
    }

    @Override
    public String toString() {
        return "\nGraphSchema{" +
                "name='" + name + '\'' +
                ", entities=" + entities +
                ", relationships=" + relationships +
                "}\n";
    }

}

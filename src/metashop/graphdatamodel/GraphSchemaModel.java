package metashop.graphdatamodel;

import org.neo4j.driver.Record;
import java.util.ArrayList;
import java.util.HashMap;

public class GraphSchemaModel {

    private final String name;
    private final HashMap<String, EntityType> entities;
    private final ArrayList<RelationshipType> relationships;

    public GraphSchemaModel (String name, ArrayList<Record> nodes, ArrayList<Record> relationships, ArrayList<Record> relationshipsCardinality) {
        this.name = name;
        this.entities = addEntities(nodes);
        this.relationships = addRelationships(relationships, relationshipsCardinality);
    }

    /**
     *
     * Método para la generación de entidades del grafo. He decidido usar un HashMap<String, EntityType>
     *     porque
     * @param nodes Lista de nodos a procesar
     * @return HashMap de entidades.
     */
    private HashMap<String, EntityType> addEntities(ArrayList<Record> nodes){
        HashMap<String, EntityType> entityTypes = new HashMap<>();
        nodes.forEach((Record node) -> {
            EntityType entityType = new EntityType(node);
            entityTypes.put(entityType.getName(), entityType);
        });
        return entityTypes;
    }

    /**
     * Método para la generación de relaciones del grafo.
     * @see RelationshipType
     * @param relationships Conjunto de relaciones
     * @return ArrayList de RelationshipType
     */
    private ArrayList<RelationshipType> addRelationships(ArrayList<Record> relationships, ArrayList<Record> relationshipsCardinality){
        ArrayList<RelationshipType> relationshipTypes = new ArrayList<>();
        HashMap<String, Integer> maxCardinality = getRelationshipsCardinality(relationshipsCardinality);
        relationships.forEach((Record relationship) -> {
            RelationshipType relationshipType = new RelationshipType(relationship);
            relationshipType.setMaxCardinality(maxCardinality.get(relationshipType.getName()));
            relationshipType.setOrigin(entities.get(relationship.values().get(RelationshipType.ORIGIN_ENTITY_TYPE_INDEX).asString()));
            relationshipType.setDestination(entities.get(relationship.values().get(RelationshipType.DESTINATION_ENTITY_TYPE_INDEX).asString()));
            relationshipTypes.add(relationshipType);
        });
        return relationshipTypes;
    }

    private HashMap<String, Integer> getRelationshipsCardinality(ArrayList<Record> relationshipsCardinality){
        HashMap<String, Integer> maxCardinality = new HashMap<>();
        relationshipsCardinality.forEach(relationshipCardinality -> maxCardinality.put(relationshipCardinality.values().get(1).asString(), relationshipCardinality.values().get(2).asInt()));
        return maxCardinality;
    }

    public String getName() {
        return name;
    }

    public HashMap<String, EntityType> getEntities() {
        return entities;
    }

    public ArrayList<RelationshipType> getRelationships() {
        return relationships;
    }

    @Override
    public String toString() {
        return "GraphSchema{" +
                "name=" + name +
                ",entities=" + entities.values() +
                ",\nrelationships=" + relationships +
                "}";
    }

}

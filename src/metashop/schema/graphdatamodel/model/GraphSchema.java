package metashop.schema.graphdatamodel.model;

import org.neo4j.driver.Record;
import org.neo4j.driver.types.Node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

public class GraphSchema{

    private final String name;
    private final HashMap<String, EntityType> entities;
    private final ArrayList<RelationshipType> relationships;
    private static final int ORIGIN_ENTITY_TYPE_INDEX = 0;
    private static final int DESTINATION_ENTITY_TYPE_INDEX = 1;


    public GraphSchema(String name, ArrayList<Record> nodes, ArrayList<Record> relationships) {
        this.name = name;
        this.entities = addEntities(nodes);
        this.relationships = addRelationships(relationships);
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
            if (!entityTypes.containsKey(entityType.getName()))
                entityTypes.put(entityType.getName(), entityType);
        });
        return entityTypes;
    }

    private ArrayList<RelationshipType> addRelationships(ArrayList<Record> relationships){
        ArrayList<RelationshipType> relationshipTypes = new ArrayList<>();
        relationships.forEach((Record relationship) -> {
            ArrayList<EntityType> originAndDestination = retrieveOriginAndDestination(relationship);
            RelationshipType relationshipType = new RelationshipType(originAndDestination.get(ORIGIN_ENTITY_TYPE_INDEX), originAndDestination.get(DESTINATION_ENTITY_TYPE_INDEX), relationship);
            if (!relationshipTypes.contains(relationshipType))
                relationshipTypes.add(relationshipType);
        });
        return relationshipTypes;
    }

    private ArrayList<EntityType> retrieveOriginAndDestination(Record record){
        EntityType origin = entities.get(concatenateLabels(Collections.singleton(concatenateLabels((((Node) record.values().get(ORIGIN_ENTITY_TYPE_INDEX).asObject()).labels())))));
        EntityType destination = entities.get(concatenateLabels(Collections.singleton(concatenateLabels((((Node) record.values().get(DESTINATION_ENTITY_TYPE_INDEX).asObject()).labels())))));
        ArrayList<EntityType> originAndDestination = new ArrayList<>();
        originAndDestination.add(origin);
        originAndDestination.add(destination);
        return originAndDestination;
    }

    private String concatenateLabels(Iterable<String> labels){
        return String.join("", labels);
    }


    @Override
    public String toString() {
        return "\nGraphSchema{" +
                "name='" + name + '\'' +
                ",\n\n entities=" + entities.values() +
                ",\n\n relationships=" + relationships +
                "}\n";
    }

}

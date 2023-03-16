package metashop.schema.graphdatamodel.model;

import org.neo4j.driver.Record;
import org.neo4j.driver.types.Node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class GraphSchema{

    private final String name;
    private final HashMap<String, EntityType> entities;
    private final ArrayList<RelationshipType> relationships;
    private int entityTypeStructuralVariationIndex = 1;
    private int relationshipTypeStructuralVariationIndex = 1;
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
    private ArrayList<RelationshipType> addRelationships(ArrayList<Record> relationships){
        ArrayList<RelationshipType> relationshipTypes = new ArrayList<>();
        relationships.forEach((Record relationship) -> {
            RelationshipType relationshipType = new RelationshipType(relationship);
            if (!relationshipTypes.contains(relationshipType)) {
                // Obtengo los nombres de los nodos origen y destino para buscarlos en la colección de EntityType
                String origin = String.join("", (Collections.singleton(String.join("", ((((Node) relationship.values().get(ORIGIN_ENTITY_TYPE_INDEX).asObject()).labels()))))));
                String destination = String.join("", (Collections.singleton(String.join("", ((((Node) relationship.values().get(DESTINATION_ENTITY_TYPE_INDEX).asObject()).labels()))))));
                ArrayList<EntityType> originAndDestination = getOriginAndDestination(origin, destination);
                // Seteo estos dos atributos aquí en vez de al crear la relación, ya que así me ahorro estas búsquedas si la relación ya existe.
                relationshipType.setOrigin(originAndDestination.get(ORIGIN_ENTITY_TYPE_INDEX));
                relationshipType.setDestination(originAndDestination.get(DESTINATION_ENTITY_TYPE_INDEX));
                relationshipTypes.add(relationshipType);
            }
        });
        return relationshipTypes;
    }

    /**
     * Método para obtener los EntityType origen y destino de la relación
     * @see EntityType
     * @param origin Nombre del EntityType origen
     * @param destination Nombre del EntityType destino
     * @return ArrayList de EntityType
     */
    private ArrayList<EntityType> getOriginAndDestination(String origin, String destination){
        EntityType originEntity = entities.get(origin);
        EntityType destinationEntity = entities.get(destination);
        ArrayList<EntityType> originAndDestination = new ArrayList<>();
        originAndDestination.add(originEntity);
        originAndDestination.add(destinationEntity);
        return originAndDestination;
    }

    @Override
    public String toString() {
        return "GraphSchema{" +
                "name='" + name +
                ",entities=" + entities.values() +
                ",relationships=" + relationships +
                "}";
    }

}

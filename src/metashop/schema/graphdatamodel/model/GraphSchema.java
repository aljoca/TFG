package metashop.schema.graphdatamodel.model;

import org.neo4j.driver.*;
import org.neo4j.driver.Record;

import java.util.ArrayList;

public class GraphSchema{

    private final String name;
    private ArrayList<EntityType> entities;
    private ArrayList<RelationshipType> relationships;


    public GraphSchema(String name, ArrayList<Record> nodes, ArrayList<Record> relationships) {
        this.name = name;
        this.entities = addEntities(nodes);
        this.relationships = addRelationships(relationships);
    }

    private ArrayList<EntityType> addEntities(ArrayList<Record> nodes){
        ArrayList<EntityType> entityTypes = new ArrayList<>();
        nodes.forEach((Record node) -> {
            EntityType entityType = new EntityType(node);
            if (!entityTypes.contains(entityType))
                entityTypes.add(entityType);
        });
        return entityTypes;
    }

    private ArrayList<RelationshipType> addRelationships(ArrayList<Record> relationships){
        ArrayList<RelationshipType> relationshipTypes = new ArrayList<>();
        relationships.forEach((Record relationship) -> {
            RelationshipType relationshipType = new RelationshipType(relationship);
            if (!relationshipTypes.contains(relationshipType))
                relationshipTypes.add(relationshipType);
        });
        return relationshipTypes;
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

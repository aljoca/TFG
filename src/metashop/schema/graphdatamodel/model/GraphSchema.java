package metashop.schema.graphdatamodel.model;

import org.neo4j.driver.Record;

import java.util.ArrayList;

public class GraphSchema {

    private String name;
    private ArrayList<EntityType> entities;
    private ArrayList<RelationshipType> relationships;

    public GraphSchema(String name) {
        this.name = name;
        this.entities = new ArrayList<>();
        this.relationships = new ArrayList<>();
    }

    public void addNodes(ArrayList<Record> nodes){
        this.entities = nodes;
    }

//    public static EntityType generateEntity(Record record){
//        EntityType entityType = new EntityType(record);
//        entityType.setLabels(entityType.generateEntityLabels(record));
//
//    }
}

package metashop.uschema;

import metashop.graphdatamodel.EntityType;
import metashop.graphdatamodel.GraphSchemaModel;
import metashop.graphdatamodel.RelationshipType;
import metashop.uschema.entities.UEntityType;
import metashop.uschema.entities.UEntityTypeMultiLabeled;
import metashop.uschema.entities.UEntityTypeSingleLabeled;
import metashop.uschema.features.UReference;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class USchemaModel {

    private final String uName;
    private final HashMap<String, UEntityType> uEntities;
    private final HashMap<String, URelationshipType> uRelationships;
    private final GraphSchemaModel graphSchemaModel;

    public USchemaModel(GraphSchemaModel graphSchemaModel) {
        this.graphSchemaModel = graphSchemaModel;
        this.uName = graphSchemaModel.getName();
        this.uEntities = new HashMap<>();
        this.uRelationships = new HashMap<>();
        fillUEntities(graphSchemaModel.getEntities());
        processRelationships(graphSchemaModel.getRelationships());
    }

    private void fillUEntities(HashMap<String, EntityType> graphModelEntities){
        graphModelEntities.forEach((entityName, entityType) -> processEntity(entityType));
    }

    private void processRelationships(List<RelationshipType> graphModelRelationships){
        // Para cada relación del grafo vamos a crear una relación en USchema y una referencia.
        graphModelRelationships.forEach(relationship -> {
            String relationshipName = relationship.getName();
            // Creamos el objeto relación de USchema y lo añadimos a la colección de relaciones
            URelationshipType uRelationshipType = new URelationshipType(relationship);
            uRelationships.put(relationshipName, uRelationshipType);
            // Creamos la referencia. Para ello, la nombramos como la relación, le pasamos la entidad destino, la structural variation de la relación y su máxima cardinalidad.
            // Además, añadimos la referencia a la lista de features de la entidad origen de la relación.
            UReference uReference = new UReference(relationship.getName(), uEntities.get(relationship.getDestination().getName()), uRelationshipType.getuStructuralVariation(), relationship.getMaxCardinality());
            uEntities.get(relationship.getOrigin().getName()).addReference(uReference);
        });

    }

    private void processEntity(EntityType entityType){

        // Si la entidad solo tiene una etiqueta, compruebo si existe en la colección de entidades.
        // Si no existe, la añado a la colección de entidades como una UEntityTypeSingleLabeled
        if (entityType.getLabels().size() == 1) {
            if (!this.uEntities.containsKey(entityType.getName()))
                this.uEntities.put(entityType.getName(), new UEntityTypeSingleLabeled(entityType.getName(), entityType));
        }
        else {
            // En el caso de que tengamos una entidad con varias etiquetas, primero vamos a ver si hay que crear alguna UEntityTypeSingleLabeled
            List<UEntityType> parentEntities = new LinkedList<>();
            entityType.getLabels().forEach(label -> {
                final String labelName = label.getName();
                    /*
                        Esto puede pasar, por ejemplo, si primero viene una entidad con la etiqueta "Actor" y seguidamente
                        llega otra con las etiquetas "Actor" y "Director".
                        Si no existe, creo la entidad, la añado a la colección de entidades y a la lista de entidades "padre".
                        Si existe, solamente la añado a la lista de entidades "padre".
                    */
                if (!this.uEntities.containsKey(labelName)) {
                    UEntityType uEntityType = new UEntityTypeSingleLabeled(labelName, graphSchemaModel.getEntities().get(labelName));
                    parentEntities.add(uEntityType);
                    this.uEntities.put(label.getName(), uEntityType);
                } else {
                    parentEntities.add(this.uEntities.get(labelName));
                }
            });
            // Una vez se han creado las entidades y/o se han añadido a las entidades padre, creamos nuestro UEntityTypeMultiLabeled
            this.uEntities.put(entityType.getName(), new UEntityTypeMultiLabeled(entityType.getName(), entityType, parentEntities));
        }
    }

    public HashMap<String, UEntityType> getUEntities() {
        return uEntities;
    }

    public HashMap<String, URelationshipType> getuRelationships() {
        return uRelationships;
    }

    @Override
    public String toString() {
        return "USchemaModel{" +
                "uName='" + uName + '\'' +
                ", \nuEntities=" + uEntities +
                ", \nuRelationships=" + uRelationships +
                '}';
    }
}

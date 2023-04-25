package metashop;

import metashop.uschema.URelationshipType;
import org.neo4j.driver.Record;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class MySqlMigrationUtils {

    /**
     * Método para calcular la cardinalidad total de una relación.
     *
     * @param relationshipTypes HashMap de con el nombre de la relación y su tipo correspondiente en USchema.
     * @param incomingRelationships Relaciones que entran a un nodo con su cardinalidad correspondiente.
     * @param outgoingRelationships Relaciones que salen de un nodo con su cardinalidad correspondiente.
     * @return HashMap con el nombre de la relación y su cardinalidad total.
     */
    public static HashMap<String, String> calculateRelationshipCardinality(HashMap<String, URelationshipType> relationshipTypes, ArrayList<Record> incomingRelationships, ArrayList<Record> outgoingRelationships){
        HashMap<String, String> relationshipsCardinality = new HashMap<>();
        // Obtengo la cardinalidad de las relaciones entrantes, es decir, el número máximo de relaciones que del mismo tipo de relación que llegan a un nodo.
        HashMap<String, Integer> incomingRel = getRelationshipCardinality(incomingRelationships);
        // Obtengo la cardinalidad de las relaciones salientes, es decir, el número máximo de relaciones que del mismo tipo de relación que salen de un nodo.
        HashMap<String, Integer> outgoingRel = getRelationshipCardinality(outgoingRelationships);

        // Para cada una de las relaciones compruebo su cardinalidad saliente y entrante.
        outgoingRel.keySet().forEach(relationship -> {
            int out = outgoingRel.get(relationship);
            int in = incomingRel.get(relationship);
            if ((out == 1 && in == 1) && relationshipTypes.get(relationship).getuStructuralVariation().getAttributes().isEmpty()){
                relationshipsCardinality.put(relationship, "1:1");
            }
            else if ((out == 1 && in > 1) && relationshipTypes.get(relationship).getuStructuralVariation().getAttributes().isEmpty()){
                relationshipsCardinality.put(relationship, "N:1");
            }
            // Para un tipo de relación, si se cumple que del nodo origen solo sale más de una relación de ese tipo (out > 1) y para el nodo destino llega una relación de ese tipo (in == 1)
            // y además se cumple que la relación no tiene atributos, la cardinalidad es 1:N
            else if (((out > 1 && in == 1)) && relationshipTypes.get(relationship).getuStructuralVariation().getAttributes().isEmpty()){
                relationshipsCardinality.put(relationship, "1:N");
            }
            else {
                // Si no se cumple ninguno de los casos anteriores, inferimos que la cardinalidad es de muchos a muchos.
                relationshipsCardinality.put(relationship, "N:M");
            }
        });
        return  relationshipsCardinality;
    }

    /**
     * Método para obtener la cardinalidad de una relación de entrada o salida.
     *
     * @param relationshipsCardinality Relación con su cardinalidad.
     * @return Relaciones con su cardinalidad. Dependiendo del parámetro del método, devuelve la cardinalidad de salida o de entrada de una relación.
     */
    private static HashMap<String, Integer> getRelationshipCardinality(ArrayList<Record> relationshipsCardinality){
        HashMap<String, Integer> relationshipCardinality = new HashMap<>();
        relationshipsCardinality.forEach(relationship -> relationshipCardinality.put(relationship.values().get(1).asString(), relationship.values().get(2).asInt()));
        return relationshipCardinality;
    }

}

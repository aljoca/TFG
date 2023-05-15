package metashop;

import metashop.graphdatamodel.GraphSchemaModel;
import metashop.graphdatamodel.Label;
import metashop.uschema.USchemaModel;
import metashop.uschema.entities.UEntityType;
import metashop.uschema.features.UReference;
import org.apache.commons.lang3.StringUtils;
import org.neo4j.driver.Record;
import org.neo4j.driver.Value;
import org.neo4j.driver.internal.InternalNode;
import java.sql.*;

import org.neo4j.driver.internal.value.DateValue;
import org.neo4j.driver.internal.value.ListValue;
import org.neo4j.driver.types.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Clase para la migración exclusiva de datos.
 */
public class MySqlDataMigrator {


    /**
     * Método para migrar los datos de Neo4J a MySQL.
     *
     * @param builderUSchemaModel Modelo USchema con el que realizar la migración.
     */
    public static void migrateDataToMySql(HashMap<String, String> relationshipsCardinality, USchemaModel builderUSchemaModel, GraphSchemaModel graphSchemaModel){
        // Recorro la lista de entidades para migrar los datos de las tablas "primitivas".
        for (UEntityType uEntity: builderUSchemaModel.getUEntities().values()) {
            // Para cada entidad, obtengo las etiquetas para poder hacer la búsqueda en Neo4J.
            ArrayList<Label> entityLabels = graphSchemaModel.getEntities().get(uEntity.getName()).getLabels();
            // Realizo la migración de los datos de las entidades.
            MySqlDataMigrator.migrateEntityData(uEntity.getName(), GraphMigrator.getDataEntity(MySqlMigrationUtils.getLabels(entityLabels), entityLabels.size()));
        }
        // Recorro la lista de entidades para migrar los datos de las relaciones.
        for (UEntityType uEntity: builderUSchemaModel.getUEntities().values()) {
            // Para cada entidad, obtengo las etiquetas para poder hacer la búsqueda en Neo4J.
            ArrayList<Label> entityLabels = graphSchemaModel.getEntities().get(uEntity.getName()).getLabels();
            final ArrayList<UReference> references = uEntity.getUStructuralVariation().getReferences();
            // Por cada referencia de la entidad origen, realizo la migración de datos.
            for (UReference uReference: references) {
                // Obtengo las etiquetas de la entidad destino para poder hacer la búsqueda en Neo4J.
                ArrayList<Label> uReferenceLabels = graphSchemaModel.getEntities().get(uReference.getUEntityTypeDestination().getName()).getLabels();
                String relationshipName = "_" + StringUtils.lowerCase(uReference.getName());
                // Obtengo los datos a migrar de las relaciones.
                ArrayList<Record> relationships = GraphMigrator.getDataRelationships(uReference.getName(), MySqlMigrationUtils.getLabelsDoubleDot(entityLabels), MySqlMigrationUtils.getLabelsDoubleDot(uReferenceLabels),
                        MySqlMigrationUtils.getLabels(entityLabels), MySqlMigrationUtils.getLabels(uReferenceLabels), entityLabels.size(), uReferenceLabels.size());
                // Compruebo qué caso se está dando, consultando la entrada de la referencia concreta en el mapa de cardinalidad.
                switch (relationshipsCardinality.get(uReference.getName())) {
                    case "1:1", "N:1" -> MySqlDataMigrator.migrateRelationshipData1To1(uEntity.getName(), relationships, relationshipName);
                    case "1:N" -> MySqlDataMigrator.migreateRelationshipData1ToN(uReference.getUEntityTypeDestination().getName(), relationships, relationshipName);
                    case "N:M" -> {
                        final String tableName = uEntity.getName() + relationshipName + "_" + uReference.getUEntityTypeDestination().getName();
                        MySqlDataMigrator.migrateRelationshipDataNToM(tableName, relationships, relationshipName);
                    }
                }
            }
        }
    }

    /**
     * Método para migrar los datos de un tipo de entidad.
     *
     * @param tableName Nombre de la tabla a crear.
     * @param entities Entidades del mismo tipo que el nombre de la tabla.
     */
    public static void migrateEntityData(String tableName, ArrayList<Record> entities){
        try {
            Statement stmt= GraphMigrator.con.createStatement();
            for (Record entity: entities) {
                InternalNode entityNode = ((InternalNode) entity.values().get(0).asNode());
                ArrayList<String> attributes = new ArrayList<>();
                ArrayList<String> values = new ArrayList<>();
                entityNode.keys().forEach(key -> {
                    attributes.add(key);
                    Value value = entityNode.get(key);
                    if (value instanceof ListValue){
                        values.add(createJsonAttributeValue((ListValue) value, key));
                    }
                    else {
                        // Esto es necesario porque cuando obtengo las fechas en Neo4J no puedo formatearlas.
                        // Así compruebo si el atributo que estoy migrando es de tipo Date. Lo malo de esto es que
                        // lo tengo que comprobar por cada atributo.
                        if (value instanceof DateValue) {
                            values.add("'" + value + "'");
                        }
                        else values.add(String.valueOf(value));

                    }
                });
                System.out.println("INSERT INTO " + tableName + "(" + String.join(",",attributes) + ") VALUES (" + String.join(",", values) + ");");
                stmt.execute("INSERT INTO " + tableName + "(" + String.join(",",attributes) + ") VALUES (" + String.join(",", values) + ");");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Método para crear un json con la información del atributo colección.
     *
     * @param arrayValues Lista de valores del atributo.
     * @param attributeName Nombre del atributo.
     * @return JSON con los valores del atributo.
     */
    private static String createJsonAttributeValue(ListValue arrayValues, String attributeName) {
        ArrayList<String> jsonList = new ArrayList<>();
        AtomicInteger attributeCount = new AtomicInteger(1);
        arrayValues.values().forEach(value ->{
            jsonList.add("\"" + attributeName + attributeCount + "\": " + value);
            attributeCount.getAndIncrement();
        });
        return "'{" + String.join(",", jsonList) + "}'";
    }


    /**
     * Método para obtener el valor de los atributos de un nodo.
     *
     * @see Node
     * @param nodeAttributes Nombres de los atributos del nodo
     * @param relationshipName Parámetro para nombrar al atributo referencia de una tabla.
     * @param node Nodo del que se quiere extraer los valores de los atributos.
     * @return ArrayList de valores de atributos.
     */
    private static ArrayList<String> getAttributeValue(ArrayList<String> nodeAttributes, String relationshipName, Node node){
        ArrayList<String> attributesValues = new ArrayList<>();
        for (String attribute: nodeAttributes) {
            attributesValues.add(attribute + relationshipName + " = " + node.get(attribute));
        }
        return attributesValues;
    }

    /**
     * Método para obtener el valor de los atributos de un nodo.
     *
     * @see Node
     * @param nodeAttributes Nombres de los atributos del nodo.
     * @param node Nodo para el que se quiere extraer los valores de los atributos.
     * @return ArrayList de valores de atributos
     */
    private static ArrayList<String> getAttributeValue(ArrayList<String> nodeAttributes, Node node){
        return getAttributeValue(nodeAttributes, "", node);
    }

    /**
     * Método para insertar los valores de las referencias en una tabla entidad.
     *
     * @see Node
     * @param setNode Nodo del que se obtienen los atributos con sus respectivos valores para incluirlos en la sentencia SET.
     * @param whereNode Nodo del que se obtienen los atributos con sus respectivos valores para incluirlos en la sentencia WHERE.
     * @param tableName Nombre de la tabla a actualizar.
     * @param relationshipName Nombre de la relación para la que se está haciendo el update.
     * @return Sentencia UPDATE para la inserción de los datos.
     */
    private static String updateTable(Node setNode, Node whereNode, String tableName, String relationshipName){
        // Obtengo los atributos a setear con sus valores y las condiciones para encontrar la entrada a actualizar.
        ArrayList<String> setAttributes = getAttributeValue((ArrayList<String>) MySqlSchemaGenerator.entityPrimaryKeys.get(GraphMigrator.appendLabels(setNode)), relationshipName, setNode);
        ArrayList<String> whereConditions = getAttributeValue((ArrayList<String>) MySqlSchemaGenerator.entityPrimaryKeys.get(GraphMigrator.appendLabels(whereNode)), whereNode);
        String set = String.join(",", setAttributes);
        String where = String.join(" AND ", whereConditions);
        return "UPDATE " + tableName + " SET " + set + " WHERE " + where + ";";
    }

    /**
     * Método para migrar los datos de una relación 1 a 1.
     *
     * @param tableName
     * @param relationships
     * @param relationshipName
     */
    public static void migrateRelationshipData1To1(String tableName, ArrayList<Record> relationships, String relationshipName){
        try {
            Statement stmt= GraphMigrator.con.createStatement();
            for (Record relationship: relationships) {
                Node originNode =  relationship.values().get(0).asNode();
                Node destinationNode =  relationship.values().get(1).asNode();
                stmt.execute(updateTable(destinationNode, originNode, tableName, relationshipName));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Método para la migración
     *
     * @param tableName Nombre de la tabla a la que se van a insertar los datos.
     * @param relationships
     * @param relationshipName
     */
    public static void migreateRelationshipData1ToN(String tableName, ArrayList<Record> relationships, String relationshipName){
        try {
            Statement stmt= GraphMigrator.con.createStatement();
            for (Record relationship: relationships) {
                Node originNode =  relationship.values().get(0).asNode();
                Node destinationNode =  relationship.values().get(1).asNode();
                stmt.execute(updateTable(originNode, destinationNode, tableName, relationshipName));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *  Método para migrar los datos de una relación de muchos a muchos.
     * @param tableName Nombre de la tabla a la que se van a hacer los inserts.
     * @param relationships Relaciones que se quieren migrar. Estas relaciones son siempre de un mismo tipo.
     * @param relationshipName Nombre de la relación que se va a migrar
     */
    public static void migrateRelationshipDataNToM(String tableName, ArrayList<Record> relationships, String relationshipName){
        try {
            Statement stmt= GraphMigrator.con.createStatement();
            // Recorremos las relaciones. Todas las relaciones son del mismo tipo (relationshipName).
            for (Record relacion: relationships) {
                // Obtengo el nodo origen y destino de la relación que estamos recorriendo.
                Node originNode =  relacion.values().get(0).asNode();
                Node destinationNode =  relacion.values().get(1).asNode();

                // Obtengo las primaryKeys del nodo origen y destino
                ArrayList<String> originPrimaryKeys = (ArrayList<String>) MySqlSchemaGenerator.entityPrimaryKeys.get(GraphMigrator.appendLabels(originNode));
                ArrayList<String> destionationPrimaryKeys = (ArrayList<String>) MySqlSchemaGenerator.entityPrimaryKeys.get(GraphMigrator.appendLabels(destinationNode));

                ArrayList<String> originPrimaryKeyValues = new ArrayList<>();
                ArrayList<String> destinationPrimaryKeyValues = new ArrayList<>();
                ArrayList<String> destinationPrimaryKeyWithoutValues = new ArrayList<>();

                // Recorro la lista de primaryKeys para cada  y guardo sus valores
                for (String primaryKey: originPrimaryKeys) {
                    originPrimaryKeyValues.add(originNode.get(primaryKey).toString());
                }
                for (String primaryKey: destionationPrimaryKeys) {
                    destinationPrimaryKeyValues.add(destinationNode.get(primaryKey).toString());
                    destinationPrimaryKeyWithoutValues.add(primaryKey + relationshipName);
                }
                ArrayList<String> relationshipAttributes = new ArrayList<>();
                ArrayList<String> relationshipAttributesValues = new ArrayList<>();

                // Recorro la lista de atributos de la relación y guardo sus valores
                for (String relationshipAttribute: MySqlSchemaGenerator.relationshipAttributes.get(relationshipName)) {
                    relationshipAttributes.add(relationshipAttribute);
                    Value attributeValue = relacion.values().get(2).get(relationshipAttribute);
                    // Si es una colección, trato el atributo como un json
                    if (attributeValue instanceof ListValue){
                        relationshipAttributesValues.add(createJsonAttributeValue((ListValue) attributeValue, relationshipAttribute));
                    }
                    else relationshipAttributesValues.add(attributeValue.toString());
                }
                System.out.println(getInsertSentence(tableName, originPrimaryKeys, destinationPrimaryKeyWithoutValues,
                        relationshipAttributes, originPrimaryKeyValues, destinationPrimaryKeyValues, relationshipAttributesValues));
                stmt.execute(getInsertSentence(tableName, originPrimaryKeys, destinationPrimaryKeyWithoutValues,
                        relationshipAttributes, originPrimaryKeyValues, destinationPrimaryKeyValues, relationshipAttributesValues));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *
     * @param tableName
     * @param originPrimaryKeysColumnsWithoutValue
     * @param destinationPrimaryKeysColumnsWithoutValue
     * @param relationshipAttributes
     * @param originPrimaryKeysColumns
     * @param destinationPrimaryKeysColumns
     * @param relationshipAttributesValues
     * @return
     */
    private static String getInsertSentence(String tableName, ArrayList<String> originPrimaryKeysColumnsWithoutValue, ArrayList<String> destinationPrimaryKeysColumnsWithoutValue,
                                            ArrayList<String> relationshipAttributes, ArrayList<String> originPrimaryKeysColumns, ArrayList<String> destinationPrimaryKeysColumns,
                                            ArrayList<String> relationshipAttributesValues){
        if (relationshipAttributes.isEmpty())
            return "INSERT INTO " + tableName + "(" + String.join(",", originPrimaryKeysColumnsWithoutValue) + "," +
                    String.join(",", destinationPrimaryKeysColumnsWithoutValue) + ") VALUES (" + String.join(",",originPrimaryKeysColumns)
                    + "," + String.join(",", destinationPrimaryKeysColumns) + ");";
        else return "INSERT INTO " + tableName + "(" + String.join(",", originPrimaryKeysColumnsWithoutValue) + "," +
                String.join(",", destinationPrimaryKeysColumnsWithoutValue) + "," +  String.join(",", relationshipAttributes) + ") VALUES (" + String.join(",",originPrimaryKeysColumns)
                + "," + String.join(",", destinationPrimaryKeysColumns) + "," +String.join(",", relationshipAttributesValues) + ");";

    }

}

package metashop;

import org.apache.commons.lang3.StringUtils;
import org.neo4j.driver.Record;
import org.neo4j.driver.Value;
import org.neo4j.driver.internal.InternalNode;
import java.sql.*;

import org.neo4j.driver.internal.value.ListValue;
import org.neo4j.driver.types.Node;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class MySqlDataMigrator {

    public static void migrarDatosPrueba(String tableName, ArrayList<Record> usuarios){
        try {
            Statement stmt=MetaShopSchema.con.createStatement();
            for (Record usuario: usuarios) {
                InternalNode user = ((InternalNode) usuario.values().get(0).asNode());
                ArrayList<String> attributes = new ArrayList<>();
                ArrayList<String> values = new ArrayList<>();
                user.keys().forEach(key -> {
                    attributes.add(key);
                    Value value = user.get(key);
                    if (value instanceof ListValue){
                        values.add(createJsonAttributeValue((ListValue) value, key));
                    }
                    else values.add(String.valueOf(value));
                });
                stmt.execute("INSERT INTO " + tableName + "(" + String.join(",",attributes) + ") VALUES (" + String.join(",", values) + ");");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    private static String createJsonAttributeValue(ListValue arrayValues, String attributeName) {
        ArrayList<String> jsonList = new ArrayList<>();
        AtomicInteger attributeCount = new AtomicInteger(1);
        arrayValues.values().forEach(value ->{
            jsonList.add("\"" + attributeName + attributeCount + "\": " + value);
            attributeCount.getAndIncrement();
        });
        return "'{" + String.join(",", jsonList) + "}'";
    }


    private static ArrayList<String> getAttributeValue(ArrayList<String> nodeAttributes, String relationshipName, Node node){
        ArrayList<String> attributesValues = new ArrayList<>();
        for (String attribute: nodeAttributes) {
            attributesValues.add(attribute + relationshipName + " = " + node.get(attribute));
        }
        return attributesValues;
    }


    private static ArrayList<String> getAttributeValue(ArrayList<String> nodeAttributes, Node node){
        return getAttributeValue(nodeAttributes, "", node);
    }

    private static String updateTable(Node setNode, Node whereNode, String tableName, String relationshipName){
        ArrayList<String> setAttributes = getAttributeValue((ArrayList<String>) MySqlSchemaGenerator.tablePrimaryKeys.get(MetaShopSchema.appendLabels(setNode)), relationshipName, setNode);
        ArrayList<String> whereConditions = getAttributeValue((ArrayList<String>) MySqlSchemaGenerator.tablePrimaryKeys.get(MetaShopSchema.appendLabels(whereNode)), whereNode);
        String set = String.join(",", setAttributes);
        String where = String.join(" AND ", whereConditions);
        return "UPDATE " + tableName + " SET " + set + " WHERE " + where + ";";
    }

    public static void migrarDatosRelaciones1To1(String tableName, ArrayList<Record> relaciones, String relationshipName){
        try {
            Statement stmt=MetaShopSchema.con.createStatement();
            for (Record relacion: relaciones) {
                Node originNode =  relacion.values().get(0).asNode();
                Node destinationNode =  relacion.values().get(1).asNode();
                stmt.execute(updateTable(destinationNode, originNode, tableName, relationshipName));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void migrarDatosRelaciones1ToN(String tableName, ArrayList<Record> relaciones, String relationshipName){
        try {
            Statement stmt=MetaShopSchema.con.createStatement();
            for (Record relacion: relaciones) {
                Node originNode =  relacion.values().get(0).asNode();
                Node destinationNode =  relacion.values().get(1).asNode();
                stmt.execute(updateTable(originNode, destinationNode, tableName, relationshipName));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public static void migrarDatosRelacionesNToM(String tableName, ArrayList<Record> relaciones, String relationshipName){
        try {
            Statement stmt=MetaShopSchema.con.createStatement();
            for (Record relacion: relaciones) {
                Node originNode =  relacion.values().get(0).asNode();
                Node destinationNode =  relacion.values().get(1).asNode();
                ArrayList<String> originPrimaryKeys = (ArrayList<String>) MySqlSchemaGenerator.tablePrimaryKeys.get(MetaShopSchema.appendLabels(originNode));
                ArrayList<String> destionationPrimaryKeys = (ArrayList<String>) MySqlSchemaGenerator.tablePrimaryKeys.get(MetaShopSchema.appendLabels(destinationNode));

                ArrayList<String> originPrimaryKeysColumns = new ArrayList<>();
                ArrayList<String> destinationPrimaryKeysColumns = new ArrayList<>();
                ArrayList<String> originPrimaryKeysColumnsWithoutValue = new ArrayList<>();
                ArrayList<String> destinationPrimaryKeysColumnsWithoutValue = new ArrayList<>();

                // Recorro la lista de primaryKeys para cada relación
                for (String primaryKey: originPrimaryKeys) {
                    originPrimaryKeysColumns.add(originNode.get(primaryKey).toString());
                    originPrimaryKeysColumnsWithoutValue.add(primaryKey);
                }
                for (String primaryKey: destionationPrimaryKeys) {
                    destinationPrimaryKeysColumns.add(destinationNode.get(primaryKey).toString());
                    destinationPrimaryKeysColumnsWithoutValue.add(primaryKey + relationshipName);
                }
                ArrayList<String> relationshipAttributes = new ArrayList<>();
                ArrayList<String> relationshipAttributesValues = new ArrayList<>();
                for (String relationshipAttribute: MySqlSchemaGenerator.tableRelationshipAttributes.get(relationshipName)) {
                    relationshipAttributes.add(relationshipAttribute);
                    Value attributeValue = relacion.values().get(2).get(relationshipAttribute);
                    if (attributeValue instanceof ListValue){
                        relationshipAttributesValues.add(createJsonAttributeValue((ListValue) attributeValue, relationshipAttribute));
                    }
                    else relationshipAttributesValues.add(attributeValue.toString());
                }
                System.out.println(getInsertSentence(tableName, originPrimaryKeysColumnsWithoutValue, destinationPrimaryKeysColumnsWithoutValue,
                        relationshipAttributes, originPrimaryKeysColumns, destinationPrimaryKeysColumns, relationshipAttributesValues));
                stmt.execute(getInsertSentence(tableName, originPrimaryKeysColumnsWithoutValue, destinationPrimaryKeysColumnsWithoutValue,
                        relationshipAttributes, originPrimaryKeysColumns, destinationPrimaryKeysColumns, relationshipAttributesValues));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

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

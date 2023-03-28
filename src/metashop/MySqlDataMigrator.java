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
                StringBuilder originPrimaryKeysColumns = new StringBuilder();
                StringBuilder destinationPrimaryKeysColumns = new StringBuilder();
                StringBuilder originPrimaryKeysColumnsWithoutValue = new StringBuilder();
                StringBuilder destinationPrimaryKeysColumnsWithoutValue = new StringBuilder();
                // Recorro la lista de primaryKeys para cada relación
                for (String primaryKey: originPrimaryKeys) {
                    originPrimaryKeysColumns.append(originNode.get(primaryKey)).append(" , ");
                    originPrimaryKeysColumnsWithoutValue.append(primaryKey).append(",");
                }
                for (String primaryKey: destionationPrimaryKeys) {
                    destinationPrimaryKeysColumns.append(destinationNode.get(primaryKey)).append(",");
                    destinationPrimaryKeysColumnsWithoutValue.append(primaryKey).append(relationshipName).append(",");
                }
                StringBuilder relationshipAttributes = new StringBuilder(",");
                StringBuilder relationshipAttributesValues = new StringBuilder(",");
                for (String relationshipAttribute: MySqlSchemaGenerator.tableRelationshipAttributes.get(relationshipName)) {
                    Value attributeValue = relacion.values().get(2).get(relationshipAttribute);
                    if (attributeValue instanceof ListValue){
                        relationshipAttributesValues.append(createJsonAttributeValue((ListValue) attributeValue, relationshipAttribute));
                    }
                    else relationshipAttributesValues.append(attributeValue).append(",");
                }
                stmt.execute("INSERT INTO " + tableName + "(" + originPrimaryKeysColumnsWithoutValue +
                        StringUtils.chop(destinationPrimaryKeysColumnsWithoutValue.toString()) + StringUtils.chop(relationshipAttributes.toString()) + ") VALUES (" + originPrimaryKeysColumns
                        + StringUtils.chop(destinationPrimaryKeysColumns.toString()) + StringUtils.chop(relationshipAttributesValues.toString()) + ");");

            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}

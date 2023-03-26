package metashop;

import org.apache.commons.lang3.StringUtils;
import org.neo4j.driver.Record;
import org.neo4j.driver.internal.InternalNode;
import java.sql.*;
import org.neo4j.driver.types.Node;

import java.util.ArrayList;

public class MySqlDataMigrator {

    public static void migrarDatosPrueba(String tableName, ArrayList<Record> usuarios){
        try {
            Statement stmt=MetaShopSchema.con.createStatement();
            for (Record usuario: usuarios) {
                InternalNode user = ((InternalNode) usuario.values().get(0).asNode());
                StringBuilder columns = new StringBuilder();
                StringBuilder values = new StringBuilder();
                user.keys().forEach(key -> {columns.append(key).append(",");});
                user.values().forEach(value -> {
                    values.append(value).append(",");
                });
                stmt.execute("INSERT INTO " + tableName + "(" + StringUtils.chop(columns.toString()) + ") VALUES (" + StringUtils.chop(values.toString()) + ");");
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public static void migrarDatosRelaciones1To1(String tableName, ArrayList<Record> relaciones, String relationshipName){
        try {
            Statement stmt=MetaShopSchema.con.createStatement();
            for (Record relacion: relaciones) {
                Node originNode =  relacion.values().get(0).asNode();
                Node destinationNode =  relacion.values().get(1).asNode();
                ArrayList<String> originPrimaryKeys = (ArrayList<String>) MySqlSchemaGenerator.tablePrimaryKeys.get(MetaShopSchema.appendLabels(originNode));
                ArrayList<String> destionationPrimaryKeys = (ArrayList<String>) MySqlSchemaGenerator.tablePrimaryKeys.get(MetaShopSchema.appendLabels(destinationNode));
                StringBuilder originPrimaryKeysColumns = new StringBuilder();
                StringBuilder destinationPrimaryKeysColumns = new StringBuilder();
                // Recorro la lista de primaryKeys para cada relación
                for (String primaryKey: destionationPrimaryKeys) {
                    destinationPrimaryKeysColumns.append(primaryKey).append(relationshipName).append(" = ").append(destinationNode.get(primaryKey)).append(" , ");
                }
                for (String primaryKey: originPrimaryKeys) {
                    originPrimaryKeysColumns.append(primaryKey).append(" = ").append(originNode.get(primaryKey)).append(" AND ");
                }
                String where = StringUtils.substring(originPrimaryKeysColumns.toString(), 0, originPrimaryKeysColumns.length()-5);
                String set = StringUtils.substring(destinationPrimaryKeysColumns.toString(), 0, destinationPrimaryKeysColumns.length()-3);
                stmt.execute("UPDATE " + tableName + "\nSET " + set + "\nWHERE " + where + ";");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public static void migrarDatosRelaciones1ToN(String tableName, ArrayList<Record> relaciones){
        try {
            Statement stmt=MetaShopSchema.con.createStatement();
            for (Record relacion: relaciones) {
                Node originNode =  relacion.values().get(0).asNode();
                Node destinationNode =  relacion.values().get(1).asNode();
                ArrayList<String> originPrimaryKeys = (ArrayList<String>) MySqlSchemaGenerator.tablePrimaryKeys.get(MetaShopSchema.appendLabels(originNode));
                ArrayList<String> destionationPrimaryKeys = (ArrayList<String>) MySqlSchemaGenerator.tablePrimaryKeys.get(MetaShopSchema.appendLabels(destinationNode));
                StringBuilder originPrimaryKeysColumns = new StringBuilder();
                StringBuilder destinationPrimaryKeysColumns = new StringBuilder();
                // Recorro la lista de primaryKeys para cada relación
                for (String primaryKey: originPrimaryKeys) {
                    originPrimaryKeysColumns.append(primaryKey).append("Ref").append(" = ").append(originNode.get(primaryKey)).append(" , ");
                }
                for (String primaryKey: destionationPrimaryKeys) {
                    destinationPrimaryKeysColumns.append(primaryKey).append(" = ").append(destinationNode.get(primaryKey)).append(" AND ");
                }
                String set = StringUtils.substring(originPrimaryKeysColumns.toString(), 0, originPrimaryKeysColumns.length()-3);
                String where = StringUtils.substring(destinationPrimaryKeysColumns.toString(), 0, destinationPrimaryKeysColumns.length()-5);
                stmt.execute("UPDATE " + tableName + "\nSET " + set + "\nWHERE " + where + ";");
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
                String originValue = originPrimaryKeysColumns.toString();
                String destinationValue = StringUtils.substring(destinationPrimaryKeysColumns.toString(), 0, destinationPrimaryKeysColumns.length()-1);
                stmt.execute("INSERT INTO " + tableName + "(" + originPrimaryKeysColumnsWithoutValue + StringUtils.chop(destinationPrimaryKeysColumnsWithoutValue.toString()) + ") VALUES (" + originValue + destinationValue + ");");

            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}

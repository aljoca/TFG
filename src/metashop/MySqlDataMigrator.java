package metashop;

import org.apache.commons.lang3.StringUtils;
import org.neo4j.driver.Record;
import org.neo4j.driver.internal.InternalNode;
import org.neo4j.driver.internal.value.NodeValue;
import org.neo4j.driver.types.Node;

import java.util.ArrayList;

public class MySqlDataMigrator {

    public static void migrarDatosPrueba(String tableName, ArrayList<Record> usuarios){
        for (Record usuario: usuarios) {
            InternalNode user = ((InternalNode) usuario.values().get(0).asNode());
            StringBuilder columns = new StringBuilder();
            StringBuilder values = new StringBuilder();
            user.keys().forEach(key -> {columns.append(key).append(",");});
            user.values().forEach(value -> {
                values.append(value).append(",");
            });
            System.out.println("INSERT INTO " + tableName + "(" + StringUtils.chop(columns.toString()) + ") VALUES (" + StringUtils.chop(values.toString()) + ");");
        }
    }

    public static void migrarDatosRelaciones1To1(String tableName, ArrayList<Record> relaciones){
        for (Record relacion: relaciones) {
            Node originNode =  relacion.values().get(0).asNode();
            Node destinationNode =  relacion.values().get(1).asNode();
            ArrayList<String> originPrimaryKeys = (ArrayList<String>) MySqlSchemaGenerator.tablePrimaryKeys.get(MetaShopSchema.appendLabels(originNode));
            ArrayList<String> destionationPrimaryKeys = (ArrayList<String>) MySqlSchemaGenerator.tablePrimaryKeys.get(MetaShopSchema.appendLabels(destinationNode));
            StringBuilder originPrimaryKeysColumns = new StringBuilder();
            StringBuilder destinationPrimaryKeysColumns = new StringBuilder();
            // Recorro la lista de primaryKeys para cada relación
            for (String primaryKey: destionationPrimaryKeys) {
                destinationPrimaryKeysColumns.append(primaryKey).append("Ref").append(" = ").append(destinationNode.get(primaryKey)).append(" , ");
            }
            for (String primaryKey: originPrimaryKeys) {
                originPrimaryKeysColumns.append(primaryKey).append(" = ").append(originNode.get(primaryKey)).append(" AND ");
            }
            String where = StringUtils.substring(originPrimaryKeysColumns.toString(), 0, originPrimaryKeysColumns.length()-5);
            String set = StringUtils.substring(destinationPrimaryKeysColumns.toString(), 0, destinationPrimaryKeysColumns.length()-3);;
            System.out.println("UPDATE " + tableName + "\nSET " + set + "\nWHERE " + where + ";");
        }
    }

    public static void migrarDatosRelaciones1ToN(String tableName, ArrayList<Record> relaciones){
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
            String where = StringUtils.substring(destinationPrimaryKeysColumns.toString(), 0, destinationPrimaryKeysColumns.length()-5);;
            System.out.println("UPDATE " + tableName + "\nSET " + set + "\nWHERE " + where + ";");
        }
    }

}

package metashop;

import metashop.graphdatamodel.GraphSchemaModel;
import metashop.graphdatamodel.Label;
import metashop.graphdatamodel.type.PrimitiveType;
import metashop.uschema.USchemaModel;
import metashop.uschema.entities.UEntityType;
import metashop.uschema.features.UAttribute;
import metashop.uschema.features.UFeature;
import metashop.uschema.types.UPrimitiveType;
import metashop.uschema.types.UType;
import org.apache.commons.lang3.StringUtils;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class MetaShopSchema implements AutoCloseable{
    private static Driver driver;
    private final static String uri = "bolt://localhost:7687";
    private final static String user = "neo4j";
    private final static String password = "12345678";
    public final static List<String> types = List.of("Double", "Long", "Boolean", "String");


    /**
     * Método para la obtención de todos los nodos existentes en la BBDD
     * @return ArrayList de nodos
     */
    private static ArrayList<Record> getNodes() {
        try (Session session = driver.session()) {
            return session.executeWrite(tx -> {
                Query query = new Query("""
                        CALL apoc.meta.nodeTypeProperties() YIELD nodeLabels, propertyName, propertyTypes, mandatory
                        WITH nodeLabels, collect([propertyName, propertyTypes[0],mandatory]) as type_data_properties
                        RETURN reduce(s = '', x IN nodeLabels | s + x) as nodeName, nodeLabels AS nodeType, type_data_properties AS properties
                        """);
                Result resultNodes = tx.run(query);
                return new ArrayList<>(resultNodes.list());
            });
        }
    }

    /**
     * Método para la obtención de todas las relaciones existentes en la BBDD
     * Esta consulta tiene la particulareidad de que cada elemento contiene el nodo origen, el nodo destino y la relación.
     * Haciendo solo la query de las relaciones sería más costoso, ya que solo vienen las referencias de los nodos relacionados.
     * @return ArrayList de relaciones
     */
    private static ArrayList<Record> getRelationships(){
        try (Session session = driver.session()) {
            return session.executeWrite(tx -> {
                Query query = new Query("""
                        CALL apoc.meta.relTypeProperties() YIELD relType, sourceNodeLabels, targetNodeLabels, propertyName, propertyTypes, mandatory
                        WITH relType,  reduce(s = '', x IN sourceNodeLabels | s + x) as sourceNodeName,
                        reduce(s = '', x IN targetNodeLabels | s + x) as targetNodeName, [val IN collect([propertyName, propertyTypes[0] ,mandatory]) WHERE val[0] IS NOT null] as properties
                        RETURN apoc.text.replace(relType, '[`:]', '') as relType, sourceNodeName, targetNodeName, properties               
                        """);
                Result result = tx.run(query);
                return new ArrayList<>(result.list());
            });
        }
    }

    private static ArrayList<Record> getRelationshipsCardinality(){
        try (Session session = driver.session()) {
            return session.executeWrite(tx -> {
                Query query = new Query("""
                        MATCH (n)-[r]->()
                        WITH id(n) AS id_nodo, labels(n) AS etiquetas, type(r) AS tipo_relacion, COUNT(*) AS cardinalidad
                        UNWIND etiquetas AS etiqueta
                        WITH etiqueta, tipo_relacion, MAX(cardinalidad) AS max_cardinalidad
                        RETURN etiqueta, tipo_relacion, max_cardinalidad            
                        """);
                Result result = tx.run(query);
                return new ArrayList<>(result.list());
            });
        }
    }

    private static ArrayList<Record> getDataNodes(String condition){
        try (Session session = driver.session()) {
            return session.executeWrite(tx -> {
                Query query = new Query("MATCH (n) WHERE (" + condition + ") WITH labels(n) AS Etiquetas, [prop in keys(n) | {name: prop, value: n[prop]}] AS Atributos  RETURN Etiquetas, Atributos");
                Result result = tx.run(query);
                return new ArrayList<>(result.list());
            });
        }
    }

//    private static ArrayList<Record> getDataRelationships(){
//        try (Session session = driver.session()) {
//            return session.executeWrite(tx -> {
//                Query query = new Query("""
//
//                        """);
//                Result result = tx.run(query);
//                return new ArrayList<>(result.list());
//            });
//        }
//    }

    private static void migrateData(GraphSchemaModel graphSchemaModel, USchemaModel uSchemaModel){

        for (String entityName: uSchemaModel.getuEntities().keySet()) {
            StringBuilder condition = new StringBuilder();
            ArrayList<Label> labels = graphSchemaModel.getEntities().get(entityName).getLabels();
            ArrayList<Record> dataNodes;
            if (labels.size() > 1) {
                labels.forEach(label -> condition.append("n:").append(label.getName()).append(" AND "));
                String finalCondition = StringUtils.substring(condition.toString(), 0, condition.length() - 4);
                dataNodes = getDataNodes(finalCondition);
            }
            else {
                dataNodes = getDataNodes("n:" + labels.get(0).getName() + " AND size(labels(n)) < 2");
            }
        }
    }


    @Override
    public void close(){
        driver.close();
    }

    public static void main(String... args) {
        driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
        GraphSchemaModel graphSchema = new GraphSchemaModel("MetaShop", getNodes(), getRelationships(), getRelationshipsCardinality());
        System.out.println(graphSchema);

        USchemaModel uSchemaModel = new USchemaModel(graphSchema);
        System.out.println(uSchemaModel);

        // Creo el esquema en MySQL
        MySqlSchemaGenerator.createMySQLSchemaFromUSchema(uSchemaModel);

        // Aquí tendría que hacer las consultas necesarias para mapear los datos
        migrateData(graphSchema,uSchemaModel);

    }

}
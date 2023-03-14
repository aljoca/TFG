package metashop;

import metashop.schema.graphdatamodel.model.EntityType;
import metashop.schema.graphdatamodel.model.GraphSchema;
import metashop.schema.graphdatamodel.model.Label;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import java.util.ArrayList;



public class MetaShopSchema implements AutoCloseable {
    private static Driver driver;

    public MetaShopSchema(String uri, String user, String password) {
        driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
    }

    @Override
    public void close() throws RuntimeException {
        driver.close();
    }

    /**
     * Método para la obtención de todas las etiquetas existentes en la BBDD
     * @return ArrayList de nodos
     */
    public static ArrayList<Record> getNodes() {
        try (Session session = driver.session()) {
            ArrayList<Record> nodes = session.executeWrite(tx -> {
                Query query = new Query("MATCH (n) RETURN n");
                Result result = tx.run(query);
                return new ArrayList<>(result.list());
            });
            return nodes;
        }
    }

    /**
     * Método para la obtención de todas las relaciones existentes en la BBDD
     * @return ArrayList de relaciones
     */
    public static  ArrayList<Record> getRelationships(){
        try (Session session = driver.session()) {
            ArrayList<Record> relationships = session.executeWrite(tx -> {
                Query query = new Query("MATCH (n)-[r]->() RETURN r");
                Result result = tx.run(query);
                return new ArrayList<>(result.list());
            });
            return relationships;
        }
    }

    public static GraphSchema generateGraphSchema(MetaShopSchema schema){
        GraphSchema graphSchema = new GraphSchema("GraphSchemaFromNeo4J");

        ArrayList<Record> nodes = getNodes();
        ArrayList<Record> relationships = getRelationships();

        graphSchema.addNodes(nodes);
        graphSchema.addRelationships(relationships);

        return graphSchema;
    }


    public static void main(String... args) {

        // Conexión a la base de datos
        try (MetaShopSchema schema = new MetaShopSchema("bolt://localhost:7687", "neo4j", "12345678")) {
            GraphSchema graphSchema = generateGraphSchema(schema);
        }
    }



    //    /**
//     * Método para extraer los campos de cada clase del esquema
//     */
//    static Map<String, Map<String, String>> createSchema(List<Class> entities) throws Exception {
//        Map<String, Map<String, String>> result = new HashMap<>();
//        for (Class entity: entities) {
//            Map<String, String> mappedClass = new HashMap<>();
//            for (Field f: entity.getDeclaredFields()) {
//                f.setAccessible(true);
//                mappedClass.put(f.getName(), f.getType().getSimpleName());
//            }
//            result.put(entity.getSimpleName(), mappedClass);
//        }
//        return result;
//    }
}
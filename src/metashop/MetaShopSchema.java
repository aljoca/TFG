package metashop;

import metashop.schema.graphdatamodel.model.GraphSchema;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;

import java.util.ArrayList;

public abstract class MetaShopSchema implements AutoCloseable{
    private static Driver driver;
    private final static String uri = "bolt://localhost:7687";
    private final static String user = "neo4j";
    private final static String password = "12345678";


    /**
     * Método para la obtención de todas las etiquetas existentes en la BBDD
     * @return ArrayList de nodos
     */
    private static ArrayList<Record> getNodes() {
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
    private static ArrayList<Record> getRelationships(){
        try (Session session = driver.session()) {
            ArrayList<Record> relationships = session.executeWrite(tx -> {
                Query query = new Query("MATCH (n)-[r]->() RETURN r");
                Result result = tx.run(query);
                return new ArrayList<>(result.list());
            });
            return relationships;
        }
    }

    @Override
    public void close() throws Exception {
        driver.close();
    }

    public static void main(String... args) throws Exception {
        driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
        GraphSchema graphSchema = new GraphSchema("MetaShop", getNodes(), getRelationships());
        System.out.println(graphSchema);
    }

}
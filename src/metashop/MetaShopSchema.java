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
     * Método para la obtención de todos los nodos existentes en la BBDD
     * @return ArrayList de nodos
     */
    private static ArrayList<Record> getNodes() {
        try (Session session = driver.session()) {
            return session.executeWrite(tx -> {
                Query query = new Query("MATCH (n) RETURN n");
                Result result = tx.run(query);
                return new ArrayList<>(result.list());
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
                Query query = new Query("MATCH (n)-[r]->(m) RETURN n,m,r");
                Result result = tx.run(query);
                return new ArrayList<>(result.list());
            });
        }
    }

    @Override
    public void close(){
        driver.close();
    }

    public static void main(String... args) {
        driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
        GraphSchema graphSchema = new GraphSchema("MetaShop", getNodes(), getRelationships());
        System.out.println(graphSchema);
    }

}
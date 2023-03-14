package metashop;

import metashop.schema.graphdatamodel.model.GraphSchema;

public class MetaShopSchema{
    public static void main(String... args) throws Exception {
            // Creo graphSchema, que es quien se encargará de realizar las conexiones a la BBDD
        try (GraphSchema graphSchema = new GraphSchema("MetaShop")) {
            System.out.println(graphSchema);
        }
    }
}
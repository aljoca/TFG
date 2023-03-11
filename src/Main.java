import metashop.schema.MetaShopSchema;
import metashop.schema.entities.*;
import metashop.schema.entities.discounts.Discount;
import java.util.*;

public class Main {

    public static void main(String[] args) throws Exception {
        Map<String, Map<String, String>> metaShopSchema = MetaShopSchema.createSchema(Arrays.asList(Discount.class, ItemOrder.class, Manufacturer.class, Order.class, Product.class, User.class));
        System.out.println(metaShopSchema);
    }
}
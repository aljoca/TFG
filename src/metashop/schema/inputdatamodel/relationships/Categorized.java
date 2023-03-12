package metashop.schema.inputdatamodel.relationships;

import metashop.schema.inputdatamodel.entities.Product;
import metashop.schema.inputdatamodel.entities.ProductCategory;

public class Categorized {

    private Product product;
    private ProductCategory productCategory;

    public Categorized(Product product, ProductCategory productCategory) {
        this.product = product;
        this.productCategory = productCategory;
    }

    public Product getProduct() {
        return product;
    }

    public ProductCategory getProductCategory() {
        return productCategory;
    }
}

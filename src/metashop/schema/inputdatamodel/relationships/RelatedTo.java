package metashop.schema.inputdatamodel.relationships;

import metashop.schema.inputdatamodel.entities.Product;

public class RelatedTo {

    private Product product;
    private Product relatedProduct;

    public RelatedTo(Product product, Product relatedProduct) {
        this.product = product;
        this.relatedProduct = relatedProduct;
    }

    public Product getProduct() {
        return product;
    }

    public Product getRelatedProduct() {
        return relatedProduct;
    }
}

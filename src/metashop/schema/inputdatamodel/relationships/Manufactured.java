package metashop.schema.inputdatamodel.relationships;

import metashop.schema.inputdatamodel.entities.Manufacturer;
import metashop.schema.inputdatamodel.entities.Product;

public class Manufactured {

    private Manufacturer manufacturer;
    private Product product;

    public Manufactured(Manufacturer manufacturer, Product product) {
        this.manufacturer = manufacturer;
        this.product = product;
    }

    public Manufacturer getManufacturer() {
        return manufacturer;
    }

    public Product getProduct() {
        return product;
    }
}

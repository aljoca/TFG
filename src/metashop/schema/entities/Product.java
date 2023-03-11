package metashop.schema.entities;

import java.util.HashMap;
import java.util.Map;

public class Product {

    private String __productId;
    private String name;
    private String imageUrl;
    private String description;
    private Double price;
    private Integer stock;

    public Product(String __productId, String name, String imageUrl, String description, Double price, Integer stock) {
        this.__productId = __productId;
        this.name = name;
        this.imageUrl = imageUrl;
        this.description = description;
        this.price = price;
        this.stock = stock;
    }

    public String getProductId() {
        return __productId;
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public Double getPrice() {
        return price;
    }

    public Integer getStock() {
        return stock;
    }

}

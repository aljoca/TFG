package metashop.schema.entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Product {

    private String __productId;
    private String name;
    private String imageUrl;
    private String description;
    private Double price;
    private Integer stock;
    private ArrayList<ProductCategory> categories;
    private ArrayList<Product> relatedProducts;

    public Product(String __productId, String name, String imageUrl, String description, Double price, Integer stock) {
        this.__productId = __productId;
        this.name = name;
        this.imageUrl = imageUrl;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.categories = new ArrayList<>();
        this.relatedProducts = new ArrayList<>();

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

    public ArrayList<ProductCategory> getCategories() {
        return categories;
    }

    public ArrayList<Product> getRelatedProducts() {
        return relatedProducts;
    }

}

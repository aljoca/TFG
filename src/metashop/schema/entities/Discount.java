package metashop.schema.entities;

import java.util.HashMap;
import java.util.Map;

public class Discount {

    private Integer __discountId;
    private String info;
    private Double value;

    public Discount(Integer __discountId, String info, Double value) {
        this.__discountId = __discountId;
        this.info = info;
        this.value = value;
    }

    public Integer getDiscountId() {
        return __discountId;
    }

    public String getInfo() {
        return info;
    }

    public Double getValue() {
        return value;
    }
}

package metashop.schema.inputdatamodel.relationships;

import metashop.schema.inputdatamodel.entities.Discount;
import metashop.schema.inputdatamodel.entities.User;

public class Earns {

    private User user;
    private Discount discount;

    Earns(User user, Discount discount){
        this.user = user;
        this.discount = discount;
    }

    public User getUser() {
        return user;
    }

    public Discount getDiscount() {
        return discount;
    }
}

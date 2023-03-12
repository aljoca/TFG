package metashop.schema.inputdatamodel.entities;

import java.util.Date;


public class User {

    private String __userId;
    private String name;
    private String email;
    private String password;
    private String country;
    private Boolean isPremium;
    private String shoppingAddress;
    private String shopOpinion;
    private Date registerDate;

    public User(String __userId, String name, String email, String password, String country, Boolean isPremium, String shoppingAddress, String shopOpinion, Date registerDate) {
        this.__userId = __userId;
        this.name = name;
        this.email = email;
        this.password = password;
        this.country = country;
        this.isPremium = isPremium;
        this.shoppingAddress = shoppingAddress;
        this.shopOpinion = shopOpinion;
        this.registerDate = registerDate;
    }

    public String getUserId() {
        return __userId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getCountry() {
        return country;
    }

    public Boolean getPremium() {
        return isPremium;
    }

    public String getShoppingAddress() {
        return shoppingAddress;
    }

    public String getShopOpinion() {
        return shopOpinion;
    }

    public Date getRegisterDate() {
        return registerDate;
    }

}

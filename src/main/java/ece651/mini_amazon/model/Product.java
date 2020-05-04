package ece651.mini_amazon.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Product {
    @Id
    @GeneratedValue
    @Column
    private int id;
    @Column
    private String photo;
    @Column
    private double price;
    @Column
    private String description;
    @Column
    private String category;

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getId() {
        return id;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setName(String description) {
        this.description = description;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public Product() {
    }

    public Product(int id, String photo, double price, String description) {
        this.id = id;
        this.description = description;
        this.photo = photo;
        this.price = price;
    }
}

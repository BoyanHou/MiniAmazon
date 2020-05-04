package ece651.mini_amazon.model;

import org.json.JSONObject;

import javax.persistence.*;

@Entity
public class Cart {
    @Id
    @GeneratedValue
    private int id;

    @OneToOne(cascade= CascadeType.ALL)
    private User owner;

    private String items; //JSONObject: (String)item_id : item_num;
    public Cart () {
        this.items = new JSONObject().toString();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public String getItems() {
        return items;
    }

    public void setItems(String items) {
        this.items = items;
    }
}

package ece651.mini_amazon.model;

import org.json.JSONObject;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class WareHouse {
    @Id
    @GeneratedValue
    private int whid;

    private int x;
    private int y;

    private String stock;
    // JSONObject: "product_id":product_num

    public WareHouse() {
        this.stock = new JSONObject().toString();
    }

    public String getStock() {
        return stock;
    }

    public void setStock(String stock) {
        this.stock = stock;
    }

    public int getWhid() {
        return whid;
    }

    public void setWhid(int whid) {
        this.whid = whid;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }


}

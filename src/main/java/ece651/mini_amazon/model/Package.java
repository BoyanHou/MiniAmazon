package ece651.mini_amazon.model;

import org.json.JSONObject;

import javax.persistence.*;

@Entity
public class Package {
    @Id
    @GeneratedValue
    private int shipid;
    private int targetX;
    private int targetY;
    private String status;
    private int uid;                // associated ups id, optional
    private String uAccountName;    // associated ups account name, optional, only valid when uid exists
    private int truckID;

    // JSONObject, in the format of:
    //   product_id : product_num
    private String products;

    @OneToOne(cascade=CascadeType.ALL)
    private User owner;

    @OneToOne(cascade = CascadeType.ALL)
    private WareHouse wh;

    public Package () {
        this.uAccountName = "";
    }

    public Package(int targetX, int targetY, String products, User owner, WareHouse wh, String uAccountName) {
        this.targetX = targetX;
        this.targetY = targetY;
        this.products = products;
        this.owner = owner;
        this.wh = wh;
        this.uAccountName = uAccountName;
    }

    public int getShipid() {
        return shipid;
    }

    public void setShipid(int shipid) {
        this.shipid = shipid;
    }

    public int getTargetX() {
        return targetX;
    }

    public void setTargetX(int targetX) {
        this.targetX = targetX;
    }

    public int getTargetY() {
        return targetY;
    }

    public void setTargetY(int targetY) {
        this.targetY = targetY;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getProducts() {
        return products;
    }

    public void setProducts(String products) {
        this.products = products;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public WareHouse getWh() {
        return wh;
    }

    public void setWh(WareHouse wh) {
        this.wh = wh;
    }

    public String getuAccountName() {
        return uAccountName;
    }

    public void setuAccountName(String uAccountName) {
        this.uAccountName = uAccountName;
    }

    public int getTruckID() {
        return truckID;
    }

    public void setTruckID(int truckID) {
        this.truckID = truckID;
    }
}

package ece651.mini_amazon.model;

import ece651.mini_amazon.dao.PackageDao;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
public class User {

    public User() {
        this.uids = new JSONArray().toString();
    }

    @Id
    @GeneratedValue
    private long userID;
    private String username;
    private String password;

    @OneToOne(cascade=CascadeType.ALL)
    private Cart cart;

    @OneToMany(cascade=CascadeType.ALL)
    private Set<Package> packages;

    // JSONArray of associated ups account ids
    private String uids;

    public long getUserID() {
        return userID;
    }

    public void setUserID(long userID) {
        this.userID = userID;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Cart getCart() {
        return cart;
    }

    public void setCart(Cart cart) {
        this.cart = cart;
    }

    public Set<Package> getPackages() {
        return packages;
    }

    public void setPackages(Set<Package> packages) {
        this.packages = packages;
    }

    public String getUids() {
        return uids;
    }

    public void setUids(String uids) {
        this.uids = uids;
    }


}

package ece651.mini_amazon.service.webService;

import ece651.mini_amazon.dao.*;
import ece651.mini_amazon.exceptions.serviceException.NoSuchPackageException;
import ece651.mini_amazon.exceptions.serviceException.NoSuchProductException;
import ece651.mini_amazon.exceptions.serviceException.NoSuchWarehouseException;
import ece651.mini_amazon.exceptions.serviceException.StockNotEnoughException;
import ece651.mini_amazon.exceptions.utilsException.IntFormatException;
import ece651.mini_amazon.model.*;
import ece651.mini_amazon.model.Package;
import ece651.mini_amazon.service.daemonService.DaemonService;
import ece651.mini_amazon.utils.tools.Logger;
import ece651.mini_amazon.utils.tools.NumUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;

@Service
public class ShoppingService {
    @Autowired
    PackageDao packageDao;
    @Autowired
    UserDao userDao;
    @Autowired
    UserService userService;
    @Autowired
    CartDao cartDao;
    @Autowired
    ProductDao productDao;
    @Autowired
    WareHouseDao whDao;
    @Autowired
    BackingService backingService;
    @Autowired
    DaemonService daemonService;

    public void addToCart(Product product, int num, User user) {
        Cart cart = user.getCart();
        JSONObject jo = new JSONObject(cart.getItems());
        String productIdStr = Integer.toString(product.getId());
        if (!jo.has(productIdStr)) {
            jo.put(productIdStr, 0);
        }
        jo.put(productIdStr, jo.getInt(productIdStr) + num);
        cart.setItems(jo.toString());
        cartDao.save(cart);
    }

    public Product getProduct(Integer productID) throws NoSuchProductException{
        if (productID == null) {
            throw new NoSuchProductException("Product ID is null");
        }
        Product product = productDao.findById(productID).orElse(null);

        if (product == null) {
            throw new NoSuchProductException("No Such Product with id: " + productID);
        }
        return product;
    }

    public int purchaseItemsInCart(User user, String xStr, String yStr, String uAccountName)
            throws NoSuchWarehouseException,
            StockNotEnoughException,
            IntFormatException {
        JSONObject items = new JSONObject(user.getCart().getItems());
        if (items.isEmpty()) {
            throw new StockNotEnoughException("Your cart is Empty!");
        }
        int x = NumUtils.strToInt(xStr);
        int y = NumUtils.strToInt(yStr);
        int whID = backingService.getOptimizedWHID(x, y);
        WareHouse wh = this.backingService.findWareHouse(whID);
        int maxPurchaseNum = -1;

        String lackedProductIDs = "";
        for (String productIDStr : items.keySet()) {
            int productID = Integer.parseInt(productIDStr);
            int purchaseNum = items.getInt(productIDStr);
            int stockNum = backingService.getStockNum(whID, productID);
            if (stockNum < purchaseNum) {
                if (maxPurchaseNum < purchaseNum) {  // record the max purchase num that is not satisfied, for re-stocking
                    maxPurchaseNum = purchaseNum;
                }
                lackedProductIDs += "productID:" + productID + " ";
            }
        }

        // there is shortage in the warehouse: APurchase more; purchase failed
        if (lackedProductIDs.length() != 0) {
            Logger.logSys("Found Stock Shortage in WareHouse:" + whID + ", launching APurchaseMore...");
            // restock everything by 5000 count
            JSONObject restockList = this.backingService.getRestockEverythingList(5000);
            this.daemonService.purchaseMore(restockList, whID);
            Logger.logSys("APurchaseMore has been launched");

            String errMsg = "There is not enough " + lackedProductIDs + " in warehouse:" + whID;
            errMsg += " We will accordingly re-stock to that warehouse, you can check back very shortly!";
            throw new StockNotEnoughException(errMsg);
        }


        // no shortage in warehouse: APack; purchase success
        String itemsStr = user.getCart().getItems();
            // reserve(deduct) stock from warehouse
        this.backingService.reserveStock(whID, itemsStr);
            // create package from cart;
        Package pkg = new Package(x, y, itemsStr, user, wh, uAccountName);
        // set package status as "Preparing"
        pkg.setStatus("Preparing");
        this.packageDao.save(pkg);
        user.getPackages().add(pkg);
        this.userDao.save(user);
        int packageID = pkg.getShipid();
        Logger.logMsg("Created: PackageID:" + packageID + " for userID:" + user.getUserID());
            // empty cart
        Cart cart = user.getCart();
        cart.setItems("{}");
        cartDao.save(cart);
            // send world APack message to pack this package in warehouse
        daemonService.pack(whID, items, packageID);

        return packageID;
    }

    public ArrayList<Item> getItemList (JSONObject itemJO) throws NoSuchProductException {
        ArrayList<Item> result = new ArrayList<>();
        for (String productIDStr : itemJO.keySet()) {
            int productID = Integer.parseInt(productIDStr);
            int quantity = itemJO.getInt(productIDStr);
            Product product = this.getProduct(productID);
            result.add(new Item(product, quantity));
        }
        return result;
    }

    public Package getPackage(String packageIDStr)
            throws IntFormatException,
            NoSuchPackageException {
        int packageID =  NumUtils.strToInt(packageIDStr);
        return this.getPackage(packageID);
    }

    public Package getPackage(int packageID) throws NoSuchPackageException {
        Package pkg = this.packageDao.findById(packageID).orElse(null);
        if (pkg == null) {
            String errStr = "Cannot find package with id:" + packageID;
            throw new NoSuchPackageException(errStr);
        }
        return pkg;
    }
    public double getTotalPrice(ArrayList<Item> items) {
        double total = 0;
        for (Item item: items) {
            total += item.getQuantity() * item.getProduct().getPrice();
        }
        return total;
    }

    public ArrayList<Product> search(String info) {
        ArrayList<Product> results = new ArrayList<>();
        for (Product product: productDao.findAll()) {
            if (product.getDescription().contains(info)) {
                results.add(product);
            }
        }
        return results;
    }

    public ArrayList<Product> category(String c) {
        ArrayList<Product> results = new ArrayList<>();
        for (Product product: productDao.findAll()) {
            if (product.getCategory().equals(c)) {
                results.add(product);
            }
        }
        return results;
    }

    public ArrayList<String> getCategories() {
        ArrayList<String> categories = new ArrayList<>();
        for (Product product: productDao.findAll()) {
            String category = product.getCategory();
            if (!categories.contains(category)) {
                categories.add(category);
            }
        }
        return categories;
    }

}

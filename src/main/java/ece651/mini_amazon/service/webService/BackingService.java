package ece651.mini_amazon.service.webService;

import ece651.mini_amazon.dao.ProductDao;
import ece651.mini_amazon.dao.WareHouseDao;
import ece651.mini_amazon.exceptions.serviceException.NoSuchProductException;
import ece651.mini_amazon.exceptions.serviceException.NoSuchWarehouseException;
import ece651.mini_amazon.exceptions.serviceException.StockNotEnoughException;
import ece651.mini_amazon.model.Product;
import ece651.mini_amazon.model.WareHouse;
import ece651.mini_amazon.utils.tools.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class BackingService {
    @Autowired
    WareHouseDao whDao;
    @Autowired
    ProductDao productDao;

    public void updateStock(int whid, int productID, int productNum) throws NoSuchWarehouseException {
        WareHouse wh = this.findWareHouse(whid);

        JSONObject jo = new JSONObject(wh.getStock());
        String idStr = Integer.toString(productID);
        if (!jo.has(idStr)) {
            jo.put(idStr, 0);
        }
        jo.put(idStr, jo.getInt(idStr) + productNum);
        wh.setStock(jo.toString());
        whDao.save(wh);
    }

    public int getStockNum(int whid, int productID) throws NoSuchWarehouseException {
        WareHouse wh = this.findWareHouse(whid);
        JSONObject stock = new JSONObject(wh.getStock());
        String productIDStr = Integer.toString(productID);
        if (!stock.has(productIDStr)) {
            return 0;
        } else {
            return stock.getInt(productIDStr);
        }
    }

    public WareHouse findWareHouse(int whid) throws NoSuchWarehouseException{
        WareHouse wh = whDao.findById(whid).orElse(null);

        if (wh == null) {
            String msg = "Failed to update warehouse stock: no such whid: " + whid;
            throw new NoSuchWarehouseException(msg);
        }
        return wh;
    }

    public int getOptimizedWHID(int x, int y) {
        int whID = -1;
        int minDistance = -1;
        List<WareHouse> whList = (List<WareHouse>)whDao.findAll();
        for (WareHouse wh : whList) {
            int l1 = x - wh.getX();
            int l2 = y - wh.getY();
            int distance = l1 * l1 + l2 * l2;
            if (whID == -1 || minDistance > distance) {
                minDistance = distance;
                whID = wh.getWhid();
            }
        }
        return whID;
    }

    public WareHouse getWareHouse(Integer whID) throws NoSuchWarehouseException {
        if (whID == null) {
            throw new NoSuchWarehouseException("WareHouse id is empty!");
        }
        WareHouse wh = whDao.findById(whID).orElse(null);

        if (wh == null) {
            throw new NoSuchWarehouseException("No such warehouse with whid:" + whID);
        }
        return wh;
    }

    public JSONObject getRestockEverythingList(int num) {
        List<Product> productList = (List<Product>) productDao.findAll();
        JSONObject jo = new JSONObject();
        for (Product product : productList) {
            jo.put(Integer.toString(product.getId()), num);
        }
        return jo;
    }

    public void reserveStock(int whID, String items) throws StockNotEnoughException, NoSuchWarehouseException {
        WareHouse wh = this.findWareHouse(whID);
        JSONObject stockJO = new JSONObject(wh.getStock());
        JSONObject itemsJO = new JSONObject(items);
        this.checkStock(whID, items);
        for (String productIDStr : itemsJO.keySet()) {
            int requiredCount = itemsJO.getInt(productIDStr);
            int newCount = stockJO.getInt(productIDStr) - requiredCount;
            stockJO.put(productIDStr, newCount);
            wh.setStock(stockJO.toString());
            this.whDao.save(wh);
            Logger.logMsg("Reserved productID:" + productIDStr + " num:" + requiredCount + " from whID:" + whID);
        }
    }

    public void checkStock(int whID, String items) throws StockNotEnoughException, NoSuchWarehouseException {
        JSONObject itemsJO = new JSONObject(items);
        String shortage = "";
        for (String productIDStr : itemsJO.keySet()) {
            int purchaseNum = itemsJO.getInt(productIDStr);
            int productID = Integer.parseInt(productIDStr);
            if (this.getStockNum(whID, productID) < purchaseNum) {
                shortage += "productID:" + productIDStr + ",";
            }
        }
        if (shortage.length() != 0) {
            throw new StockNotEnoughException("Not enough " + shortage + "in warehouseID:" + whID);
        }
    }


}

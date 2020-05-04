package ece651.mini_amazon.dao;

import ece651.mini_amazon.model.WareHouse;
import org.json.JSONObject;
import org.springframework.data.repository.CrudRepository;

public interface WareHouseDao extends CrudRepository<WareHouse, Integer> {


}
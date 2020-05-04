package ece651.mini_amazon.dao;

import ece651.mini_amazon.model.Cart;
import org.springframework.data.repository.CrudRepository;

public interface CartDao extends CrudRepository<Cart, Integer> {
}


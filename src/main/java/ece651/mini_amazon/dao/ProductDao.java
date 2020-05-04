package ece651.mini_amazon.dao;

import ece651.mini_amazon.model.Product;
import ece651.mini_amazon.model.User;
import org.springframework.data.repository.CrudRepository;

import java.lang.reflect.Array;
import java.util.List;
import java.util.UUID;

public interface ProductDao extends CrudRepository<Product, Integer> {
}

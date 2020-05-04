package ece651.mini_amazon.dao;

import ece651.mini_amazon.model.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface UserDao extends CrudRepository<User, Long> {
    List<User> findByUsername(String username);
}

package ece651.mini_amazon.dao;

import ece651.mini_amazon.model.Package;
import org.springframework.data.repository.CrudRepository;

public interface PackageDao extends CrudRepository<Package, Integer> {

}
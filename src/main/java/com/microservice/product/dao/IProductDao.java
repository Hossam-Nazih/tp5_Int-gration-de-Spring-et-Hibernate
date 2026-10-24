package com.microservice.product.dao;

import com.microservice.product.entity.Product;

import java.util.List;

/*
 * DAO des produits = CRUD générique (hérité de IDao<Product>) + requêtes propres aux produits.
 */
public interface IProductDao extends IDao<Product> {
    List<Product> findByPrixBetween(double min, double max);
    List<Product> findByDesignationContaining(String motCle);
    List<Product> findByCategorie(Long idCategorie);
    // Somme de prixUnitaire x quantiteStock sur tous les produits
    double calculerValeurStock();
}

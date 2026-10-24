package com.microservice.product.service;

import com.microservice.product.entity.Product;

import java.util.List;

/*
 * Ce que le microservice sait faire avec les produits (couche métier).
 * Les noms en anglais sont ceux de l'énoncé, ceux en français sont des ajouts.
 * MainApp, et plus tard un contrôleur REST, ne dépendent que de cette interface.
 */
public interface ProductService {
    void addProduct(Product product);
    void addProducts(List<Product> products);
    List<Product> getAllProducts();

    Product trouverProduit(Long idProduit);
    List<Product> rechercherParDesignation(String motCle);
    List<Product> produitsEntrePrix(double min, double max);
    Product appliquerRemise(Long idProduit, double pourcentage);
    Product retirerDuStock(Long idProduit, int quantite);
    double valeurDuStock();
    void supprimerProduit(Long idProduit);
}

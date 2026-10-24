package com.microservice.product.service;

import com.microservice.product.entity.Categorie;
import com.microservice.product.entity.Product;

import java.util.List;

/*
 * Ce que le microservice sait faire avec les catégories (ajout personnel).
 */
public interface CategorieService {
    Categorie creerCategorie(Categorie categorie);
    Categorie trouverCategorie(Long idCategorie);
    List<Categorie> toutesLesCategories();
    Product affecterCategorie(Long idProduit, Long idCategorie);
    List<Product> produitsDeLaCategorie(Long idCategorie);
    void supprimerCategorie(Long idCategorie);
}

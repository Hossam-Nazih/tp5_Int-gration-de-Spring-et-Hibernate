package com.microservice.product.dao;

import com.microservice.product.entity.Categorie;

/*
 * DAO des catégories : le CRUD générique + une recherche par nom.
 */
public interface ICategorieDao extends IDao<Categorie> {
    // null si aucune catégorie ne porte ce nom
    Categorie findByNom(String nomCategorie);
}

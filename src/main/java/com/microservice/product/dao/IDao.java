package com.microservice.product.dao;

import java.util.List;

/*
 * Contrat commun à tous les DAO du microservice : les opérations CRUD de base.
 * <T> = le type d'entité manipulé, choisi par celui qui utilise l'interface :
 *   IDao<Product>   travaille sur des produits
 *   IDao<Categorie> travaille sur des catégories
 * Une seule interface pour toutes les entités, et le compilateur empêche de mélanger les types.
 */
public interface IDao<T> {
    void save(T entity);        // ajouter une ligne       (INSERT)
    void update(T entity);      // modifier une ligne      (UPDATE)
    void delete(T entity);      // supprimer une ligne     (DELETE)
    T findById(Long id);        // lire une ligne par clé  (SELECT ... WHERE id = ?)
    List<T> findAll();          // lire toute la table     (SELECT *)
}

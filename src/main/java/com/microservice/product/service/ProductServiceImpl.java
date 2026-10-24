package com.microservice.product.service;

import com.microservice.product.dao.IProductDao;
import com.microservice.product.entity.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/*
 * Règles métier des produits. Schéma de chaque méthode :
 *   1. contrôler les données (sinon IllegalArgumentException)
 *   2. déléguer la lecture/écriture au DAO
 *
 * @Transactional est posé ici et non dans le DAO : une méthode du service = une transaction,
 * même quand elle appelle le DAO plusieurs fois. En cas d'exception, tout est annulé (rollback).
 */
@Service
public class ProductServiceImpl implements ProductService {

    // Au-delà de 90 % de remise, on considère qu'il s'agit d'une erreur de saisie
    private static final double REMISE_MAXIMALE = 90.0;

    @Autowired
    private IProductDao productDao;

    @Override
    @Transactional
    public void addProduct(Product product) {
        verifier(product);
        productDao.save(product);
    }

    // Ajout groupé "tout ou rien" : un seul produit invalide annule aussi ceux déjà enregistrés
    @Override
    @Transactional
    public void addProducts(List<Product> products) {
        for (Product product : products) {
            verifier(product);
            productDao.save(product);
        }
    }

    // readOnly = true : transaction de lecture seule, plus légère pour Hibernate
    @Override
    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        return productDao.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Product trouverProduit(Long idProduit) {
        Product product = productDao.findById(idProduit);
        if (product == null) {
            throw new IllegalArgumentException("Produit introuvable : id " + idProduit);
        }
        return product;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> rechercherParDesignation(String motCle) {
        return productDao.findByDesignationContaining(motCle);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> produitsEntrePrix(double min, double max) {
        if (min > max) {
            throw new IllegalArgumentException("Intervalle de prix incohérent : " + min + " > " + max);
        }
        return productDao.findByPrixBetween(min, max);
    }

    @Override
    @Transactional
    public Product appliquerRemise(Long idProduit, double pourcentage) {
        if (pourcentage <= 0 || pourcentage > REMISE_MAXIMALE) {
            throw new IllegalArgumentException("Remise refusée : elle doit être comprise entre 0 et "
                    + REMISE_MAXIMALE + " %");
        }
        Product product = trouverProduit(idProduit);
        // Arrondi au centime : 149.99 - 10 % = 134.991 -> 134.99
        double nouveauPrix = Math.round(product.getPrixUnitaire() * (1 - pourcentage / 100) * 100) / 100.0;
        product.setPrixUnitaire(nouveauPrix);
        // Le produit a été lu dans CETTE transaction : Hibernate repère la modification
        // et écrit l'UPDATE au commit. L'appel à update() rend simplement l'intention explicite.
        productDao.update(product);
        return product;
    }

    @Override
    @Transactional
    public Product retirerDuStock(Long idProduit, int quantite) {
        if (quantite <= 0) {
            throw new IllegalArgumentException("La quantité retirée doit être positive");
        }
        Product product = trouverProduit(idProduit);
        if (quantite > product.getQuantiteStock()) {
            throw new IllegalArgumentException("Stock insuffisant pour " + product.getDesignation()
                    + " : " + product.getQuantiteStock() + " disponible(s), " + quantite + " demandé(s)");
        }
        product.setQuantiteStock(product.getQuantiteStock() - quantite);
        productDao.update(product);
        return product;
    }

    @Override
    @Transactional(readOnly = true)
    public double valeurDuStock() {
        return productDao.calculerValeurStock();
    }

    @Override
    @Transactional
    public void supprimerProduit(Long idProduit) {
        productDao.delete(trouverProduit(idProduit));
    }

    // Conditions pour qu'un produit soit accepté en base
    private void verifier(Product product) {
        if (product.getDesignation() == null || product.getDesignation().trim().isEmpty()) {
            throw new IllegalArgumentException("La désignation du produit est obligatoire");
        }
        if (product.getPrixUnitaire() < 0) {
            throw new IllegalArgumentException("Le prix ne peut pas être négatif");
        }
        if (product.getQuantiteStock() < 0) {
            throw new IllegalArgumentException("Le stock ne peut pas être négatif");
        }
    }
}

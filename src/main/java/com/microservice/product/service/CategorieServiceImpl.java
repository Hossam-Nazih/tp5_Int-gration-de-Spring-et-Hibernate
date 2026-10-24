package com.microservice.product.service;

import com.microservice.product.dao.ICategorieDao;
import com.microservice.product.dao.IProductDao;
import com.microservice.product.entity.Categorie;
import com.microservice.product.entity.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/*
 * Règles métier des catégories :
 *   - nom obligatoire et unique (vérifié AVANT l'insertion, pour un message clair)
 *   - on ne supprime pas une catégorie qui contient encore des produits
 * Ce service utilise deux DAO : celui des catégories et celui des produits.
 */
@Service
public class CategorieServiceImpl implements CategorieService {

    @Autowired
    private ICategorieDao categorieDao;

    @Autowired
    private IProductDao productDao;

    @Override
    @Transactional
    public Categorie creerCategorie(Categorie categorie) {
        if (categorie.getNomCategorie() == null || categorie.getNomCategorie().trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom de la catégorie est obligatoire");
        }
        // Sans ce contrôle, la contrainte UNIQUE de la base lèverait une erreur SQL peu lisible
        if (categorieDao.findByNom(categorie.getNomCategorie()) != null) {
            throw new IllegalArgumentException("La catégorie existe déjà : " + categorie.getNomCategorie());
        }
        categorieDao.save(categorie);
        return categorie;
    }

    @Override
    @Transactional(readOnly = true)
    public Categorie trouverCategorie(Long idCategorie) {
        Categorie categorie = categorieDao.findById(idCategorie);
        if (categorie == null) {
            throw new IllegalArgumentException("Catégorie introuvable : id " + idCategorie);
        }
        return categorie;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Categorie> toutesLesCategories() {
        return categorieDao.findAll();
    }

    // Les deux lectures et la modification se font dans la MÊME transaction
    @Override
    @Transactional
    public Product affecterCategorie(Long idProduit, Long idCategorie) {
        Product product = productDao.findById(idProduit);
        if (product == null) {
            throw new IllegalArgumentException("Produit introuvable : id " + idProduit);
        }
        product.setCategorie(trouverCategorie(idCategorie));
        productDao.update(product);
        return product;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> produitsDeLaCategorie(Long idCategorie) {
        trouverCategorie(idCategorie); // erreur claire si la catégorie n'existe pas
        return productDao.findByCategorie(idCategorie);
    }

    @Override
    @Transactional
    public void supprimerCategorie(Long idCategorie) {
        Categorie categorie = trouverCategorie(idCategorie);
        int nombreProduits = productDao.findByCategorie(idCategorie).size();
        if (nombreProduits > 0) {
            throw new IllegalStateException("Impossible de supprimer " + categorie.getNomCategorie()
                    + " : elle contient encore " + nombreProduits + " produit(s)");
        }
        categorieDao.delete(categorie);
    }
}

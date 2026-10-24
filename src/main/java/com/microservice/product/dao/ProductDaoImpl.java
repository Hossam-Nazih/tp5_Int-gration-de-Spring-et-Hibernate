package com.microservice.product.dao;

import com.microservice.product.entity.Product;
import org.springframework.stereotype.Repository;

import java.util.List;

/*
 * DAO concret des produits. Le CRUD vient de GenericDaoImpl<Product> ;
 * cette classe n'ajoute que les requêtes HQL spécifiques.
 *
 * @Repository : variante de @Component réservée à la couche d'accès aux données.
 * Spring la détecte au démarrage et en fait un bean injectable.
 *
 * Dans le HQL on utilise les noms d'ATTRIBUTS Java (prixUnitaire, designation),
 * pas les noms de colonnes SQL (prix_unitaire).
 */
@Repository
public class ProductDaoImpl extends GenericDaoImpl<Product> implements IProductDao {

    public ProductDaoImpl() {
        super(Product.class);
    }

    @Override
    public List<Product> findByPrixBetween(double min, double max) {
        // :min et :max sont des paramètres nommés, remplis de façon sûre (pas d'injection SQL)
        return session()
                .createQuery("FROM Product p WHERE p.prixUnitaire BETWEEN :min AND :max ORDER BY p.prixUnitaire",
                        Product.class)
                .setParameter("min", min)
                .setParameter("max", max)
                .list();
    }

    @Override
    public List<Product> findByDesignationContaining(String motCle) {
        // lower(...) des deux côtés : "GATE" trouve "Microservice Gateway"
        return session()
                .createQuery("FROM Product p WHERE lower(p.designation) LIKE :motif", Product.class)
                .setParameter("motif", "%" + motCle.toLowerCase() + "%")
                .list();
    }

    @Override
    public List<Product> findByCategorie(Long idCategorie) {
        // p.categorie.idCategorie : HQL suit la relation @ManyToOne, Hibernate écrit la jointure
        return session()
                .createQuery("FROM Product p WHERE p.categorie.idCategorie = :id ORDER BY p.designation",
                        Product.class)
                .setParameter("id", idCategorie)
                .list();
    }

    @Override
    public double calculerValeurStock() {
        // SUM renvoie null quand la table est vide : on renvoie alors 0
        Double total = session()
                .createQuery("SELECT SUM(p.prixUnitaire * p.quantiteStock) FROM Product p", Double.class)
                .uniqueResult();
        return total == null ? 0 : total;
    }
}

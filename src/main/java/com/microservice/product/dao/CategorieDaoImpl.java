package com.microservice.product.dao;

import com.microservice.product.entity.Categorie;
import org.springframework.stereotype.Repository;

/*
 * DAO des catégories. Grâce à GenericDaoImpl, aucune méthode CRUD n'est écrite ici :
 * c'est tout l'intérêt du DAO générique <T> pour une nouvelle entité.
 */
@Repository
public class CategorieDaoImpl extends GenericDaoImpl<Categorie> implements ICategorieDao {

    public CategorieDaoImpl() {
        super(Categorie.class);
    }

    @Override
    public Categorie findByNom(String nomCategorie) {
        // uniqueResult : au plus un résultat attendu (le nom est UNIQUE en base), null sinon
        return session()
                .createQuery("FROM Categorie c WHERE lower(c.nomCategorie) = :nom", Categorie.class)
                .setParameter("nom", nomCategorie.toLowerCase())
                .uniqueResult();
    }
}

package com.microservice.product.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/*
 * Implémentation du CRUD écrite une seule fois pour TOUTES les entités.
 * Chaque DAO concret hérite de cette classe en précisant son type :
 *   ProductDaoImpl   extends GenericDaoImpl<Product>
 *   CategorieDaoImpl extends GenericDaoImpl<Categorie>
 * et récupère save / update / delete / findById / findAll sans réécrire une ligne.
 *
 * abstract : cette classe sert de modèle, on ne l'instancie jamais directement.
 */
public abstract class GenericDaoImpl<T> implements IDao<T> {

    // Injectée par Spring : c'est le bean créé dans HibernateConfig.sessionFactory()
    @Autowired
    private SessionFactory sessionFactory;

    // À l'exécution, Java "oublie" ce qu'est T : on mémorise donc la classe réelle (Product.class...)
    private final Class<T> typeEntite;

    protected GenericDaoImpl(Class<T> typeEntite) {
        this.typeEntite = typeEntite;
    }

    /*
     * Renvoie la session attachée à la transaction en cours.
     * Cette transaction est ouverte par @Transactional dans la couche service : le DAO, lui,
     * ne commence ni ne valide jamais de transaction. Appelé hors transaction => exception.
     */
    protected Session session() {
        return sessionFactory.getCurrentSession();
    }

    @Override
    public void save(T entity) {
        // Après save, l'id généré par la base est recopié dans l'objet
        session().save(entity);
    }

    @Override
    public void update(T entity) {
        session().update(entity);
    }

    @Override
    public void delete(T entity) {
        session().delete(entity);
    }

    @Override
    public T findById(Long id) {
        // null si la clé n'existe pas
        return session().get(typeEntite, id);
    }

    @Override
    public List<T> findAll() {
        // Requête HQL : on écrit le nom de la CLASSE Java ("Product"), Hibernate le traduit en nom de table
        return session().createQuery("FROM " + typeEntite.getSimpleName(), typeEntite).list();
    }
}

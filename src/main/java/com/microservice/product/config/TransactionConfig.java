package com.microservice.product.config;

import org.hibernate.SessionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/*
 * Met en place la gestion automatique des transactions.
 *
 * @EnableTransactionManagement : pour chaque bean qui a des méthodes @Transactional, Spring crée
 * un "proxy" (un intermédiaire) qui, à chaque appel :
 *   - démarre une transaction,
 *   - appelle la vraie méthode,
 *   - valide (COMMIT) si tout s'est bien passé, ou annule (ROLLBACK) si une RuntimeException est levée.
 *
 * HibernateTransactionManager est le chef d'orchestre qui ouvre/valide/annule ces transactions
 * sur les sessions Hibernate.
 *
 * Correction de l'énoncé : l'import "org.SessionFactory" n'existe pas, c'est org.hibernate.SessionFactory.
 */
@Configuration
@EnableTransactionManagement
public class TransactionConfig {

    // Spring injecte automatiquement le SessionFactory produit par HibernateConfig
    @Bean
    public HibernateTransactionManager transactionManager(SessionFactory sessionFactory) {
        HibernateTransactionManager txManager = new HibernateTransactionManager();
        txManager.setSessionFactory(sessionFactory);
        return txManager;
    }
}

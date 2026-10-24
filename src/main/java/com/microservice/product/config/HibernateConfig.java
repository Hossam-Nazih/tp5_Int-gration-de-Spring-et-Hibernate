package com.microservice.product.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;

import javax.sql.DataSource;
import java.util.Properties;

/*
 * Relie Hibernate à la base de données. Deux beans sont créés :
 *   1. DataSource     : les informations de connexion (pilote, URL, identifiants)
 *   2. SessionFactory : l'usine Hibernate qui ouvre les sessions de travail avec la base
 * Toutes les valeurs viennent d'application.properties : changer de base ne demande aucune recompilation.
 */
@Configuration
@PropertySource("classpath:application.properties")
public class HibernateConfig {

    // Accès aux propriétés : fichier .properties, mais aussi -Dcle=valeur et variables d'environnement
    @Autowired
    private Environment env;

    @Bean
    public DataSource dataSource() {
        // DriverManagerDataSource ouvre une connexion neuve à chaque besoin : simple, adapté à un TP.
        // Un vrai microservice utiliserait un pool de connexions réutilisables (HikariCP).
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        // getRequiredProperty : erreur explicite au démarrage si la clé manque dans la configuration
        dataSource.setDriverClassName(env.getRequiredProperty("jdbc.driver"));
        dataSource.setUrl(env.getRequiredProperty("jdbc.url"));
        dataSource.setUsername(env.getRequiredProperty("jdbc.username"));
        dataSource.setPassword(env.getProperty("jdbc.password", ""));
        return dataSource;
    }

    // LocalSessionFactoryBean est une "fabrique" : Spring l'utilise pour produire le vrai SessionFactory
    @Bean
    public LocalSessionFactoryBean sessionFactory() {
        LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
        sessionFactory.setDataSource(dataSource());
        // Paquet où Hibernate cherche les classes @Entity (Product et Categorie)
        sessionFactory.setPackagesToScan("com.microservice.product.entity");

        Properties props = new Properties();
        props.put("hibernate.dialect", env.getRequiredProperty("hibernate.dialect"));
        props.put("hibernate.show_sql", env.getProperty("hibernate.show_sql", "false"));
        // Par défaut "validate" : Hibernate vérifie les tables sans jamais les modifier
        props.put("hibernate.hbm2ddl.auto", env.getProperty("hibernate.hbm2ddl.auto", "validate"));
        sessionFactory.setHibernateProperties(props);

        return sessionFactory;
    }
}

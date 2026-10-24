package com.microservice.product;

import com.microservice.product.config.AppConfig;
import org.hibernate.SessionFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/*
 * Test de démarrage : si les deux messages s'affichent, alors
 *   - Spring a trouvé et créé tous les beans,
 *   - Hibernate s'est connecté à la base (H2, ou MySQL selon application.properties)
 *     et a préparé les tables products et categories.
 * En cas de problème (mauvais mot de passe MySQL, serveur arrêté...), une exception s'affiche à la place.
 */
public class TestHibernate {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        System.out.println("Contexte Spring initialisé avec succès !");

        SessionFactory sessionFactory = context.getBean(SessionFactory.class);
        System.out.println("SessionFactory ouverte : " + sessionFactory.isOpen());

        context.close();
    }
}

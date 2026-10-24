package com.microservice.product;

import org.h2.tools.Server;

/*
 * Ouvre la console web de H2 pour voir le contenu des tables products et categories.
 * Lancer cette classe, puis dans le navigateur (http://localhost:8082) se connecter avec :
 *   JDBC URL : jdbc:h2:file:./data/product_db;AUTO_SERVER=TRUE
 *   User     : sa        (mot de passe vide)
 * Exemple de requête : SELECT * FROM PRODUCTS;
 */
public class H2Console {
    public static void main(String[] args) throws Exception {
        // Démarre le serveur web de H2 et ouvre le navigateur ; s'arrête quand on ferme le programme
        Server.createWebServer("-webPort", "8082").start();
        Server.openBrowser("http://localhost:8082");
        System.out.println("Console H2 ouverte sur http://localhost:8082 (arrêter le programme pour la fermer)");
    }
}

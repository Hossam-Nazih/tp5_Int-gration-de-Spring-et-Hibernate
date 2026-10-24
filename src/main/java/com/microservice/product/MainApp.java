package com.microservice.product;

import com.microservice.product.config.AppConfig;
import com.microservice.product.entity.Categorie;
import com.microservice.product.entity.Product;
import com.microservice.product.service.CategorieService;
import com.microservice.product.service.ProductService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Arrays;

/*
 * Démonstration complète du microservice. Tout passe par les services, jamais directement par les DAO.
 */
public class MainApp {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);

        // On récupère les INTERFACES : à cause de @Transactional, Spring fournit des proxys qui
        // implémentent l'interface mais ne sont pas des ProductServiceImpl
        // (getBean(ProductServiceImpl.class), comme dans l'énoncé, échouerait).
        ProductService productService = context.getBean(ProductService.class);
        CategorieService categorieService = context.getBean(CategorieService.class);

        // 1. Catégories (réutilisées si elles existent déjà d'un lancement précédent)
        Categorie infra = obtenirCategorie(categorieService,
                "Infrastructure", "Briques techniques d'une architecture microservices");
        Categorie securite = obtenirCategorie(categorieService,
                "Sécurité", "Authentification et protection des services");
        System.out.println("Catégories : " + categorieService.toutesLesCategories());

        // 2. Produit de l'énoncé
        Product gateway = new Product("Microservice Gateway", 149.99, 20);
        productService.addProduct(gateway);
        System.out.println("Produit ajouté avec succès ! Total en base : " + productService.getAllProducts().size());

        // 3. Ajout groupé
        Product discovery = new Product("Service Discovery", 89.50, 15);
        Product auth = new Product("Auth Server", 120.00, 8);
        productService.addProducts(Arrays.asList(discovery, auth));

        // 4. Rangement dans les catégories
        categorieService.affecterCategorie(gateway.getIdProduit(), infra.getIdCategorie());
        categorieService.affecterCategorie(discovery.getIdProduit(), infra.getIdCategorie());
        categorieService.affecterCategorie(auth.getIdProduit(), securite.getIdCategorie());
        System.out.println("Produits de " + infra.getNomCategorie() + " : "
                + categorieService.produitsDeLaCategorie(infra.getIdCategorie()));

        // 5. Règles métier refusées
        try {
            productService.addProduct(new Product("Produit invalide", -10));
        } catch (IllegalArgumentException e) {
            System.out.println("Refusé : " + e.getMessage());
        }
        try {
            categorieService.creerCategorie(new Categorie("infrastructure", "doublon"));
        } catch (IllegalArgumentException e) {
            System.out.println("Refusé : " + e.getMessage());
        }
        try {
            categorieService.supprimerCategorie(securite.getIdCategorie());
        } catch (IllegalStateException e) {
            System.out.println("Refusé : " + e.getMessage());
        }

        // 6. Transaction tout ou rien : le 2e produit est invalide, le 1er est annulé lui aussi
        int avant = productService.getAllProducts().size();
        try {
            productService.addProducts(Arrays.asList(
                    new Product("Circuit Breaker", 75.00, 5),
                    new Product("", 10.00)));
        } catch (IllegalArgumentException e) {
            System.out.println("Ajout groupé annulé (" + e.getMessage() + ") : "
                    + avant + " produits avant, " + productService.getAllProducts().size() + " après");
        }

        // 7. Stock et prix
        System.out.println("Valeur du stock : " + productService.valeurDuStock() + " DH");
        System.out.println("Après vente de 3 unités : " + productService.retirerDuStock(gateway.getIdProduit(), 3));
        System.out.println("Après remise de 10 % : " + productService.appliquerRemise(gateway.getIdProduit(), 10));
        System.out.println("Valeur du stock : " + productService.valeurDuStock() + " DH");

        // 8. Recherches
        System.out.println("Prix entre 50 et 130 : " + productService.produitsEntrePrix(50, 130));
        System.out.println("Désignation contenant \"server\" : " + productService.rechercherParDesignation("server"));

        context.close();
    }

    // La base H2 est conservée entre deux lancements : la 2e fois, la catégorie existe déjà
    // et creerCategorie la refuserait (nom unique). On reprend alors celle qui est en base.
    private static Categorie obtenirCategorie(CategorieService service, String nom, String description) {
        for (Categorie existante : service.toutesLesCategories()) {
            if (existante.getNomCategorie().equalsIgnoreCase(nom)) {
                return existante;
            }
        }
        return service.creerCategorie(new Categorie(nom, description));
    }
}

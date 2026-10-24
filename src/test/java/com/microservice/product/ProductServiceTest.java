package com.microservice.product;

import com.microservice.product.config.AppConfig;
import com.microservice.product.entity.Categorie;
import com.microservice.product.entity.Product;
import com.microservice.product.service.CategorieService;
import com.microservice.product.service.ProductService;
import org.hibernate.SessionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/*
 * Tests de bout en bout : service -> DAO -> Hibernate -> base de données.
 * La base utilisée est H2 en mémoire (réglages dans maven-surefire-plugin du pom.xml) :
 * aucun serveur MySQL n'est nécessaire et les vraies données ne sont jamais touchées.
 * hbm2ddl=create-drop : chaque test repart de tables vides, supprimées quand le contexte se ferme.
 */
public class ProductServiceTest {

    private AnnotationConfigApplicationContext context;
    private ProductService productService;
    private CategorieService categorieService;

    @Before
    public void demarrerSpring() {
        context = new AnnotationConfigApplicationContext(AppConfig.class);
        productService = context.getBean(ProductService.class);
        categorieService = context.getBean(CategorieService.class);
    }

    @After
    public void arreterSpring() {
        context.close();
    }

    // ===== Démarrage =====

    @Test
    public void testSessionFactoryOuverte() {
        assertTrue(context.getBean(SessionFactory.class).isOpen());
    }

    // ===== Produits =====

    @Test
    public void testAjoutEtLecture() {
        Product p = new Product("Microservice Gateway", 149.99, 20);
        productService.addProduct(p);

        // L'id a été attribué par la base au moment de l'insertion
        assertNotNull(p.getIdProduit());
        assertEquals(1, productService.getAllProducts().size());
        Product relu = productService.trouverProduit(p.getIdProduit());
        assertEquals("Microservice Gateway", relu.getDesignation());
        assertEquals(20, relu.getQuantiteStock());
    }

    @Test
    public void testPrixNegatifRefuse() {
        try {
            productService.addProduct(new Product("Invalide", -1));
            fail("Un prix négatif aurait dû être refusé");
        } catch (IllegalArgumentException e) {
            assertEquals("Le prix ne peut pas être négatif", e.getMessage());
        }
        assertTrue(productService.getAllProducts().isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDesignationVideRefusee() {
        productService.addProduct(new Product("  ", 10));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStockNegatifRefuse() {
        productService.addProduct(new Product("Produit", 10, -3));
    }

    @Test
    public void testRollbackAjoutGroupe() {
        // Le 1er produit est déjà enregistré quand le 2e échoue : @Transactional l'annule aussi
        try {
            productService.addProducts(Arrays.asList(new Product("Valide", 10), new Product("Invalide", -5)));
            fail("L'ajout groupé aurait dû échouer");
        } catch (IllegalArgumentException attendu) {
            // attendu
        }
        assertTrue(productService.getAllProducts().isEmpty());
    }

    @Test
    public void testRemise() {
        Product p = new Product("Microservice Gateway", 149.99);
        productService.addProduct(p);

        productService.appliquerRemise(p.getIdProduit(), 10);

        // Relu depuis la base : 149.99 - 10 % = 134.99
        assertEquals(134.99, productService.trouverProduit(p.getIdProduit()).getPrixUnitaire(), 0.001);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemiseTropForteRefusee() {
        Product p = new Product("Produit", 100);
        productService.addProduct(p);
        productService.appliquerRemise(p.getIdProduit(), 95);
    }

    @Test
    public void testRetraitDuStock() {
        Product p = new Product("Config Server", 59.00, 10);
        productService.addProduct(p);

        productService.retirerDuStock(p.getIdProduit(), 4);

        assertEquals(6, productService.trouverProduit(p.getIdProduit()).getQuantiteStock());
    }

    @Test
    public void testStockInsuffisant() {
        Product p = new Product("Config Server", 59.00, 2);
        productService.addProduct(p);
        try {
            productService.retirerDuStock(p.getIdProduit(), 5);
            fail("Le retrait aurait dû être refusé");
        } catch (IllegalArgumentException attendu) {
            // attendu
        }
        // Rien n'a changé en base
        assertEquals(2, productService.trouverProduit(p.getIdProduit()).getQuantiteStock());
    }

    @Test
    public void testValeurDuStock() {
        assertEquals(0, productService.valeurDuStock(), 0.001);
        productService.addProducts(Arrays.asList(
                new Product("A", 10.0, 3),     // 30
                new Product("B", 2.5, 4)));    // 10
        assertEquals(40.0, productService.valeurDuStock(), 0.001);
    }

    @Test
    public void testRecherches() {
        productService.addProducts(Arrays.asList(
                new Product("Microservice Gateway", 149.99),
                new Product("Service Discovery", 89.50),
                new Product("Config Server", 59.00)));

        List<Product> moyens = productService.produitsEntrePrix(50, 100);
        assertEquals(2, moyens.size());
        // Triés du moins cher au plus cher
        assertEquals("Config Server", moyens.get(0).getDesignation());

        assertEquals(1, productService.rechercherParDesignation("GATEWAY").size());
        // "Microservice Gateway" et "Service Discovery"
        assertEquals(2, productService.rechercherParDesignation("service").size());
    }

    @Test
    public void testSuppressionProduit() {
        Product p = new Product("A supprimer", 5);
        productService.addProduct(p);

        productService.supprimerProduit(p.getIdProduit());

        assertTrue(productService.getAllProducts().isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testProduitIntrouvable() {
        productService.trouverProduit(999L);
    }

    // ===== Catégories =====

    @Test
    public void testCreationCategorie() {
        Categorie c = categorieService.creerCategorie(new Categorie("Infrastructure", "Briques techniques"));
        assertNotNull(c.getIdCategorie());
        assertEquals(1, categorieService.toutesLesCategories().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCategorieEnDoubleRefusee() {
        categorieService.creerCategorie(new Categorie("Infrastructure", null));
        // Même nom avec une autre casse : refusé aussi
        categorieService.creerCategorie(new Categorie("INFRASTRUCTURE", null));
    }

    @Test
    public void testAffectationEtProduitsDeLaCategorie() {
        Categorie infra = categorieService.creerCategorie(new Categorie("Infrastructure", null));
        Categorie securite = categorieService.creerCategorie(new Categorie("Sécurité", null));
        Product gateway = new Product("Microservice Gateway", 149.99);
        Product discovery = new Product("Service Discovery", 89.50);
        Product auth = new Product("Auth Server", 120.00);
        productService.addProducts(Arrays.asList(gateway, discovery, auth));

        categorieService.affecterCategorie(gateway.getIdProduit(), infra.getIdCategorie());
        categorieService.affecterCategorie(discovery.getIdProduit(), infra.getIdCategorie());
        categorieService.affecterCategorie(auth.getIdProduit(), securite.getIdCategorie());

        List<Product> produitsInfra = categorieService.produitsDeLaCategorie(infra.getIdCategorie());
        assertEquals(2, produitsInfra.size());
        // La relation @ManyToOne est bien enregistrée : le produit relu connaît sa catégorie
        assertEquals("Sécurité",
                productService.trouverProduit(auth.getIdProduit()).getCategorie().getNomCategorie());
    }

    @Test
    public void testSuppressionCategorieNonVideRefusee() {
        Categorie infra = categorieService.creerCategorie(new Categorie("Infrastructure", null));
        Product p = new Product("Microservice Gateway", 149.99);
        productService.addProduct(p);
        categorieService.affecterCategorie(p.getIdProduit(), infra.getIdCategorie());

        try {
            categorieService.supprimerCategorie(infra.getIdCategorie());
            fail("Une catégorie non vide ne doit pas être supprimée");
        } catch (IllegalStateException attendu) {
            // attendu
        }
        assertEquals(1, categorieService.toutesLesCategories().size());
    }

    @Test
    public void testSuppressionCategorieVide() {
        Categorie c = categorieService.creerCategorie(new Categorie("Temporaire", null));
        categorieService.supprimerCategorie(c.getIdCategorie());
        assertTrue(categorieService.toutesLesCategories().isEmpty());
    }
}

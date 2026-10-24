package com.microservice.product.entity;

import javax.persistence.*;

/*
 * Entité Product : la classe Java qui représente la table "products".
 *   - un objet Product   <-> une ligne de la table
 *   - un attribut        <-> une colonne
 * Hibernate lit les annotations ci-dessous pour savoir comment faire la correspondance.
 */
@Entity
@Table(name = "products")
public class Product {

    // Clé primaire, numérotée automatiquement par la base (1, 2, 3...)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_produit")
    private Long idProduit;

    // Nom commercial du produit, obligatoire, 100 caractères maximum
    @Column(name = "designation", nullable = false, length = 100)
    private String designation;

    // Prix d'une seule unité, en dirhams
    @Column(name = "prix_unitaire", nullable = false)
    private double prixUnitaire;

    // Nombre d'unités disponibles en magasin (jamais négatif, vérifié par le service)
    @Column(name = "quantite_stock", nullable = false)
    private int quantiteStock;

    /*
     * Relation PLUSIEURS produits -> UNE catégorie.
     * En base, cela devient une colonne "id_categorie" (clé étrangère) dans la table products.
     * Elle peut être vide : un produit peut exister sans catégorie.
     * Chargement EAGER par défaut pour un @ManyToOne : la catégorie est lue en même temps que le produit.
     */
    @ManyToOne
    @JoinColumn(name = "id_categorie")
    private Categorie categorie;

    // Hibernate a besoin d'un constructeur sans argument pour recréer les objets lus en base
    public Product() {}

    public Product(String designation, double prixUnitaire) {
        this(designation, prixUnitaire, 0);
    }

    public Product(String designation, double prixUnitaire, int quantiteStock) {
        this.designation = designation;
        this.prixUnitaire = prixUnitaire;
        this.quantiteStock = quantiteStock;
    }

    public Long getIdProduit() { return idProduit; }
    public void setIdProduit(Long idProduit) { this.idProduit = idProduit; }
    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }
    public double getPrixUnitaire() { return prixUnitaire; }
    public void setPrixUnitaire(double prixUnitaire) { this.prixUnitaire = prixUnitaire; }
    public int getQuantiteStock() { return quantiteStock; }
    public void setQuantiteStock(int quantiteStock) { this.quantiteStock = quantiteStock; }
    public Categorie getCategorie() { return categorie; }
    public void setCategorie(Categorie categorie) { this.categorie = categorie; }

    @Override
    public String toString() {
        String nomCategorie = categorie == null ? "sans catégorie" : categorie.getNomCategorie();
        return "Produit#" + idProduit + " [" + designation + ", " + prixUnitaire + " DH, stock "
                + quantiteStock + ", " + nomCategorie + "]";
    }
}

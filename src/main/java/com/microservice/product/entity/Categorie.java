package com.microservice.product.entity;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/*
 * Nouvelle entité (ajout personnel) : la catégorie d'un produit (ex : "Infrastructure", "Sécurité").
 * Table "categories". Une catégorie regroupe plusieurs produits.
 */
@Entity
@Table(name = "categories")
public class Categorie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categorie")
    private Long idCategorie;

    // unique = true : deux catégories ne peuvent pas porter le même nom (contrainte UNIQUE en base)
    @Column(name = "nom_categorie", nullable = false, unique = true, length = 50)
    private String nomCategorie;

    // Texte libre, facultatif
    @Column(name = "description", length = 255)
    private String description;

    /*
     * Côté inverse de la relation : UNE catégorie -> PLUSIEURS produits.
     * mappedBy = "categorie" : la clé étrangère est gérée par l'attribut "categorie" de Product,
     * aucune colonne n'est ajoutée dans la table categories.
     * Chargement LAZY par défaut : la liste n'est lue en base que si on y accède (dans une transaction).
     */
    @OneToMany(mappedBy = "categorie")
    private List<Product> produits = new ArrayList<>();

    public Categorie() {}

    public Categorie(String nomCategorie, String description) {
        this.nomCategorie = nomCategorie;
        this.description = description;
    }

    public Long getIdCategorie() { return idCategorie; }
    public void setIdCategorie(Long idCategorie) { this.idCategorie = idCategorie; }
    public String getNomCategorie() { return nomCategorie; }
    public void setNomCategorie(String nomCategorie) { this.nomCategorie = nomCategorie; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<Product> getProduits() { return produits; }

    // toString n'affiche pas "produits" : hors transaction, lire cette liste LAZY lèverait une exception
    @Override
    public String toString() {
        return "Categorie#" + idCategorie + " [" + nomCategorie + "]";
    }
}

/*
        Classe User qui contient les caractéristiques d'un utilisateur
        Auteur :  MATTEI Paul
        Dernière Modification : 24/06
 */

package com.example.androidapp;

public class User {

    private String nom;

    private String prenom;

    private String photo;

    private String contact;

    private String etudiant;

    private String ancien;

    public String[] items;

    public User(String Nom, String Prenom, String Photo, String Contact, String Etudiant, String Ancien)
    {
        nom = Nom != null && Nom != "" && !Nom.contains("null") ? Nom.toUpperCase() : "";
        prenom = Prenom != null && Prenom != "" && !Prenom.contains("null")? Prenom.trim().substring(0, 1).toUpperCase() + Prenom.trim().substring(1).toLowerCase() : "";
        photo = Photo != null && Photo != "" && !Photo.contains("null")? Photo : "";
        contact = Contact != null && Contact != "" && !Contact.contains("null")? Contact : "";
        etudiant = Etudiant != null && Etudiant != "" && !Etudiant.contains("null")? Etudiant : Etudiant != null && Etudiant != "" ? Etudiant.split("TD")[0]: "";
        ancien = Ancien != null && Ancien != "" && !Ancien.contains("null")? Ancien : "";

        items = new String[]{nom, prenom, photo, contact, etudiant, ancien};
    }
}

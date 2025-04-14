package tn.esprit.services;

import tn.esprit.entities.Categorie;
import java.util.List;

public interface ICategorieService {
    void ajouter(Categorie categorie);
    void modifier(Categorie categorie);
    void supprimer(int id);
    List<Categorie> findAll();
    Categorie findById(int id);
    List<Categorie> searchByName(String name);
}
package interfaces;


import models.MaterielLocation;
import models.MaterielVente;

import java.util.List;

public interface IMaterielService {
    void ajouterLocation(MaterielLocation materiel);
    void modifierLocation(MaterielLocation materiel);
    List<MaterielLocation> findAllLocation();
    List<MaterielLocation> searchLocation(String query);

    void ajouterVente(MaterielVente materiel);
    void modifierVente(MaterielVente materiel);
    List<MaterielVente> findAllVente();
    List<MaterielVente> searchVente(String query);
    List<MaterielVente> findByCategorie(int categorieId);

    void supprimer(int id, String type);
}
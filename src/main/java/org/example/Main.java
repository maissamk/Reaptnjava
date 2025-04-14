package org.example;

import models.ParcelleProprietes;
import org.example.utils.MaConnexion;
import services.ParcelleProprietesService;
import java.util.Date;
import java.sql.Connection;
import java.sql.Timestamp;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {

    Connection conn;
    conn=MaConnexion.getInstance().getConn();
    System.out.println(conn);
        //TIP Press <shortcut actionId="ShowIntentionActions"/> with your caret at the highlighted text
        // to see how IntelliJ IDEA suggests fixing it.
        ParcelleProprietesService ps = new ParcelleProprietesService();

        ParcelleProprietes parcelle = new ParcelleProprietes(
                "Ferme bio",                              // titre
                "Terrain agricole de 2 hectares",         // description
                200000.0,                                  // prix
                "disponible",                              // status
                "Tunis",                                   // emplacement
                2.0,                                       // taille
                new Date(),                                // date_creation_annonce
                new Date(),                                // date_misajour_annonce
                true,                                      // est_disponible
                "Ali Ferjani",                             // nom_proprietaire
                "22 333 444",                              // contact_proprietaire
                "image.png",                               // image
                1,                                         // user_id_parcelle_id
                "résidentiel",                             // type_terrain
                "36.8065",                                 // latitude
                "10.1815",                                 // longitude
                "ali.ferjani@example.com"                  // email
        );

        ps.add(parcelle);
        for (ParcelleProprietes p : ps.getAll()) {
            System.out.println(p);
        }


    }
}
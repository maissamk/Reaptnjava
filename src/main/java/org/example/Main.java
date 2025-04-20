package org.example;

import models.gestionCommande.Commande;
import services.gestionCommande.CommandeService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        CommandeService cm = new CommandeService();

        try {
            // Conversion de la date String vers java.util.Date
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
            Date date = sdf.parse("12-04-2025");

            // Création et ajout de la commande
            cm.add(new Commande(4, date, 958));

        } catch (ParseException e) {
            System.out.println("Erreur de parsing de la date : " + e.getMessage());
        }


        System.out.println(cm.getAll());
    }
}

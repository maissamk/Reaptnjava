package org.example.utils;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MaConnexion {
    private  final String URL = "jdbc:mysql://localhost:3306/reaptn";
    private  final String USER = "root";
    private  final String PASSWORD = "";
    Connection connection;

    static MaConnexion instance;

    public static MaConnexion getInstance()  {
        if (instance == null) {
            instance = new MaConnexion();
        }
        return instance;
    }
    public Connection getConn() {
        return connection;

    }



    private MaConnexion() {
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connexion établie avec succès");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
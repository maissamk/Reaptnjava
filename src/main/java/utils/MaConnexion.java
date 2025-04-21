package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MaConnexion {

    final String url="jdbc:mysql://localhost:3306/reaptn";
    final String username="root";
    final String password="";

    static MaConnexion instance;

    Connection connection;


    public static MaConnexion getInstance()
    {
        if (instance==null){
            instance=new MaConnexion();
        }
        return instance;
    }

    public Connection getConn(){
        return connection;
    }

    private MaConnexion(){
        try {
            connection = DriverManager.getConnection(url, username, password);
            System.out.println("Connexion etablie avec succés");
        }catch (SQLException e){
            System.out.println(e.getMessage());
        }
    }


}

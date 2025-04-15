package utils;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
public class MaConnexion {
    private   final   String url="jdbc:mysql://root:@127.0.0.1:3306/reaptn";
    private   final   String user ="root";
    private   final   String pws ="";

    private Connection connection;
    static MaConnexion instance;

    public static MaConnexion getInstance(){
        if (instance==null){
            instance= new MaConnexion();

        }
        return instance;
    }

    private MaConnexion()
    {
        try {
            connection= DriverManager.getConnection(url,user,pws);
            System.out.println("connecter a la base de données");
        } catch (SQLException e) {
            System.err.println(e.getMessage());    }
    }
    public Connection getConnection() {
        return connection;
    }
}

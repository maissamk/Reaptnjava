package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDataBase {
    private final String URL="jdbc:mysql://localhost:3306/reaptn";
    private final String USER="root";
    private final String PSW="";


private Connection connection;
private static MyDataBase instance;
    private MyDataBase(){
        try {
            myConnection = DriverManager.getConnection(URL,USER,PSW);
            connection= DriverManager.getConnection(URL,USER,PSW);
            System.out.println("Connected");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

public static MyDataBase getInstance(){
    if (instance==null){
     instance= new MyDataBase();

    }
    return instance;
}

    public Connection getConnection() {
        return connection;
    }

    private Connection myConnection;




    public Connection getCnx() {
        return myConnection;
    }


}

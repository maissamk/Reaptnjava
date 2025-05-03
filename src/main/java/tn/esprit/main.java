package tn.esprit;

import Models.user;
import services.UserServices;
import utils.MaConnexion;

import java.sql.Connection;

public class main {
    public static void main(String[] args) {

        Connection conn;
        conn=MaConnexion.getInstance().getConn();

        System.out.println(conn);


        UserServices us=new UserServices();
//        us.add(new user("foulen@esprit.tn","Romdhani789", "ROLE_CLIENT","foiulen","felen","25220551","online"));


        System.out.println(us.getAll());


    }
}

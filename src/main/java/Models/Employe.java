
package Models;

import java.time.LocalDateTime;
import java.util.ArrayList;


public class Employe {
    private int id;
    private int offre_id,user_identifier;
    private boolean conf,suggested;
    private String comp;
    private  ArrayList<String> dispo;
    private LocalDateTime date_join;
    private Offre offre;

    ////////////////////////////////CONSTRUCTEURS/////////////////////////////////////////////


    public Employe() {}

    public Employe(int id, Integer offre_id, Integer user_identifier, boolean conf, boolean suggested, String comp, ArrayList dispo, LocalDateTime date_join) {
        this.id = id;
        this.offre_id = offre_id;
        this.user_identifier = user_identifier;
        this.conf = conf;
        this.suggested = suggested;
        this.comp = comp;
        this.dispo = dispo;
        this.date_join = date_join;
    }
    public Employe(Integer offre_id, Integer user_identifier, boolean conf, boolean suggested, String comp, ArrayList dispo, LocalDateTime date_join) {

        this.offre_id = offre_id;
        this.user_identifier = user_identifier;
        this.conf = conf;
        this.suggested = suggested;
        this.comp = comp;
        this.dispo = dispo;
        this.date_join = date_join;
    }
    public Employe(Integer user_identifier, boolean conf, boolean suggested, String comp, ArrayList dispo, LocalDateTime date_join) {

        this.offre_id = offre_id;
        this.user_identifier = user_identifier;
        this.conf = conf;
        this.suggested = suggested;
        this.comp = comp;
        this.dispo = dispo;
        this.date_join = date_join;
    }


    ////////////////////////////////GETTERS ET SETTERS////////////////////////////////////////


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getOffre_id() {
        return offre_id;
    }

    public void setOffre_id(Integer offre_id) {
        this.offre_id = offre_id;
    }

    public Integer getUser_identifier() {
        return user_identifier;
    }

    public void setUser_identifier(Integer user_identifier) {
        this.user_identifier = user_identifier;
    }



    public boolean isSuggested() {
        return suggested;
    }

    public void setSuggested(boolean suggested) {
        this.suggested = suggested;
    }

    public boolean isConf() {
        return conf;
    }

    public void setConf(boolean conf) {
        this.conf = conf;
    }

    public String getComp() {
        return comp;
    }

    public void setComp(String comp) {
        this.comp = comp;
    }

    public void setDispo(ArrayList<String> dispo) {
        this.dispo = dispo;
    }
    public ArrayList<String> getDispo() {
        return dispo;
    }





    public LocalDateTime getDate_join() {
        return date_join;
    }

    public void setDate_join(LocalDateTime date_join) {
        this.date_join = date_join;
    }


    ////////////////////////////////OVERRIDES////////////////////////////////////////


    @Override
    public String toString() {
        return "Employe{" +
                "id=" + id +
                ", offre_id=" + offre_id +
                ", user_identifier=" + user_identifier +
                ", conf=" + conf +
                ", suggested=" + suggested +
                ", comp='" + comp + '\'' +
                ", dispo='" + dispo + '\'' +
                ", date_join=" + date_join +
                '}';
    }
}

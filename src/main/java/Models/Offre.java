package Models;

public class Offre {
    private int id;
    private Integer ida,ide;
    private boolean statut = false;
    private String descr,titre,comp ;

    ////////////////////////////////CONSTRUCTEURS/////////////////////////////////////////////

    public Offre() {}
    public Offre( Integer ida, Integer ide, boolean statut, String descr, String titre, String comp) {

        this.ida = ida;
        this.ide = ide;
        this.statut = statut;
        this.descr = descr;
        this.titre = titre;
        this.comp = comp;
    }
    public Offre(int id, Integer ida, Integer ide, boolean statut, String descr, String titre, String comp) {
        this.id = id;
        this.ida = ida;
        this.ide = ide;
        this.statut = statut;
        this.descr = descr;
        this.titre = titre;
        this.comp = comp;
    }


    public Offre(Integer ida, boolean statut, String descr, String titre, String comp) {

        this.ida = ida;

        this.statut = statut;
        this.descr = descr;
        this.titre = titre;
        this.comp = comp;
    }
    ////////////////////////////////GETTERS ET SETTERS////////////////////////////////////////

    public int getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getIda() {
        return ida;
    }

    public void setIda(Integer ida) {
        this.ida = ida;
    }

    public Integer getIde() {
        return ide;
    }

    public void setIde(Integer ide) {
        this.ide = ide;
    }

    public boolean isStatut() {
        return statut;
    }

    public void setStatut(boolean statut) {
        this.statut = statut;
    }

    public String getDescr() {
        return descr;
    }

    public void setDescr(String descr) {
        this.descr = descr;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getComp() {
        return comp;
    }

    public void setComp(String comp) {
        this.comp = comp;
    }

    ////////////////////////////////OVERRIDES////////////////////////////////////////

    @Override
    public String toString() {
        return "Offre{" +
                "id=" + id +
                ", ida=" + ida +
                ", ide=" + ide +
                ", statut=" + statut +
                ", descr='" + descr + '\'' +
                ", titre='" + titre + '\'' +
                ", comp='" + comp + '\'' +
                '}';
    }
}

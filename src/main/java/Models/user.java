package Models;

public class user {
    private int id;
    private String email;
    private String password;
    private String roles;
    private String nom;
    private String prenom;
    private String telephone;
    private String status;
    private String avatar;



    public user() {}

    @Override
    public String toString() {
        return "user{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", roles='" + roles + '\'' +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", telephone='" + telephone + '\'' +
                ", status='" + status + '\'' +
                ", avatar='" + avatar + '\'' +
                '}';
    }

    public user(String email, String password, String roles, String nom, String prenom, String telephone, String status, String avatar) {
        this.email = email;
        this.password = password;
        this.roles = roles;
        this.nom = nom;
        this.prenom = prenom;
        this.telephone = telephone;
        this.status = status;
        this.avatar = avatar;
    }

    public user(int id, String email, String password, String roles, String nom, String prenom, String telephone, String status, String avatar) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.roles = roles;
        this.nom = nom;
        this.prenom = prenom;
        this.telephone = telephone;
        this.status = status;
        this.avatar = avatar;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}

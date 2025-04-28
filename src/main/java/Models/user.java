package Models;

import java.time.LocalDateTime;

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
    private String face_token;
    private String verificationCode;
    private LocalDateTime verificationSentAt;
    private boolean isVerified = false;
    private String resetToken;
    private LocalDateTime tokenExpiry;
    private String googleId;
    private Integer loginAttempts;


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
                ", face_token='" + face_token + '\'' +
                ", verificationCode='" + verificationCode + '\'' +
                ", verificationSentAt=" + verificationSentAt +
                ", isVerified=" + isVerified +
                ", resetToken='" + resetToken + '\'' +
                ", tokenExpiry=" + tokenExpiry +
                ", googleId='" + googleId + '\'' +
                ", loginAttempts=" + loginAttempts +
                '}';
    }

    public user(String email, String password, String roles, String nom, String prenom, String telephone, String status, String avatar, String face_token) {
        this.email = email;
        this.password = password;
        this.roles = roles;
        this.nom = nom;
        this.prenom = prenom;
        this.telephone = telephone;
        this.status = status;
        this.avatar = avatar;
        this.face_token = face_token;

    }

    public user(int id, String email, String password, String roles, String nom, String prenom, String telephone, String status, String avatar, String face_token) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.roles = roles;
        this.nom = nom;
        this.prenom = prenom;
        this.telephone = telephone;
        this.status = status;
        this.avatar = avatar;
        this.face_token = face_token;
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
    public String getFace_token() {
        return face_token;
    }

    public void setFace_token(String face_token) {
        this.face_token = face_token;
    }
    public String getVerificationCode() {
        return verificationCode;
    }

    public void setVerificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
    }

    public LocalDateTime getVerificationSentAt() {
        return verificationSentAt;
    }

    public void setVerificationSentAt(LocalDateTime verificationSentAt) {
        this.verificationSentAt = verificationSentAt;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
    }
    public String getResetToken() {
        return resetToken;
    }

    public void setResetToken(String resetToken) {
        this.resetToken = resetToken;
    }

    public LocalDateTime getTokenExpiry() {
        return tokenExpiry;
    }

    public void setTokenExpiry(LocalDateTime tokenExpiry) {
        this.tokenExpiry = tokenExpiry;
    }
    public String getGoogleId() {
        return googleId;
    }

    public void setGoogleId(String googleId) {
        this.googleId = googleId;
    }
    public Integer getLoginAttempts() {
        return loginAttempts;
    }

    public void setLoginAttempts(Integer loginAttempts) {
        this.loginAttempts = loginAttempts;
    }
}

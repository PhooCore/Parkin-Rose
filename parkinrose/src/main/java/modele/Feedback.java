package modele;

import java.time.LocalDateTime;

public class Feedback {
    private int idFeedback;
    private int idUsager;
    private String sujet;
    private String message;
    private LocalDateTime dateCreation;
    private String statut;
    private boolean gotanswer;
    private Integer idAdminReponse;
    private Integer idFeedbackParent;
    private LocalDateTime dateReponse;
    private String reponse;
    
    // Pour l'affichage (non persisté)
    private String nomUsager;
    private String prenomUsager;
    private String mailUsager;
    private String nomAdminReponse;
    private String prenomAdminReponse;
    
    public Feedback() {
    }
    
    public Feedback(int idUsager, String sujet, String message) {
        this.idUsager = idUsager;
        this.sujet = sujet;
        this.message = message;
        this.dateCreation = LocalDateTime.now();
        this.statut = "NOUVEAU";
        this.gotanswer = false;
    }
    
    // Getters et Setters
    public int getIdFeedback() {
        return idFeedback;
    }
    
    public void setIdFeedback(int idFeedback) {
        this.idFeedback = idFeedback;
    }
    
    public int getIdUsager() {
        return idUsager;
    }
    
    public void setIdUsager(int idUsager) {
        this.idUsager = idUsager;
    }
    
    public String getSujet() {
        return sujet;
    }
    
    public void setSujet(String sujet) {
        this.sujet = sujet;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public LocalDateTime getDateCreation() {
        return dateCreation;
    }
    
    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }
    
    public String getStatut() {
        return statut;
    }
    
    public void setStatut(String statut) {
        this.statut = statut;
    }
    
    public boolean isGotanswer() {
        return gotanswer;
    }
    
    public void setGotanswer(boolean gotanswer) {
        this.gotanswer = gotanswer;
    }
    
    public Integer getIdAdminReponse() {
        return idAdminReponse;
    }
    
    public void setIdAdminReponse(Integer idAdminReponse) {
        this.idAdminReponse = idAdminReponse;
    }
    
    public Integer getIdFeedbackParent() {
        return idFeedbackParent;
    }
    
    public void setIdFeedbackParent(Integer idFeedbackParent) {
        this.idFeedbackParent = idFeedbackParent;
    }
    
    public LocalDateTime getDateReponse() {
        return dateReponse;
    }
    
    public void setDateReponse(LocalDateTime dateReponse) {
        this.dateReponse = dateReponse;
    }
    
    public String getReponse() {
        return reponse;
    }
    
    public void setReponse(String reponse) {
        this.reponse = reponse;
    }
    
    // Getters et Setters pour les informations d'affichage
    public String getNomUsager() {
        return nomUsager;
    }
    
    public void setNomUsager(String nomUsager) {
        this.nomUsager = nomUsager;
    }
    
    public String getPrenomUsager() {
        return prenomUsager;
    }
    
    public void setPrenomUsager(String prenomUsager) {
        this.prenomUsager = prenomUsager;
    }
    
    public String getMailUsager() {
        return mailUsager;
    }
    
    public void setMailUsager(String mailUsager) {
        this.mailUsager = mailUsager;
    }
    
    public String getNomAdminReponse() {
        return nomAdminReponse;
    }
    
    public void setNomAdminReponse(String nomAdminReponse) {
        this.nomAdminReponse = nomAdminReponse;
    }
    
    public String getPrenomAdminReponse() {
        return prenomAdminReponse;
    }
    
    public void setPrenomAdminReponse(String prenomAdminReponse) {
        this.prenomAdminReponse = prenomAdminReponse;
    }
    
    // Méthodes utilitaires
    public String getNomCompletUsager() {
        if (prenomUsager != null && nomUsager != null) {
            return prenomUsager + " " + nomUsager;
        }
        return "Utilisateur #" + idUsager;
    }
    
    public String getNomCompletAdminReponse() {
        if (prenomAdminReponse != null && nomAdminReponse != null) {
            return prenomAdminReponse + " " + nomAdminReponse;
        }
        return idAdminReponse != null ? "Admin #" + idAdminReponse : null;
    }
    
    public boolean isRepondu() {
        return gotanswer || idAdminReponse != null;
    }
    
    public boolean estUnMessageParent() {
        return idFeedbackParent == null;
    }
    
    @Override
    public String toString() {
        return "Feedback{" +
               "id=" + idFeedback +
               ", sujet='" + sujet + '\'' +
               ", statut='" + statut + '\'' +
               ", date=" + dateCreation +
               '}';
    }
}
package modele;

public class Usager {
    private int idUsager;
    private String nomUsager;
    private String prenomUsager;
    private String mailUsager;
    private String motDePasse;
    private String numeroCarteTisseo;
    private String adresse;
    private String codePostal;
    private String ville;
    private String idZoneResidentielle;
    private boolean isAdmin;
  

    public Usager(String nomUsager, String prenomUsager, String mailUsager, String motDePasse) {
        this.nomUsager = nomUsager;
        this.prenomUsager = prenomUsager;
        this.mailUsager = mailUsager;
        this.motDePasse = motDePasse;
        this.numeroCarteTisseo = null;
    }

    public Usager() {}

    public int getIdUsager() { 
        return idUsager; 
    }
    
    public String getNomUsager() { 
        return nomUsager; 
    }
    
    public String getPrenomUsager() { 
        return prenomUsager; 
    }
    
    public String getMailUsager() { 
        return mailUsager; 
    }
    
    public String getMotDePasse() { 
        return motDePasse; 
    }

    public void setIdUsager(int idUsager) { 
        this.idUsager = idUsager; 
    }
    
    public void setNomUsager(String nomUsager) { 
        this.nomUsager = nomUsager; 
    }
    
    public void setPrenomUsager(String prenomUsager) { 
        this.prenomUsager = prenomUsager; 
    }
    
    public void setMailUsager(String mailUsager) { 
        this.mailUsager = mailUsager; 
    }
    
    public void setMotDePasse(String motDePasse) { 
        this.motDePasse = motDePasse; 
    }
    
    public String getNumeroCarteTisseo() {
        return numeroCarteTisseo;
    }

    public void setNumeroCarteTisseo(String numeroCarteTisseo) {
        this.numeroCarteTisseo = numeroCarteTisseo;
    }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }
    
    public String getCodePostal() { return codePostal; }
    public void setCodePostal(String codePostal) { this.codePostal = codePostal; }
    
    public String getVille() { return ville; }
    public void setVille(String ville) { this.ville = ville; }
    
    public String getIdZoneResidentielle() { return idZoneResidentielle; }
    public void setIdZoneResidentielle(String idZoneResidentielle) { 
        this.idZoneResidentielle = idZoneResidentielle; 
    }
    
    // Méthode pour obtenir l'adresse complète
    public String getAdresseComplete() {
        if (adresse == null || adresse.trim().isEmpty()) {
            return "Non renseignée";
        }
        StringBuilder sb = new StringBuilder(adresse);
        if (codePostal != null && !codePostal.trim().isEmpty()) {
            sb.append(", ").append(codePostal);
        }
        if (ville != null && !ville.trim().isEmpty()) {
            sb.append(" ").append(ville);
        }
        return sb.toString();
    }
    
    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }
}
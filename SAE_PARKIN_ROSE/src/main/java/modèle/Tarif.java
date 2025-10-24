package modèle;

import java.time.LocalTime;

public class Tarif {
    private String idTarification;
    private double tarifParHeure;
    private LocalTime dureeMax;
    private String nomZone;

    public Tarif(String idTarification, double tarifParHeure, LocalTime dureeMax) {
        this.idTarification = idTarification;
        this.tarifParHeure = tarifParHeure;
        this.dureeMax = dureeMax;
        this.nomZone = idTarification.replace("TARIF_", "");
    }

    public String getIdTarification() { return idTarification; }
    public double getTarifParHeure() { return tarifParHeure; }
    public LocalTime getDureeMax() { return dureeMax; }
    public String getNomZone() { return nomZone; }


    public int getDureeMaxMinutes() {
        return dureeMax.getHour() * 60 + dureeMax.getMinute();
    }


    public double calculerCout(int dureeMinutes) {
        if (idTarification.equals("TARIF_SOIREE")) {
            return 5.90;
        }
        
        if (idTarification.equals("TARIF_BLEUE")) {
            return 0.00;
        }
        
        double dureeHeures = dureeMinutes / 60.0;
        return dureeHeures * tarifParHeure;
    }

    // Méthode pour l'affichage
    public String getAffichage() {
        String nom = nomZone.substring(0, 1).toUpperCase() + nomZone.substring(1).toLowerCase();
        
        if (idTarification.equals("TARIF_SOIREE")) {
            return "Zone " + nom + " - " + String.format("%.2f", tarifParHeure) + "€ forfait (" + 
                   dureeMax.getHour() + "h" + (dureeMax.getMinute() > 0 ? dureeMax.getMinute() : "") + ")";
        } else if (idTarification.equals("TARIF_BLEUE")) {
            return "Zone " + nom + " - Gratuite (max " + 
                   dureeMax.getHour() + "h" + (dureeMax.getMinute() > 0 ? dureeMax.getMinute() : "") + ")";
        } else {
            return "Zone " + nom + " - " + String.format("%.2f", tarifParHeure) + "€/h (max " + 
                   dureeMax.getHour() + "h" + (dureeMax.getMinute() > 0 ? dureeMax.getMinute() : "") + ")";
        }
    }
}
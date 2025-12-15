package controleur;

import ihm.Page_Administration;
import ihm.PageGestionUtilisateurs;
import ihm.Page_Principale;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ControleurAdministration implements ActionListener {
    
    private Page_Administration vue;
    private String emailAdmin;
    
    public ControleurAdministration(Page_Administration vue, String emailAdmin) {
        this.vue = vue;
        this.emailAdmin = emailAdmin;
        configurerListeners();
    }
    
    private void configurerListeners() {
        // Dans cette version, les actions sont directement gérées dans la page
        // Mais on peut ajouter des validations ou logiques supplémentaires ici si besoin
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        // Les actions sont gérées directement dans la page pour simplifier
    }
    
    // Méthodes utilitaires
    public void ouvrirGestionUtilisateurs() {
        if (verifierDroitsAdmin()) {
            new PageGestionUtilisateurs(emailAdmin);
            vue.dispose();
        }
    }
    
    public void ouvrirGestionParkings() {
        if (verifierDroitsAdmin()) {
            // La carte sera ouverte depuis la page d'administration
            // Cette méthode est appelée depuis le bouton de la page
        }
    }
    
    private boolean verifierDroitsAdmin() {
        // Vérification simplifiée - normalement tu devrais vérifier dans la base
        return emailAdmin != null && (emailAdmin.contains("admin") || emailAdmin.equals("admin@pr.com"));
    }
}
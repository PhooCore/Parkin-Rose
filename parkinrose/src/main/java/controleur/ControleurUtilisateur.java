package controleur;

import modele.Usager;
import modele.dao.UsagerDAO;
import ihm.Page_Utilisateur;
import ihm.Page_Authentification;
import ihm.Page_Modif_MDP;
import ihm.Page_Principale;
import ihm.Page_Abonnements;
import ihm.Page_Historique_Stationnements;
import ihm.Page_Gestion_Vehicules;
import javax.swing.JOptionPane;
import javax.swing.JButton;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ControleurUtilisateur implements ActionListener {
    
    private Page_Utilisateur vue;
    private String emailUtilisateur;
    private Usager usager;
    
    public ControleurUtilisateur(Page_Utilisateur vue) {
        this.vue = vue;
        this.emailUtilisateur = vue.getEmailUtilisateur();
        
        // Récupérer les données de l'utilisateur
        if (emailUtilisateur != null) {
            this.usager = UsagerDAO.getUsagerByEmail(emailUtilisateur);
        }
        
        configurerListeners();
    }
    
    private void configurerListeners() {
        // Configuration des boutons
        if (vue.getBtnModifierMdp() != null) {
            vue.getBtnModifierMdp().addActionListener(this);
        }
        
        if (vue.getBtnDeconnexion() != null) {
            vue.getBtnDeconnexion().addActionListener(this);
        }
        
        if (vue.getBtnRetour() != null) {
            vue.getBtnRetour().addActionListener(this);
        }
        
        // Nouveau bouton pour gérer les véhicules
        if (vue.getBtnGestionVehicules() != null) {
            vue.getBtnGestionVehicules().addActionListener(this);
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        
        if (source == vue.getBtnModifierMdp()) {
            ouvrirModificationMotDePasse();
        } else if (source == vue.getBtnGestionVehicules()) {
            ouvrirGestionVehicules();
        } else if (source == vue.getBtnDeconnexion()) {
            deconnecterUtilisateur();
        } else if (source == vue.getBtnRetour()) {
            retourAccueil();
        }
    }
    
    /**
     * Ouvre la page de gestion des véhicules
     */
    private void ouvrirGestionVehicules() {
        if (usager == null) {
            JOptionPane.showMessageDialog(vue,
                "Utilisateur non trouvé. Veuillez vous reconnecter.",
                "Erreur",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Créer la page de gestion des véhicules
        Page_Gestion_Vehicules pageVehicules = new Page_Gestion_Vehicules(emailUtilisateur);
        pageVehicules.setVisible(true);
        
        // Masquer la page utilisateur temporairement
        vue.setVisible(false);
        
        // Ajouter un listener pour détecter la fermeture de la page
        pageVehicules.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                // Lorsque la page de gestion des véhicules est fermée, réafficher la page utilisateur
                vue.setVisible(true);
                vue.toFront();
            }
            
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                // Même traitement que windowClosed
                vue.setVisible(true);
                vue.toFront();
            }
        });
    }
    
    /**
     * Ouvre la page de modification du mot de passe
     */
    private void ouvrirModificationMotDePasse() {
        // Créer la page de modification de mot de passe
        Page_Modif_MDP pageModifMdp = new Page_Modif_MDP(emailUtilisateur, vue);
        pageModifMdp.setVisible(true);
        
        // Masquer la page utilisateur (elle sera réaffichée au retour)
        vue.setVisible(false);
    }
    
    /**
     * Déconnecte l'utilisateur et retourne à la page d'authentification
     */
    private void deconnecterUtilisateur() {
        // Demander confirmation
        int confirmation = JOptionPane.showConfirmDialog(vue,
            "Êtes-vous sûr de vouloir vous déconnecter ?",
            "Déconnexion",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
            
        if (confirmation == JOptionPane.YES_OPTION) {
            // Afficher message de confirmation
            JOptionPane.showMessageDialog(vue,
                "Déconnexion réussie !\nÀ bientôt sur Parkin'Rose.",
                "Déconnexion",
                JOptionPane.INFORMATION_MESSAGE);
            
            // Ouvrir la page d'authentification
            Page_Authentification authPage = new Page_Authentification();
            authPage.setVisible(true);
            
            // Fermer toutes les fenêtres de l'utilisateur
            fermerFenetresUtilisateur();
        }
    }
    
    /**
     * Ferme toutes les fenêtres ouvertes pour cet utilisateur
     */
    private void fermerFenetresUtilisateur() {
        // Fermer la page utilisateur actuelle
        if (vue != null && vue.isVisible()) {
            vue.dispose();
        }
        
        // Parcourir toutes les fenêtres ouvertes
        Window[] windows = Window.getWindows();
        for (Window window : windows) {
            if (window != vue && window.isVisible()) {
                // Vérifier si c'est une fenêtre de l'application liée à cet utilisateur
                if (window instanceof Page_Principale || 
                    window instanceof Page_Abonnements ||
                    window instanceof Page_Modif_MDP ||
                    window instanceof Page_Historique_Stationnements ||
                    window instanceof Page_Gestion_Vehicules) { // Ajout de la nouvelle page
                    window.dispose();
                }
            }
        }
    }
    
    /**
     * Retourne à la page principale (accueil)
     */
    private void retourAccueil() {
        // Rechercher si une page principale existe déjà
        Page_Principale pagePrincipale = trouverPagePrincipaleExistante();
        
        if (pagePrincipale != null) {
            // Utiliser la page existante
            pagePrincipale.setVisible(true);
            pagePrincipale.toFront(); // Mettre au premier plan
        } else {
            // Créer une nouvelle page principale
            pagePrincipale = new Page_Principale(emailUtilisateur);
            pagePrincipale.setVisible(true);
        }
        
        // Fermer la page utilisateur
        vue.dispose();
    }
    
    /**
     * Cherche une instance existante de Page_Principale
     */
    private Page_Principale trouverPagePrincipaleExistante() {
        Window[] windows = Window.getWindows();
        for (Window window : windows) {
            if (window instanceof Page_Principale && window.isVisible()) {
                return (Page_Principale) window;
            }
        }
        return null;
    }
    
    /**
     * Méthode appelée par Page_Modif_MDP après modification du mot de passe
     */
    public void onRetourModificationMDP(boolean modificationReussie) {
        // Réafficher la page utilisateur
        vue.setVisible(true);
        
        if (modificationReussie) {
            JOptionPane.showMessageDialog(vue,
                "Mot de passe modifié avec succès !\n" +
                "Votre nouveau mot de passe est maintenant actif.",
                "Modification réussie",
                JOptionPane.INFORMATION_MESSAGE);
            
            // Rafraîchir les données si nécessaire
            rechargerDonneesUtilisateur();
        }
    }
    
    /**
     * Recharge les données de l'utilisateur depuis la base
     */
    public void rechargerDonneesUtilisateur() {
        if (emailUtilisateur != null) {
            this.usager = UsagerDAO.getUsagerByEmail(emailUtilisateur);
        }
    }
    
    // Getters
    public Usager getUsager() {
        return usager;
    }
    
    public String getEmailUtilisateur() {
        return emailUtilisateur;
    }
}
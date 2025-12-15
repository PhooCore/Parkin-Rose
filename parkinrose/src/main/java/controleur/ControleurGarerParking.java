package controleur;

import ihm.Page_Garer_Parking;
import javax.swing.JButton;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;
import modele.dao.UsagerDAO;
import modele.dao.VehiculeUsagerDAO;
import modele.Usager;
import modele.VehiculeUsager;

public class ControleurGarerParking implements ActionListener {
    
    private enum EtatParking {
        INITIAL,
        SELECTION,
        VERIFICATION,
        RESERVATION,
        REDIRECTION
    }
    
    private Page_Garer_Parking vue;
    private EtatParking etat;
    private StationnementControleur controleur;
    private String emailUtilisateur; // Déclarer la variable
    private Usager usager; // Stocker l'usager
    
    public ControleurGarerParking(Page_Garer_Parking vue) {
        this.vue = vue;
        this.emailUtilisateur = vue.emailUtilisateur; // Récupérer depuis la vue
        this.usager = UsagerDAO.getUsagerByEmail(emailUtilisateur); // Récupérer l'usager
        this.controleur = new StationnementControleur(emailUtilisateur);
        this.etat = EtatParking.INITIAL;
        configurerListeners();
        
        etat = EtatParking.SELECTION;
        chargerVehiculePrincipal(); // Charger le véhicule principal
    }
    
    private void chargerVehiculePrincipal() {
        if (usager != null) {
            VehiculeUsager vehiculePrincipal = VehiculeUsagerDAO.getVehiculePrincipal(usager.getIdUsager());
            if (vehiculePrincipal != null) {
                vue.lblPlaque.setText(vehiculePrincipal.getPlaqueImmatriculation());
                // Mettre à jour le type de véhicule dans la vue
                String typeVehicule = vehiculePrincipal.getTypeVehicule();
                if ("Voiture".equals(typeVehicule)) {
                    vue.radioVoiture.setSelected(true);
                } else if ("Moto".equals(typeVehicule)) {
                    vue.radioMoto.setSelected(true);
                } else if ("Camion".equals(typeVehicule)) {
                    vue.radioCamion.setSelected(true);
                }
            } else {
                // Si pas de véhicule principal, essayer d'en avoir un autre
                java.util.List<VehiculeUsager> vehicules = VehiculeUsagerDAO.getVehiculesByUsager(usager.getIdUsager());
                if (!vehicules.isEmpty()) {
                    vue.lblPlaque.setText(vehicules.get(0).getPlaqueImmatriculation());
                }
            }
        }
    }
    
    private void configurerListeners() {
        // Ajouter ActionListener aux boutons
        vue.getBtnAnnuler().addActionListener(this);
        vue.getBtnReserver().addActionListener(this);
        vue.getBtnModifierPlaque().addActionListener(this);
        
        // Ajouter ItemListener au combobox
        vue.comboParking.addItemListener(e -> {
            if (etat == EtatParking.SELECTION && e.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
                mettreAJourInfosParking(vue.comboParking.getSelectedIndex());
            }
        });
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        String action = getActionBouton((JButton) e.getSource());
        
        switch (etat) {
            case SELECTION:
                if (action.equals("ANNULER")) {
                    etat = EtatParking.REDIRECTION;
                    annuler();
                } else if (action.equals("RESERVER")) {
                    etat = EtatParking.VERIFICATION;
                    verifierReservation();
                } else if (action.equals("MODIFIER_PLAQUE")) {
                    modifierPlaque();
                }
                break;
                
            case VERIFICATION:
                if (action.equals("CONFIRMER")) {
                    etat = EtatParking.RESERVATION;
                    reserverPlace();
                } else if (action.equals("MODIFIER")) {
                    etat = EtatParking.SELECTION;
                }
                break;
                
            case REDIRECTION:
                // Ne rien faire
                break;
        }
    }
    
    private void verifierReservation() {
        int index = vue.comboParking.getSelectedIndex();
        if (index >= 0 && index < vue.listeParkings.size()) {
            modele.Parking parking = vue.listeParkings.get(index);
            String typeVehicule = vue.getTypeVehicule();
            
            // Vérification pour les motos
            if ("Moto".equals(typeVehicule)) {
                if (!parking.hasMoto()) {
                    JOptionPane.showMessageDialog(vue,
                        "Ce parking ne dispose pas de places pour les motos",
                        "Parking non adapté",
                        JOptionPane.WARNING_MESSAGE);
                    etat = EtatParking.SELECTION;
                    return;
                }
                if (parking.getPlacesMotoDisponibles() <= 0) {
                    JOptionPane.showMessageDialog(vue,
                        "Plus de places moto disponibles dans ce parking",
                        "Parking complet",
                        JOptionPane.WARNING_MESSAGE);
                    etat = EtatParking.SELECTION;
                    return;
                }
            }
            
            // Demander confirmation
            int choix = JOptionPane.showConfirmDialog(vue,
                "Confirmez-vous la réservation pour le parking :\n" +
                parking.getLibelleParking() + "\n" +
                "Véhicule: " + typeVehicule,
                "Confirmation de réservation",
                JOptionPane.YES_NO_OPTION);
                
            if (choix == JOptionPane.YES_OPTION) {
                etat = EtatParking.RESERVATION;
                reserverPlace();
            } else {
                etat = EtatParking.SELECTION;
            }
        }
    }
    
    private void reserverPlace() {
        int index = vue.comboParking.getSelectedIndex();
        if (index >= 0 && index < vue.listeParkings.size()) {
            modele.Parking parking = vue.listeParkings.get(index);
            String typeVehicule = vue.getTypeVehicule();
            
            boolean succes = controleur.preparerStationnementParking(
                typeVehicule,
                vue.lblPlaque.getText(),
                parking.getIdParking(),
                vue
            );
            
            if (succes) {
                etat = EtatParking.REDIRECTION;
            } else {
                etat = EtatParking.SELECTION;
            }
        }
    }
    
    private void modifierPlaque() {
        // Demander si l'utilisateur veut modifier ou choisir parmi ses véhicules
        String[] options = {"Choisir un véhicule existant", "Saisir une nouvelle plaque"};
        int choix = JOptionPane.showOptionDialog(vue,
            "Que souhaitez-vous faire ?",
            "Modifier la plaque",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]);
        
        if (choix == 0) {
            // Choisir parmi les véhicules existants
            choisirVehiculeExistant();
        } else if (choix == 1) {
            // Saisir une nouvelle plaque
            saisirNouvellePlaque();
        }
    }
    
    private void choisirVehiculeExistant() {
        if (usager != null) {
            java.util.List<VehiculeUsager> vehicules = VehiculeUsagerDAO.getVehiculesByUsager(usager.getIdUsager());
            
            if (vehicules.isEmpty()) {
                JOptionPane.showMessageDialog(vue,
                    "Vous n'avez aucun véhicule enregistré.\nVeuillez d'abord ajouter un véhicule.",
                    "Aucun véhicule",
                    JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            // Créer un tableau pour la sélection
            String[] options = new String[vehicules.size()];
            for (int i = 0; i < vehicules.size(); i++) {
                VehiculeUsager v = vehicules.get(i);
                String etoile = v.isEstPrincipal() ? " ★" : "";
                options[i] = v.getPlaqueImmatriculation() + " - " + v.getTypeVehicule() + etoile;
            }
            
            String selection = (String) JOptionPane.showInputDialog(vue,
                "Choisissez un véhicule :",
                "Sélection du véhicule",
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]);
            
            if (selection != null) {
                // Trouver le véhicule correspondant
                for (VehiculeUsager v : vehicules) {
                    if (selection.startsWith(v.getPlaqueImmatriculation())) {
                        vue.lblPlaque.setText(v.getPlaqueImmatriculation());
                        
                        // Mettre à jour le type de véhicule dans la vue
                        String typeVehicule = v.getTypeVehicule();
                        if ("Voiture".equals(typeVehicule)) {
                            vue.radioVoiture.setSelected(true);
                        } else if ("Moto".equals(typeVehicule)) {
                            vue.radioMoto.setSelected(true);
                        } else if ("Camion".equals(typeVehicule)) {
                            vue.radioCamion.setSelected(true);
                        }
                        break;
                    }
                }
            }
        }
    }
    private void saisirNouvellePlaque() {
        String nouvellePlaque = JOptionPane.showInputDialog(vue, 
            "Entrez la plaque d'immatriculation (format: AA-123-AA):", 
            vue.lblPlaque.getText());
        
        if (nouvellePlaque != null && !nouvellePlaque.trim().isEmpty()) {
            String plaqueNettoyee = nouvellePlaque.trim().toUpperCase();
            
            if (controleur.validerPlaque(plaqueNettoyee)) {
                vue.lblPlaque.setText(plaqueNettoyee);
                
                // Demander si l'utilisateur veut sauvegarder ce véhicule
                if (usager != null) {
                    int sauvegarder = JOptionPane.showConfirmDialog(vue,
                        "Voulez-vous enregistrer ce véhicule pour une utilisation future ?",
                        "Enregistrer le véhicule",
                        JOptionPane.YES_NO_OPTION);
                    
                    if (sauvegarder == JOptionPane.YES_OPTION) {
                        // Demander le type de véhicule
                        String typeVehicule = vue.getTypeVehicule();
                        
                        // Créer et sauvegarder le véhicule
                        VehiculeUsager vehicule = new VehiculeUsager(
                            usager.getIdUsager(),
                            plaqueNettoyee,
                            typeVehicule
                        );
                        
                        // Demander si c'est le véhicule principal
                        int estPrincipal = JOptionPane.showConfirmDialog(vue,
                            "Définir ce véhicule comme véhicule principal ?",
                            "Véhicule principal",
                            JOptionPane.YES_NO_OPTION);
                        
                        vehicule.setEstPrincipal(estPrincipal == JOptionPane.YES_OPTION);
                        
                        if (VehiculeUsagerDAO.ajouterVehicule(vehicule)) {
                            JOptionPane.showMessageDialog(vue,
                                "Véhicule enregistré avec succès !",
                                "Succès",
                                JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                }
            } else {
                JOptionPane.showMessageDialog(vue,
                    "Format de plaque invalide. Utilisez AA-123-AA",
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    private void mettreAJourInfosParking(int index) {
        // Utiliser le getter public pour accéder à lblPlacesDispo
        if (index >= 0 && index < vue.listeParkings.size()) {
            modele.Parking parking = vue.listeParkings.get(index);
            vue.getLblPlacesDispo().setText(parking.getPlacesDisponibles() + " / " + parking.getNombrePlaces());
            
            if (parking.getPlacesDisponibles() <= 5) {
                vue.getLblPlacesDispo().setForeground(java.awt.Color.RED);
            } else if (parking.getPlacesDisponibles() <= 10) {
                vue.getLblPlacesDispo().setForeground(java.awt.Color.ORANGE);
            } else {
                vue.getLblPlacesDispo().setForeground(java.awt.Color.BLACK);
            }
        }
    }
    
    private void annuler() {
        etat = EtatParking.REDIRECTION;
        ihm.Page_Principale pagePrincipale = new ihm.Page_Principale(vue.emailUtilisateur);
        pagePrincipale.setVisible(true);
        vue.dispose();
    }
    
    private String getActionBouton(JButton b) {
        String texte = b.getText();
        if (texte != null) {
            if (texte.contains("Annuler")) {
                return "ANNULER";
            } else if (texte.contains("Réserver") || texte.contains("Stationner")) {
                return "RESERVER";
            } else if (texte.contains("Modifier")) {
                return "MODIFIER_PLAQUE";
            }
        }
        return "INCONNU";
    }
}
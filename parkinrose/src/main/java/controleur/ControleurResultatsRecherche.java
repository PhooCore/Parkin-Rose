package controleur;

import ihm.Page_Garer_Parking;
import ihm.Page_Resultats_Recherche;
import modele.Parking;
import modele.dao.TarifParkingDAO;
import modele.dao.UsagerDAO;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ControleurResultatsRecherche implements ActionListener {
    
    private Page_Resultats_Recherche vue;
    
    public ControleurResultatsRecherche(Page_Resultats_Recherche vue) {
        this.vue = vue;
        configurerListeners();
    }
    
    public void configurerListeners() {
        // Configurer les listeners pour les filtres
        if (vue.comboFiltres != null) {
            vue.comboFiltres.addActionListener(this);
        }
        
        if (vue.checkGratuit != null) {
            vue.checkGratuit.addActionListener(this);
        }
        if (vue.checkSoiree != null) {
            vue.checkSoiree.addActionListener(this);
        }
        if (vue.checkRelais != null) {
            vue.checkRelais.addActionListener(this);
        }
        if (vue.checkMoto != null) {
            vue.checkMoto.addActionListener(this);
        }
        
        // Configurer les boutons de manière récursive
        configurerListenersRecursifs(vue.getContentPane());
    }
    
    // Nouvelle méthode pour reconfigurer les listeners après filtrage
    public void configurerListenersApresFiltrage() {
        configurerListenersRecursifs(vue.getContentPane());
    }
    
    private void configurerListenersRecursifs(java.awt.Container container) {
        for (java.awt.Component comp : container.getComponents()) {
            if (comp instanceof JButton) {
                JButton button = (JButton) comp;
                // Ajouter le listener seulement si le bouton n'en a pas encore
                boolean alreadyHasListener = false;
                for (ActionListener listener : button.getActionListeners()) {
                    if (listener == this) {
                        alreadyHasListener = true;
                        break;
                    }
                }
                if (!alreadyHasListener) {
                    button.addActionListener(this);
                }
            } else if (comp instanceof JPanel) {
                configurerListenersRecursifs((JPanel) comp);
            } else if (comp instanceof JScrollPane) {
                JScrollPane scrollPane = (JScrollPane) comp;
                configurerListenersRecursifs(scrollPane.getViewport());
            }
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        
        // Gérer les filtres
        if (source == vue.comboFiltres || 
            source == vue.checkGratuit || 
            source == vue.checkSoiree || 
            source == vue.checkRelais || 
            source == vue.checkMoto) {
            vue.appliquerFiltres();
            return;
        }
        
        // Gérer les boutons avec action command
        if (source instanceof JButton) {
            JButton button = (JButton) source;
            String action = button.getActionCommand();
            
            if (action != null) {
                if (action.equals("RETOUR")) {
                    retourAccueil();
                } else if (action.equals("TOUS_PARKINGS")) {
                    afficherTousParkings();
                } else if (action.startsWith("STATIONNER_")) {
                    int index = Integer.parseInt(action.replace("STATIONNER_", ""));
                    selectionnerParking(index);
                }
            }
        }
    }
    
    @SuppressWarnings("null")
	private void selectionnerParking(int index) {
        if (index >= 0 && index < vue.parkingsFiltres.size()) {
            Parking parking = vue.parkingsFiltres.get(index);
            boolean estRelais = TarifParkingDAO.estParkingRelais(parking.getIdParking());

            // Pour les parkings relais, vérifier OBLIGATOIREMENT la carte Tisséo
            if (estRelais) {
                String carteTisseo = UsagerDAO.getCarteTisseoByUsager(
                    UsagerDAO.getUsagerByEmail(vue.emailUtilisateur).getIdUsager());
                
                if (carteTisseo == null) {
                    // L'utilisateur n'a pas de carte Tisséo -> INTERDIT de stationner
                    Object[] options = {"Ajouter une carte Tisséo", "Annuler"};
                    int choix = JOptionPane.showOptionDialog(
                        vue,
                        "🚫  ACCÈS IMPOSSIBLE\n\n" +
                        parking.getLibelleParking() + "\n" +
                        "(" + parking.getAdresseParking() + ")\n\n" +
                        "❌  Ce parking relais est exclusivement réservé\n" +
                        "aux détenteurs d'une carte Tisséo (Pastel).\n\n" +
                        "Vous ne pouvez pas stationner dans ce parking\n" +
                        "sans présenter votre carte Tisséo.\n\n" +
                        "Veuillez ajouter votre carte Tisséo à votre compte\n" +
                        "pour accéder à ce parking.",
                        "Accès refusé - Parking réservé",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.ERROR_MESSAGE,
                        null,
                        options,
                        options[0]
                    );
                    
                    if (choix == JOptionPane.YES_OPTION) {
                        // Ouvrir la page utilisateur pour ajouter une carte
                        ihm.Page_Utilisateur pageUtilisateur = new ihm.Page_Utilisateur(vue.emailUtilisateur);
                        pageUtilisateur.setVisible(true);
                    }
                    // Dans tous les cas, on ne peut pas continuer vers le stationnement
                    return;
                }
            }
            
            // Si on arrive ici, soit ce n'est pas un parking relais,
            // soit c'est un parking relais ET l'utilisateur a une carte Tisséo
            
            StringBuilder message = new StringBuilder();
            message.append("Voulez-vous préparer un stationnement pour :\n")
                   .append(parking.getLibelleParking()).append("\n")
                   .append(parking.getAdresseParking()).append("\n\n")
                   .append("Places voiture: ")
                   .append(parking.getPlacesDisponibles()).append("/")
                   .append(parking.getNombrePlaces()).append("\n");

            if (parking.hasMoto()) {
                message.append("Places moto: ")
                       .append(parking.getPlacesMotoDisponibles()).append("/")
                       .append(parking.getPlacesMoto()).append("\n");
            }

            message.append("Hauteur maximale: ")
                   .append(parking.getHauteurParking()).append("m\n");

            // Afficher le statut Tisséo pour les parkings relais
            if (estRelais) {
                String carteTisseo = UsagerDAO.getCarteTisseoByUsager(
                    UsagerDAO.getUsagerByEmail(vue.emailUtilisateur).getIdUsager());
                
                if (carteTisseo != null) {
                    message.append("\nCarte Tisséo détectée : ")
                           .append(carteTisseo.substring(0, 4)).append("******")
                           .append("\nStationnement gratuit");
                }
            }

            int choix = JOptionPane.showConfirmDialog(
                vue,
                message.toString(),
                "Confirmation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );

            if (choix == JOptionPane.YES_OPTION) {
            	
            	if (estRelais) {
            		  String carteTisseo = UsagerDAO.getCarteTisseoByUsager(UsagerDAO.getUsagerByEmail(vue.emailUtilisateur).getIdUsager());
            		  if (carteTisseo == null) {
            			  JOptionPane.showMessageDialog(vue, "Vous n'avez aucune carte Tisseo renseignée.", "Carte Tisseo requise", JOptionPane.WARNING_MESSAGE);
            			  return;
            		  }
            	
            	}
                Page_Garer_Parking pageParking = new Page_Garer_Parking(vue.emailUtilisateur, parking);
                pageParking.setVisible(true);
                vue.dispose();
            }
        }
    }
    
    private void retourAccueil() {
        ihm.Page_Principale pagePrincipale = new ihm.Page_Principale(vue.emailUtilisateur);
        pagePrincipale.setVisible(true);
        vue.dispose();
    }
    
    private void afficherTousParkings() {
        java.util.List<modele.Parking> tousParkings = modele.dao.ParkingDAO.getAllParkings();
        ihm.Page_Tous_Parkings pageTousParkings = new ihm.Page_Tous_Parkings(vue.emailUtilisateur, tousParkings);
        pageTousParkings.setVisible(true);
        vue.dispose();
    }
}
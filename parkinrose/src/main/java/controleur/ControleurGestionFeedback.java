package controleur;

import ihm.Page_Gestion_Feedback;
import ihm.Page_Administration;
import modele.Feedback;
import modele.Usager;
import modele.dao.FeedbackDAO;
import modele.dao.UsagerDAO;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ControleurGestionFeedback implements ActionListener, ListSelectionListener {
    
    // États du contrôleur
    private enum Etat {
        INITIAL,
        CHARGEMENT_ADMIN,
        CHARGEMENT_FEEDBACKS,
        MESSAGE_SELECTIONNE,
        SAISIE_REPONSE,
        MODIFICATION_STATUT,
        ENVOI_REPONSE,
        ACTUALISATION,
        RETOUR_ADMINISTRATION, // AJOUTÉ
        ERREUR
    }
    
    // Références
    private Page_Gestion_Feedback vue;
    private Etat etat;
    
    // Données
    private String emailAdmin;
    private Usager admin;
    private List<Feedback> feedbacksList;
    private Feedback feedbackSelectionne;
    
    // Constantes
    private static final int LONGUEUR_MIN_REPONSE = 10;
    private static final int LONGUEUR_MAX_REPONSE = 2000;
    
    public ControleurGestionFeedback(Page_Gestion_Feedback vue) {
        this.vue = vue;
        this.emailAdmin = vue.getEmailAdmin();
        this.etat = Etat.INITIAL;
        
        initialiserControleur();
    }
    
    private void initialiserControleur() {
        try {
            chargerInformationsAdmin();
            configurerListeners();
            chargerFeedbacks();
            etat = Etat.CHARGEMENT_FEEDBACKS;
        } catch (Exception e) {
            gererErreurInitialisation("Erreur initialisation: " + e.getMessage());
        }
    }
    
    private void chargerInformationsAdmin() throws Exception {
        this.admin = UsagerDAO.getUsagerByEmail(emailAdmin);
        
        if (this.admin == null) {
            throw new Exception("Administrateur non trouvé");
        }
        
        // Vérifier les droits d'admin
        if (!admin.isAdmin()) {
            throw new Exception("Accès administrateur requis");
        }
    }
    
    private void configurerListeners() {
        // Boutons
        vue.getBtnRetour().addActionListener(this);
        vue.getBtnMarquerEnCours().addActionListener(this);
        vue.getBtnMarquerResolu().addActionListener(this);
        vue.getBtnRepondre().addActionListener(this);
        
        // Table
        vue.getTableFeedbacks().getSelectionModel().addListSelectionListener(this);
        
        // ComboBox filtre
        vue.getComboFiltre().addActionListener(this);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        
        switch (etat) {
            case CHARGEMENT_FEEDBACKS:
            case MESSAGE_SELECTIONNE:
            case SAISIE_REPONSE:
                if (source == vue.getBtnRetour()) {
                    retourAdministration();
                } else if (source == vue.getBtnMarquerEnCours()) {
                    changerStatut("EN_COURS");
                } else if (source == vue.getBtnMarquerResolu()) {
                    changerStatut("RESOLU");
                } else if (source == vue.getBtnRepondre()) {
                    envoyerReponse();
                } else if (source == vue.getComboFiltre()) {
                    filtrerFeedbacks();
                }
                break;
                
            case MODIFICATION_STATUT:
            case ENVOI_REPONSE:
            case ACTUALISATION:
                // En cours de traitement
                break;
                
            case ERREUR:
                if (source == vue.getBtnRetour()) {
                    retourAdministration();
                }
                break;
        }
    }
    
    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting() && etat != Etat.MODIFICATION_STATUT && 
            etat != Etat.ENVOI_REPONSE && etat != Etat.ACTUALISATION) {
            afficherDetailsSelection();
        }
    }
    
    private void chargerFeedbacks() {
        try {
            feedbacksList = FeedbackDAO.getAllFeedbacksWithInfo();
            vue.getTableModel().setRowCount(0);
            feedbackSelectionne = null;
            
            if (feedbacksList == null || feedbacksList.isEmpty()) {
                JOptionPane.showMessageDialog(vue,
                    "Aucun message à afficher.",
                    "Information",
                    JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            // Remplir la table
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm");
            
            for (Feedback feedback : feedbacksList) {
                String statut = getStatutAvecIcone(feedback.getStatut());
                String utilisateur = feedback.getPrenomUsager() + " " + feedback.getNomUsager();
                String date = feedback.getDateCreation().format(formatter);
                String repondu = feedback.isRepondu() ? "O" : "X";
                
                vue.getTableModel().addRow(new Object[]{
                    feedback.getIdFeedback(),
                    statut,
                    feedback.getSujet(),
                    utilisateur,
                    date,
                    repondu
                });
            }
            
            // Mettre à jour le titre avec le nombre de nouveaux messages
            mettreAJourTitre();
            
        } catch (Exception ex) {
            gererErreur("Erreur chargement feedbacks: " + ex.getMessage());
        }
    }
    
    private String getStatutAvecIcone(String statut) {
        if (statut == null) return "INCONNU";
        
        switch (statut) {
            case "NOUVEAU": return "NOUVEAU";
            case "EN_COURS": return "EN COURS";
            case "RESOLU": return "RÉSOLU";
            default: return statut;
        }
    }
    
    private void mettreAJourTitre() {
        int nouveaux = FeedbackDAO.getNombreNouveauxFeedbacks();
        if (nouveaux > 0) {
            vue.setTitle("Gestion des Feedbacks - " + nouveaux + " nouveau" + (nouveaux > 1 ? "x" : ""));
        }
    }
    
    private void filtrerFeedbacks() {
        String filtre = (String) vue.getComboFiltre().getSelectedItem();
        
        if (feedbacksList == null) return;
        
        vue.getTableModel().setRowCount(0);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm");
        
        for (Feedback feedback : feedbacksList) {
            boolean afficher = true;
            
            if (filtre != null) {
                switch (filtre) {
                    case "Nouveaux":
                        afficher = "NOUVEAU".equals(feedback.getStatut());
                        break;
                    case "En cours":
                        afficher = "EN_COURS".equals(feedback.getStatut());
                        break;
                    case "Résolus":
                        afficher = "RESOLU".equals(feedback.getStatut());
                        break;
                }
            }
            
            if (afficher || "Tous les messages".equals(filtre) || filtre == null) {
                String statut = getStatutAvecIcone(feedback.getStatut());
                String utilisateur = feedback.getPrenomUsager() != null && feedback.getNomUsager() != null ?
                    feedback.getPrenomUsager() + " " + feedback.getNomUsager() :
                    "Utilisateur #" + feedback.getIdUsager();
                String date = feedback.getDateCreation() != null ?
                    feedback.getDateCreation().format(formatter) : "Date inconnue";
                String repondu = feedback.isRepondu() ? "O" : "X";
                
                vue.getTableModel().addRow(new Object[]{
                    feedback.getIdFeedback(),
                    statut,
                    feedback.getSujet(),
                    utilisateur,
                    date,
                    repondu
                });
            }
        }
    }
    
    private void afficherDetailsSelection() {
        int selectedRow = vue.getTableFeedbacks().getSelectedRow();
        if (selectedRow == -1) {
            desactiverBoutons();
            effacerDetails();
            return;
        }
        
        try {
            // Récupérer l'ID du feedback sélectionné
            Object idObj = vue.getTableModel().getValueAt(selectedRow, 0);
            if (!(idObj instanceof Integer)) {
                afficherMessageErreur("Format d'ID invalide", "Erreur");
                return;
            }
            
            int idFeedback = (Integer) idObj;
            feedbackSelectionne = FeedbackDAO.getFeedbackById(idFeedback);
            
            if (feedbackSelectionne == null) {
                afficherMessageErreur("Impossible de charger les détails du message.", "Erreur");
                return;
            }
            
            etat = Etat.MESSAGE_SELECTIONNE;
            
            // Activer les boutons
            activerBoutons();
            
            // Mettre à jour les informations
            afficherInformationsFeedback();
            
            // Charger l'historique
            chargerHistorique();
            
        } catch (Exception ex) {
            gererErreur("Erreur affichage détails: " + ex.getMessage());
        }
    }
    
    private void activerBoutons() {
        vue.getBtnMarquerEnCours().setEnabled(true);
        vue.getBtnMarquerResolu().setEnabled(true);
        vue.getBtnRepondre().setEnabled(true);
    }
    
    private void desactiverBoutons() {
        vue.getBtnMarquerEnCours().setEnabled(false);
        vue.getBtnMarquerResolu().setEnabled(false);
        vue.getBtnRepondre().setEnabled(false);
    }
    
    private void effacerDetails() {
        vue.getLblUserInfo().setText("Utilisateur : ");
        vue.getLblDateInfo().setText("Date : ");
        vue.getLblStatut().setText("Statut : ");
        vue.getLblSujetInfo().setText("Sujet : ");
        vue.getTxtMessageDetail().setText("");
        vue.getTxtHistorique().setText("");
        vue.getTxtReponse().setText("");
    }
    
    private void afficherInformationsFeedback() {
        if (feedbackSelectionne == null) return;
        
        try {
            // Récupérer l'utilisateur
            Usager usager = UsagerDAO.getUsagerByEmail(feedbackSelectionne.getMailUsager());
            String nomUtilisateur;
            
            if (usager != null && usager.getPrenomUsager() != null && usager.getNomUsager() != null) {
                nomUtilisateur = usager.getPrenomUsager() + " " + usager.getNomUsager() + 
                               " (" + (feedbackSelectionne.getMailUsager() != null ? feedbackSelectionne.getMailUsager() : "sans email") + ")";
            } else {
                nomUtilisateur = "Utilisateur #" + feedbackSelectionne.getIdUsager();
            }
            
            // Formater la date
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            String dateStr = feedbackSelectionne.getDateCreation() != null ?
                feedbackSelectionne.getDateCreation().format(formatter) : "Date inconnue";
            
            // Mettre à jour les labels
            vue.getLblUserInfo().setText("Utilisateur : " + nomUtilisateur);
            vue.getLblDateInfo().setText("Date : " + dateStr);
            vue.getLblStatut().setText("Statut : " + getStatutAvecIcone(feedbackSelectionne.getStatut()));
            vue.getLblSujetInfo().setText("Sujet : " + feedbackSelectionne.getSujet());
            
            // Afficher le message
            vue.getTxtMessageDetail().setText(feedbackSelectionne.getMessage() != null ? 
                feedbackSelectionne.getMessage() : "Message non disponible");
            
        } catch (Exception ex) {
            vue.getLblUserInfo().setText("Utilisateur : Erreur chargement");
            vue.getTxtMessageDetail().setText("Erreur lors du chargement des informations.");
        }
    }
    
    private void chargerHistorique() {
        if (feedbackSelectionne == null) return;
        
        List<Feedback> reponses = FeedbackDAO.getReponsesFeedback(feedbackSelectionne.getIdFeedback());
        
        if (reponses == null || reponses.isEmpty()) {
            vue.getTxtHistorique().setText("Aucune réponse pour le moment.");
            return;
        }
        
        StringBuilder historique = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        
        for (Feedback reponse : reponses) {
            String date = reponse.getDateCreation() != null ?
                reponse.getDateCreation().format(formatter) : "Date inconnue";
            
            String nomAdmin = "Administrateur";
            
            if (reponse.getIdAdminReponse() != null) {
                try {
                    Usager admin = UsagerDAO.getUsagerByEmail(String.valueOf(reponse.getIdAdminReponse()));
                    if (admin != null && admin.getPrenomUsager() != null && admin.getNomUsager() != null) {
                        nomAdmin = admin.getPrenomUsager() + " " + admin.getNomUsager();
                    }
                } catch (Exception ex) {
                    // Continuer avec le nom par défaut
                }
            }
            
            historique.append("[").append(date).append("] ").append(nomAdmin).append(" :\n")
                     .append(reponse.getMessage() != null ? reponse.getMessage() : "Message non disponible")
                     .append("\n\n");
        }
        
        vue.getTxtHistorique().setText(historique.toString());
        vue.getTxtHistorique().setCaretPosition(0); // Remonter en haut
    }
    
    private void changerStatut(String nouveauStatut) {
        if (feedbackSelectionne == null) {
            afficherMessageErreur("Veuillez sélectionner un message.", "Aucune sélection");
            return;
        }
        
        etat = Etat.MODIFICATION_STATUT;
        
        // Confirmation
        String messageConfirmation = "Êtes-vous sûr de vouloir marquer ce message comme ";
        if ("EN_COURS".equals(nouveauStatut)) {
            messageConfirmation += "en cours ?";
        } else {
            messageConfirmation += "résolu ?";
        }
        
        int confirmation = JOptionPane.showConfirmDialog(
            vue,
            messageConfirmation,
            "Confirmation",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (confirmation != JOptionPane.YES_OPTION) {
            etat = Etat.MESSAGE_SELECTIONNE;
            return;
        }
        
        // Mettre à jour le statut
        boolean gotanswer = "RESOLU".equals(nouveauStatut);
        boolean success = FeedbackDAO.mettreAJourStatut(feedbackSelectionne.getIdFeedback(), 
                                                       nouveauStatut, gotanswer);
        
        if (success) {
            // Mettre à jour la table
            int selectedRow = vue.getTableFeedbacks().getSelectedRow();
            if (selectedRow != -1) {
                vue.getTableModel().setValueAt(getStatutAvecIcone(nouveauStatut), selectedRow, 1);
                vue.getTableModel().setValueAt(gotanswer ? "O" : "X", selectedRow, 5);
            }
            
            // Mettre à jour le label
            vue.getLblStatut().setText("Statut : " + getStatutAvecIcone(nouveauStatut));
            
            afficherMessageSucces("Statut mis à jour avec succès.");
            
            // Mettre à jour le feedback sélectionné
            feedbackSelectionne.setStatut(nouveauStatut);
            feedbackSelectionne.setGotanswer(gotanswer);
            
        } else {
            afficherMessageErreur("Erreur lors de la mise à jour du statut.", "Erreur");
        }
        
        etat = Etat.MESSAGE_SELECTIONNE;
    }
    
    private void envoyerReponse() {
        if (feedbackSelectionne == null) {
            afficherMessageErreur("Veuillez sélectionner un message.", "Aucune sélection");
            return;
        }
        
        String reponse = vue.getTxtReponse().getText().trim();
        if (reponse.isEmpty()) {
            afficherMessageErreur("Veuillez saisir une réponse.", "Réponse vide");
            return;
        }
        
        if (reponse.length() < LONGUEUR_MIN_REPONSE) {
            afficherMessageErreur(
                String.format("La réponse doit contenir au moins %d caractères.", LONGUEUR_MIN_REPONSE),
                "Réponse trop courte"
            );
            return;
        }
        
        if (reponse.length() > LONGUEUR_MAX_REPONSE) {
            afficherMessageErreur(
                String.format("La réponse ne peut pas dépasser %d caractères.", LONGUEUR_MAX_REPONSE),
                "Réponse trop longue"
            );
            return;
        }
        
        etat = Etat.SAISIE_REPONSE;
        
        // Confirmation
        int confirmation = JOptionPane.showConfirmDialog(
            vue,
            "Envoyer cette réponse à l'utilisateur ?",
            "Confirmation d'envoi",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (confirmation != JOptionPane.YES_OPTION) {
            etat = Etat.MESSAGE_SELECTIONNE;
            return;
        }
        
        etat = Etat.ENVOI_REPONSE;
        
        try {
            // Envoyer la réponse
            boolean success = FeedbackDAO.repondreFeedback(feedbackSelectionne.getIdFeedback(), 
                                                          admin.getIdUsager(), reponse);
            
            if (success) {
                // Mettre à jour la table
                int selectedRow = vue.getTableFeedbacks().getSelectedRow();
                if (selectedRow != -1) {
                    vue.getTableModel().setValueAt(getStatutAvecIcone("EN_COURS"), selectedRow, 1);
                    vue.getTableModel().setValueAt("O", selectedRow, 5);
                }
                
                // Vider la zone de réponse
                vue.getTxtReponse().setText("");
                
                // Recharger l'historique
                chargerHistorique();
                
                // Mettre à jour le feedback
                feedbackSelectionne.setStatut("EN_COURS");
                feedbackSelectionne.setGotanswer(true);
                
                afficherMessageSucces("Réponse envoyée avec succès.");
            } else {
                afficherMessageErreur("Erreur lors de l'envoi de la réponse.", "Erreur");
            }
            
        } catch (Exception ex) {
            gererErreur("Erreur envoi réponse: " + ex.getMessage());
        }
        
        etat = Etat.MESSAGE_SELECTIONNE;
    }
    
    private void actualiserFeedbacks() {
        etat = Etat.ACTUALISATION;
        chargerFeedbacks();
        effacerDetails();
        desactiverBoutons();
        afficherMessageSucces("Liste des messages actualisée.");
        etat = Etat.CHARGEMENT_FEEDBACKS;
    }
    
    // AJOUTÉ : Méthode pour retourner à la page d'administration
    private void retourAdministration() {
        etat = Etat.RETOUR_ADMINISTRATION;
        
        // Demander confirmation si des modifications sont en cours
        if (etat == Etat.SAISIE_REPONSE || etat == Etat.MODIFICATION_STATUT) {
            int confirmation = JOptionPane.showConfirmDialog(
                vue,
                "Une réponse est en cours de saisie.\n" +
                "Êtes-vous sûr de vouloir retourner à l'administration ?\n" +
                "Les modifications non enregistrées seront perdues.",
                "Confirmation de retour",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );
            
            if (confirmation != JOptionPane.YES_OPTION) {
                etat = Etat.MESSAGE_SELECTIONNE;
                return;
            }
        }
        
        // Fermer la fenêtre courante
        vue.dispose();
        
        // Réouvrir la page d'administration
        SwingUtilities.invokeLater(() -> {
            Page_Administration pageAdmin = new Page_Administration(emailAdmin);
            pageAdmin.setVisible(true);
        });
    }
    
    private void fermerPage() {
        vue.dispose();
    }
    
    private void afficherMessageErreur(String message, String titre) {
        JOptionPane.showMessageDialog(vue, message, titre, JOptionPane.ERROR_MESSAGE);
    }
    
    private void afficherMessageSucces(String message) {
        JOptionPane.showMessageDialog(vue, message, "Succès", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void gererErreur(String message) {
        System.err.println(message);
        afficherMessageErreur(message, "Erreur");
        etat = Etat.ERREUR;
    }
    
    private void gererErreurInitialisation(String message) {
        System.err.println("Erreur initialisation: " + message);
        afficherMessageErreur(message, "Erreur d'initialisation");
        vue.dispose();
    }
    
    // Getters pour débogage
    public Etat getEtat() {
        return etat;
    }
    
    public Usager getAdmin() {
        return admin;
    }
}
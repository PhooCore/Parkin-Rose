package controleur;

import ihm.Page_Feedback;
import ihm.Page_Principale;
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

public class ControleurFeedback implements ActionListener, ListSelectionListener {
    
    // États du contrôleur
    private enum Etat {
        INITIAL,
        CHARGEMENT_UTILISATEUR,
        CHARGEMENT_FEEDBACKS,
        CONVERSATION_SELECTIONNEE,
        SAISIE_NOUVEAU_MESSAGE,
        ENVOI_MESSAGE,
        ACTUALISATION,
        REDIRECTION,
        ERREUR
    }
    
    // Références
    private Page_Feedback vue;
    private Etat etat;
    
    // Données
    private String emailUtilisateur;
    private Usager usager;
    private List<Feedback> feedbacksList;
    private Feedback feedbackSelectionne;
    
    // Constantes
    private static final int LONGUEUR_MIN_SUJET = 3;
    private static final int LONGUEUR_MIN_MESSAGE = 10;
    private static final int LONGUEUR_MAX_SUJET = 100;
    private static final int LONGUEUR_MAX_MESSAGE = 1000;
    
    public ControleurFeedback(Page_Feedback vue) {
        this.vue = vue;
        this.emailUtilisateur = vue.getEmailUtilisateur();
        this.etat = Etat.INITIAL;
        
        initialiserControleur();
    }
    
    private void initialiserControleur() {
        try {
            chargerInformationsUtilisateur();
            configurerListeners();
            chargerFeedbacks();
            etat = Etat.CHARGEMENT_FEEDBACKS;
        } catch (Exception e) {
            gererErreurInitialisation("Erreur initialisation: " + e.getMessage());
        }
    }
    
    private void chargerInformationsUtilisateur() throws Exception {
        etat = Etat.CHARGEMENT_UTILISATEUR;
        
        this.usager = UsagerDAO.getUsagerByEmail(emailUtilisateur);
        
        if (this.usager == null) {
            this.usager = new Usager();
            this.usager.setMailUsager(emailUtilisateur);
            this.usager.setIdUsager(-1);
        }
        
        // Mettre à jour le label utilisateur dans la vue
        String nomUser = usager.getPrenomUsager() != null ? 
            usager.getPrenomUsager() + " " + usager.getNomUsager() : 
            emailUtilisateur;
        vue.getLblUser().setText(nomUser);
    }
    
    private void configurerListeners() {
        // Boutons
        vue.getBtnFermer().addActionListener(this);
        vue.getBtnEnvoyerMessage().addActionListener(this);
        vue.getBtnEffacer().addActionListener(this);
        
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
            case CONVERSATION_SELECTIONNEE:
            case SAISIE_NOUVEAU_MESSAGE:
                if (source == vue.getBtnFermer()) {
                    fermerPage();
                } else if (source == vue.getBtnEnvoyerMessage()) {
                    envoyerNouveauMessage();
                } else if (source == vue.getBtnEffacer()) {
                    effacerFormulaire();
                } else if (source == vue.getComboFiltre()) {
                    filtrerFeedbacks();
                }
                break;
                
            case ENVOI_MESSAGE:
            case ACTUALISATION:
                // En cours de traitement
                break;
                
            case ERREUR:
                if (source == vue.getBtnFermer()) {
                    fermerPage();
                }
                break;
        }
    }
    
    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting() && etat != Etat.ENVOI_MESSAGE && etat != Etat.ACTUALISATION) {
            afficherDetailsConversation();
        }
    }
    
    private void chargerFeedbacks() {
        try {
            feedbacksList = FeedbackDAO.getFeedbacksByUser(usager.getIdUsager());
            
            // Mettre à jour la vue
            vue.getTableModel().setRowCount(0);
            feedbackSelectionne = null;
            
            if (feedbacksList == null || feedbacksList.isEmpty()) {
                vue.getTableModel().addRow(new Object[]{"✉️", "Vous n'avez pas encore de conversation", ""});
                mettreAJourInfoConversation();
                return;
            }
            
            for (Feedback feedback : feedbacksList) {
                String statutIcon = getIconeStatut(feedback);
                String conversationInfo = getConversationInfo(feedback);
                String derniereActivite = getDerniereActivite(feedback);
                
                vue.getTableModel().addRow(new Object[]{statutIcon, conversationInfo, derniereActivite});
            }
            
            mettreAJourInfoConversation();
            
        } catch (Exception e) {
            gererErreur("Erreur chargement feedbacks: " + e.getMessage());
        }
    }
    
    private String getIconeStatut(Feedback feedback) {
        return feedback.isRepondu() ? "✓" : "●";
    }
    
    private String getConversationInfo(Feedback feedback) {
        String snippet = feedback.getMessage().length() > 70 ? 
            feedback.getMessage().substring(0, 70) + "..." : 
            feedback.getMessage();
        
        String statutTexte = feedback.isRepondu() ? 
            "<font color='green' size='-1'><b>RÉPONDU</b></font>" : 
            "<font color='orange' size='-1'><b>EN ATTENTE</b></font>";
        
        return "<html><b>" + feedback.getSujet() + "</b><br>" + 
               "<font color='#666666' size='-1'>" + statutTexte + " • " + snippet + "</font></html>";
    }
    
    private String getDerniereActivite(Feedback feedback) {
        List<Feedback> reponses = FeedbackDAO.getReponsesFeedback(feedback.getIdFeedback());
        if (reponses != null && !reponses.isEmpty()) {
            Feedback derniere = reponses.get(reponses.size() - 1);
            return "<html><font size='-1'>" + 
                   derniere.getDateCreation().format(DateTimeFormatter.ofPattern("dd/MM")) + 
                   "<br><font color='gray'>" + 
                   derniere.getDateCreation().format(DateTimeFormatter.ofPattern("HH:mm")) + 
                   "</font></font></html>";
        }
        return "<html><font size='-1'>" + 
               feedback.getDateCreation().format(DateTimeFormatter.ofPattern("dd/MM")) + 
               "<br><font color='gray'>" + 
               feedback.getDateCreation().format(DateTimeFormatter.ofPattern("HH:mm")) + 
               "</font></font></html>";
    }
    
    private void mettreAJourInfoConversation() {
        int total = vue.getTableModel().getRowCount();
        String info = total + " conversation" + (total > 1 ? "s" : "");
        vue.getLblInfoConversation().setText(info);
    }
    
    private void filtrerFeedbacks() {
        String filtre = (String) vue.getComboFiltre().getSelectedItem();
        vue.getTableModel().setRowCount(0);
        
        if (feedbacksList == null) return;
        
        for (Feedback feedback : feedbacksList) {
            boolean afficher = true;
            
            switch (filtre) {
                case "En attente":
                    afficher = !feedback.isRepondu();
                    break;
                case "Répondu":
                    afficher = feedback.isRepondu();
                    break;
            }
            
            if (afficher || filtre.equals("Toutes mes conversations")) {
                String statutIcon = getIconeStatut(feedback);
                String conversationInfo = getConversationInfo(feedback);
                String derniereActivite = getDerniereActivite(feedback);
                
                vue.getTableModel().addRow(new Object[]{statutIcon, conversationInfo, derniereActivite});
            }
        }
        
        mettreAJourInfoConversation();
    }
    
    private void afficherDetailsConversation() {
        int selectedRow = vue.getTableFeedbacks().getSelectedRow();
        if (selectedRow == -1) {
            feedbackSelectionne = null;
            return;
        }
        
        // Trouver le feedback correspondant dans la liste filtrée
        String filtre = (String) vue.getComboFiltre().getSelectedItem();
        int indexFiltre = 0;
        
        for (int i = 0; i < feedbacksList.size(); i++) {
            Feedback feedback = feedbacksList.get(i);
            boolean afficher = true;
            
            if (!filtre.equals("Toutes mes conversations")) {
                switch (filtre) {
                    case "En attente":
                        afficher = !feedback.isRepondu();
                        break;
                    case "Répondu":
                        afficher = feedback.isRepondu();
                        break;
                }
            }
            
            if (afficher) {
                if (indexFiltre == selectedRow) {
                    feedbackSelectionne = feedback;
                    break;
                }
                indexFiltre++;
            }
        }
        
        if (feedbackSelectionne == null) {
            return;
        }
        
        etat = Etat.CONVERSATION_SELECTIONNEE;
        chargerDetailsConversation();
    }
    
    private void chargerDetailsConversation() {
        if (feedbackSelectionne == null) {
            return;
        }
        
        // Afficher le message original
        String dateCreation = feedbackSelectionne.getDateCreation().format(
            DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm"));
        
        String statutTexte = feedbackSelectionne.isRepondu() ? 
            "Répondu" : "En attente de réponse";
        
        vue.getTxtMessageDetail().setText("Sujet : " + feedbackSelectionne.getSujet() + "\n" +
                                         "Date : " + dateCreation + "\n" +
                                         "Statut : " + statutTexte + "\n\n" +
                                         "Votre message :\n" + feedbackSelectionne.getMessage());
        
        // Charger l'historique
        chargerHistoriqueConversation();
    }
    
    private void chargerHistoriqueConversation() {
        List<Feedback> reponses = FeedbackDAO.getReponsesFeedback(feedbackSelectionne.getIdFeedback());
        
        if (reponses == null || reponses.isEmpty()) {
            vue.getTxtHistorique().setText("Aucune réponse pour le moment.\n\n" +
                                          "L'équipe ParkinRose vous répondra dans les plus brefs délais.");
            return;
        }
        
        StringBuilder historique = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm");
        
        historique.append("Réponses de l'équipe ParkinRose :\n\n");
        
        for (Feedback reponse : reponses) {
            String date = reponse.getDateCreation().format(formatter);
            
            historique.append("------------------------------------\n")
                     .append(date).append("\n\n")
                     .append(reponse.getMessage()).append("\n\n");
        }
        
        vue.getTxtHistorique().setText(historique.toString());
        vue.getTxtHistorique().setCaretPosition(0);
    }
    
    private void envoyerNouveauMessage() {
        etat = Etat.SAISIE_NOUVEAU_MESSAGE;
        
        if (!validerFormulaire()) {
            return;
        }
        
        etat = Etat.ENVOI_MESSAGE;
        traiterEnvoiMessage();
    }
    
    private boolean validerFormulaire() {
        String sujet = vue.getTxtSujetNouveau().getText().trim();
        String message = vue.getTxtNouveauMessage().getText().trim();
        
        // Validation du sujet
        if (sujet.isEmpty()) {
            afficherMessageErreur("Veuillez saisir un sujet.", "Sujet vide");
            return false;
        }
        
        if (sujet.length() < LONGUEUR_MIN_SUJET) {
            afficherMessageErreur(
                String.format("Le sujet doit contenir au moins %d caractères.", LONGUEUR_MIN_SUJET),
                "Sujet trop court"
            );
            return false;
        }
        
        if (sujet.length() > LONGUEUR_MAX_SUJET) {
            afficherMessageErreur(
                String.format("Le sujet ne peut pas dépasser %d caractères.", LONGUEUR_MAX_SUJET),
                "Sujet trop long"
            );
            return false;
        }
        
        // Validation du message
        if (message.isEmpty()) {
            afficherMessageErreur("Veuillez saisir un message.", "Message vide");
            return false;
        }
        
        if (message.length() < LONGUEUR_MIN_MESSAGE) {
            afficherMessageErreur(
                String.format("Le message doit contenir au moins %d caractères.", LONGUEUR_MIN_MESSAGE),
                "Message trop court"
            );
            return false;
        }
        
        if (message.length() > LONGUEUR_MAX_MESSAGE) {
            afficherMessageErreur(
                String.format("Le message ne peut pas dépasser %d caractères.", LONGUEUR_MAX_MESSAGE),
                "Message trop long"
            );
            return false;
        }
        
        return true;
    }
    
    private void traiterEnvoiMessage() {
        try {
            String sujet = vue.getTxtSujetNouveau().getText().trim();
            String message = vue.getTxtNouveauMessage().getText().trim();
            
            boolean success = FeedbackDAO.envoyerFeedback(usager.getIdUsager(), sujet, message);
            
            if (success) {
                afficherConfirmationEnvoi();
                effacerFormulaire();
                actualiserFeedbacks();
                etat = Etat.CHARGEMENT_FEEDBACKS;
            } else {
                gererErreur("Erreur lors de l'envoi du message.");
            }
            
        } catch (Exception e) {
            gererErreur("Erreur envoi message: " + e.getMessage());
        }
    }
    
    private void afficherConfirmationEnvoi() {
        String message = String.format(
            "<html><div style='text-align: center;'>"
            + "<h2 style='color: green;'>Message envoyé !</h2>"
            + "<p>Votre message a été envoyé avec succès à notre équipe.</p>"
            + "<br>"
            + "<div style='background-color: #f0f8ff; padding: 15px; border-radius: 5px; text-align: left;'>"
            + "<p><b>Sujet :</b> %s</p>"
            + "<p><b>Message :</b> %s</p>"
            + "</div>"
            + "<p style='color: #666;'>Vous recevrez une réponse dans les plus brefs délais.</p>"
            + "</div></html>",
            vue.getTxtSujetNouveau().getText().trim(),
            vue.getTxtNouveauMessage().getText().trim().substring(0, Math.min(100, vue.getTxtNouveauMessage().getText().trim().length())) + 
            (vue.getTxtNouveauMessage().getText().trim().length() > 100 ? "..." : "")
        );
        
        JOptionPane.showMessageDialog(
            vue,
            message,
            "Message envoyé",
            JOptionPane.INFORMATION_MESSAGE
        );
    }
    
    private void effacerFormulaire() {
        vue.getTxtSujetNouveau().setText("");
        vue.getTxtNouveauMessage().setText("");
    }
    
    private void actualiserFeedbacks() {
        etat = Etat.ACTUALISATION;
        chargerFeedbacks();
        vue.getTxtMessageDetail().setText("");
        vue.getTxtHistorique().setText("");
        etat = Etat.CHARGEMENT_FEEDBACKS;
    }
    
    private void fermerPage() {
        etat = Etat.REDIRECTION;
        
        int confirmation = JOptionPane.showConfirmDialog(
            vue,
            "Êtes-vous sûr de vouloir fermer la messagerie ?",
            "Confirmation de fermeture",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (confirmation == JOptionPane.YES_OPTION) {
            retourPagePrincipale();
        } else {
            etat = Etat.CHARGEMENT_FEEDBACKS;
        }
    }
    
    private void retourPagePrincipale() {
        try {
            // Fermer la page feedback seulement
            vue.dispose();
        } catch (Exception e) {
            gererErreur("Erreur fermeture: " + e.getMessage());
        }
    }
    
    private void afficherMessageErreur(String message, String titre) {
        JOptionPane.showMessageDialog(vue, message, titre, JOptionPane.ERROR_MESSAGE);
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
    
    public Usager getUsager() {
        return usager;
    }
}
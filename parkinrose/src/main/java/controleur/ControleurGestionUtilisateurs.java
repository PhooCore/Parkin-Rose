package controleur;

import ihm.PageGestionUtilisateurs;
import ihm.Page_Administration;
import modele.Usager;
import modele.VehiculeUsager;
import modele.Abonnement;
import modele.dao.UsagerDAO;
import modele.dao.VehiculeUsagerDAO;
import modele.dao.AbonnementDAO;
import modele.dao.StationnementDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ControleurGestionUtilisateurs implements ActionListener {
    
    private PageGestionUtilisateurs vue;
    
    public ControleurGestionUtilisateurs(PageGestionUtilisateurs vue) {
        this.vue = vue;
        configurerListeners();
    }
    
    private void configurerListeners() {
        vue.getBtnRechercher().addActionListener(this);
        vue.getBtnActualiser().addActionListener(this);
        vue.getBtnNouveau().addActionListener(this);
        vue.getBtnModifier().addActionListener(this);
        vue.getBtnSupprimer().addActionListener(this);
        vue.getBtnVoirVehicules().addActionListener(this);
        vue.getBtnVoirAbonnements().addActionListener(this);
        vue.getBtnVoirStationnements().addActionListener(this);
        vue.getBtnRetour().addActionListener(this);
        
        // Recherche à la frappe (Enter)
        vue.getTxtRecherche().addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    vue.rechercherUtilisateurs();
                }
            }
        });
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        
        if (source == vue.getBtnRechercher()) {
            vue.rechercherUtilisateurs();
        } else if (source == vue.getBtnActualiser()) {
            vue.chargerUtilisateurs();
            vue.afficherInformation("Liste actualisée");
        } else if (source == vue.getBtnNouveau()) {
            vue.afficherFormulaireNouvelUtilisateur();
        } else if (source == vue.getBtnModifier()) {
            vue.modifierUtilisateur();
        } else if (source == vue.getBtnSupprimer()) {
            toggleAdminStatus();
        } else if (source == vue.getBtnVoirVehicules()) {
            gererVehicules();
        } else if (source == vue.getBtnVoirAbonnements()) {
            gererAbonnements();
        } else if (source == vue.getBtnVoirStationnements()) {
            voirStationnements();
        } else if (source == vue.getBtnRetour()) {
            retourAdministration();
        }
    }
    
    private void toggleAdminStatus() {
        Usager usager = vue.getUsagerSelectionne();
        if (usager == null) {
            vue.afficherInformation("Veuillez sélectionner un utilisateur");
            return;
        }
        
        String action = usager.isAdmin() ? "retirer les droits d'administrateur" : "donner les droits d'administrateur";
        
        if (vue.demanderConfirmation("Voulez-vous " + action + " à " + 
                                   usager.getPrenomUsager() + " " + usager.getNomUsager() + " ?")) {
            
            try {
                boolean success = mettreAJourStatutAdmin(usager.getIdUsager(), !usager.isAdmin());
                if (success) {
                    vue.afficherInformation("Statut administrateur modifié avec succès");
                    vue.chargerUtilisateurs();
                } else {
                    vue.afficherErreur("Erreur lors de la modification du statut");
                }
            } catch (Exception e) {
                vue.afficherErreur("Erreur: " + e.getMessage());
            }
        }
    }
    
    private boolean mettreAJourStatutAdmin(int idUsager, boolean estAdmin) {
        String sql = "UPDATE Usager SET is_admin = ? WHERE id_usager = ?";
        
        try (java.sql.Connection conn = modele.dao.MySQLConnection.getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setBoolean(1, estAdmin);
            pstmt.setInt(2, idUsager);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private void gererVehicules() {
        Usager usager = vue.getUsagerSelectionne();
        if (usager == null) {
            vue.afficherInformation("Veuillez sélectionner un utilisateur");
            return;
        }
        
        // Afficher une boîte de dialogue pour gérer les véhicules
        JDialog dialog = new JDialog(vue, "Gestion des Véhicules - " + usager.getNomUsager(), true);
        dialog.setSize(700, 500);
        dialog.setLocationRelativeTo(vue);
        
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Tableau des véhicules
        String[] colonnes = {"ID", "Plaque", "Type", "Marque", "Modèle", "Principal", "Date d'ajout"};
        DefaultTableModel modelVehicules = new DefaultTableModel(colonnes, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Rendre toutes les cellules non éditables
            }
        };
        JTable tableVehicules = new JTable(modelVehicules);
        
        // Charger les véhicules existants
        chargerVehiculesUsager(usager.getIdUsager(), modelVehicules);
        
        JScrollPane scrollPane = new JScrollPane(tableVehicules);
        
        // Panel de boutons
        JPanel panelBoutons = new JPanel(new FlowLayout());
        JButton btnAjouter = new JButton("Ajouter un véhicule");
        JButton btnSupprimer = new JButton("Supprimer le véhicule");
        JButton btnDefinirPrincipal = new JButton("Définir comme principal");
        JButton btnFermer = new JButton("Fermer");
        
        btnAjouter.addActionListener(e -> ajouterVehiculeDialog(usager, dialog, modelVehicules));
        
        btnSupprimer.addActionListener(e -> {
            int selectedRow = tableVehicules.getSelectedRow();
            if (selectedRow >= 0) {
                int idVehiculeUsager = (int) modelVehicules.getValueAt(selectedRow, 0);
                String plaque = (String) modelVehicules.getValueAt(selectedRow, 1);
                boolean estPrincipal = "Oui".equals(modelVehicules.getValueAt(selectedRow, 5));
                
                if (vue.demanderConfirmation("Supprimer le véhicule " + plaque + " ?")) {
                    if (estPrincipal) {
                        int confirmation = JOptionPane.showConfirmDialog(dialog,
                            "Ce véhicule est défini comme principal. Sa suppression désactivera le véhicule principal.\nContinuer quand même ?",
                            "Attention - Véhicule principal",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE);
                        
                        if (confirmation != JOptionPane.YES_OPTION) {
                            return;
                        }
                    }
                    
                    if (VehiculeUsagerDAO.supprimerVehicule(idVehiculeUsager)) {
                        modelVehicules.removeRow(selectedRow);
                        vue.afficherInformation("Véhicule supprimé avec succès");
                    } else {
                        vue.afficherErreur("Erreur lors de la suppression du véhicule");
                    }
                }
            } else {
                vue.afficherInformation("Veuillez sélectionner un véhicule");
            }
        });
        
        btnDefinirPrincipal.addActionListener(e -> {
            int selectedRow = tableVehicules.getSelectedRow();
            if (selectedRow >= 0) {
                int idVehiculeUsager = (int) modelVehicules.getValueAt(selectedRow, 0);
                String plaque = (String) modelVehicules.getValueAt(selectedRow, 1);
                
                // Vérifier si déjà principal
                if ("Oui".equals(modelVehicules.getValueAt(selectedRow, 5))) {
                    JOptionPane.showMessageDialog(dialog,
                        "Ce véhicule est déjà défini comme véhicule principal",
                        "Information",
                        JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                
                if (vue.demanderConfirmation("Définir le véhicule " + plaque + " comme véhicule principal ?")) {
                    if (VehiculeUsagerDAO.definirVehiculePrincipal(idVehiculeUsager, usager.getIdUsager())) {
                        vue.afficherInformation("Véhicule défini comme principal avec succès");
                        // Rafraîchir la liste
                        modelVehicules.setRowCount(0);
                        chargerVehiculesUsager(usager.getIdUsager(), modelVehicules);
                    } else {
                        vue.afficherErreur("Erreur lors de la définition du véhicule principal");
                    }
                }
            } else {
                vue.afficherInformation("Veuillez sélectionner un véhicule");
            }
        });
        
        btnFermer.addActionListener(e -> dialog.dispose());
        
        panelBoutons.add(btnAjouter);
        panelBoutons.add(btnSupprimer);
        panelBoutons.add(btnDefinirPrincipal);
        panelBoutons.add(btnFermer);
        
        panel.add(new JLabel("Véhicules de " + usager.getPrenomUsager() + " " + usager.getNomUsager() + 
                            " (" + usager.getMailUsager() + ")"), BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(panelBoutons, BorderLayout.SOUTH);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    private void ajouterVehiculeDialog(Usager usager, JDialog parentDialog, DefaultTableModel model) {
        JDialog dialog = new JDialog(parentDialog, "Ajouter un véhicule", true);
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(parentDialog);
        dialog.setLayout(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Formulaire
        JLabel lblPlaque = new JLabel("Plaque d'immatriculation:*");
        JTextField txtPlaque = new JTextField(15);
        
        JLabel lblType = new JLabel("Type de véhicule:*");
        JComboBox<String> comboType = new JComboBox<>(new String[]{"Voiture", "Moto", "Camion"});
        
        JLabel lblMarque = new JLabel("Marque (optionnel):");
        JTextField txtMarque = new JTextField(15);
        
        JLabel lblModele = new JLabel("Modèle (optionnel):");
        JTextField txtModele = new JTextField(15);
        
        JCheckBox chkPrincipal = new JCheckBox("Définir comme véhicule principal");
        
        // Ajout des composants
        gbc.gridx = 0; gbc.gridy = 0;
        dialog.add(lblPlaque, gbc);
        gbc.gridx = 1;
        dialog.add(txtPlaque, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        dialog.add(lblType, gbc);
        gbc.gridx = 1;
        dialog.add(comboType, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        dialog.add(lblMarque, gbc);
        gbc.gridx = 1;
        dialog.add(txtMarque, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        dialog.add(lblModele, gbc);
        gbc.gridx = 1;
        dialog.add(txtModele, gbc);
        
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        dialog.add(chkPrincipal, gbc);
        
        // Boutons
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 1;
        JButton btnAnnuler = new JButton("Annuler");
        btnAnnuler.addActionListener(e -> dialog.dispose());
        
        gbc.gridx = 1;
        JButton btnValider = new JButton("Ajouter");
        btnValider.addActionListener(e -> {
            String plaque = txtPlaque.getText().trim().toUpperCase();
            
            // Validation
            if (plaque.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "La plaque d'immatriculation est obligatoire", 
                    "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (!plaque.matches("[A-Z]{2}-\\d{3}-[A-Z]{2}")) {
                JOptionPane.showMessageDialog(dialog, 
                    "Format de plaque invalide. Utilisez le format: AA-123-AA", 
                    "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Vérifier si la plaque existe déjà pour cet utilisateur
            if (VehiculeUsagerDAO.plaqueExistePourUsager(usager.getIdUsager(), plaque)) {
                JOptionPane.showMessageDialog(dialog, 
                    "Cette plaque d'immatriculation est déjà enregistrée pour cet utilisateur", 
                    "Plaque existante", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Créer le véhicule
            VehiculeUsager vehicule = new VehiculeUsager(
                usager.getIdUsager(),
                plaque,
                (String) comboType.getSelectedItem()
            );
            vehicule.setMarque(txtMarque.getText().trim());
            vehicule.setModele(txtModele.getText().trim());
            vehicule.setEstPrincipal(chkPrincipal.isSelected());
            
            // Sauvegarder
            if (VehiculeUsagerDAO.ajouterVehicule(vehicule)) {
                JOptionPane.showMessageDialog(dialog, 
                    "Véhicule ajouté avec succès !", 
                    "Succès", JOptionPane.INFORMATION_MESSAGE);
                
                // Rafraîchir la liste dans le dialogue parent
                model.setRowCount(0);
                chargerVehiculesUsager(usager.getIdUsager(), model);
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, 
                    "Erreur lors de l'ajout du véhicule", 
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        JPanel panelBoutonsDialog = new JPanel(new FlowLayout());
        panelBoutonsDialog.add(btnAnnuler);
        panelBoutonsDialog.add(btnValider);
        
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        dialog.add(panelBoutonsDialog, gbc);
        
        dialog.pack();
        dialog.setVisible(true);
    }
    
    private void chargerVehiculesUsager(int idUsager, DefaultTableModel model) {
        model.setRowCount(0);
        List<VehiculeUsager> vehicules = VehiculeUsagerDAO.getVehiculesByUsager(idUsager);
        
        for (VehiculeUsager v : vehicules) {
            model.addRow(new Object[]{
                v.getIdVehiculeUsager(),
                v.getPlaqueImmatriculation(),
                v.getTypeVehicule(),
                v.getMarque() != null ? v.getMarque() : "",
                v.getModele() != null ? v.getModele() : "",
                v.isEstPrincipal() ? "Oui" : "Non",
                v.getDateAjout().toString()
            });
        }
    }
    
    private void gererAbonnements() {
        Usager usager = vue.getUsagerSelectionne();
        if (usager == null) {
            vue.afficherInformation("Veuillez sélectionner un utilisateur");
            return;
        }
        
        // Récupérer tous les abonnements disponibles
        List<Abonnement> abonnements = AbonnementDAO.getAllAbonnements();
        
        // Créer un tableau pour afficher les abonnements actuels
        String[] abonnementsArray = abonnements.stream()
            .map(a -> a.getLibelleAbonnement() + " (" + a.getTarifAbonnement() + "€)")
            .toArray(String[]::new);
        
        // Vérifier l'abonnement actuel
        Abonnement abonnementActuel = AbonnementDAO.getAbonnementByUsager(usager.getIdUsager());
        String abonnementActuelStr = "Aucun";
        String idAbonnementActuel = null;
        if (AbonnementDAO.hasAbonnement(usager.getIdUsager(), abonnementActuel.getIdAbonnement())) {
        	abonnementActuelStr = abonnementActuel.getLibelleAbonnement();
        	idAbonnementActuel = abonnementActuel.getIdAbonnement();
        }
        
        JDialog dialog = new JDialog(vue, "Gestion des Abonnements", true);
        dialog.setSize(500, 300);
        dialog.setLocationRelativeTo(vue);
        
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(new JLabel("Abonnement actuel: " + abonnementActuel), gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Nouvel abonnement:"), gbc);
        gbc.gridx = 1;
        JComboBox<String> comboAbonnements = new JComboBox<>(abonnementsArray);
        panel.add(comboAbonnements, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        JPanel panelBoutons = new JPanel(new FlowLayout());
        JButton btnAttribuer = new JButton("Attribuer");
        JButton btnSupprimer = new JButton("Supprimer");
        JButton btnFermer = new JButton("Fermer");
        
        btnAttribuer.addActionListener(e -> {
            int selectedIndex = comboAbonnements.getSelectedIndex();
            if (selectedIndex >= 0) {
                Abonnement abonnementSelectionne = abonnements.get(selectedIndex);
                
                if (vue.demanderConfirmation("Attribuer l'abonnement " + 
                    abonnementSelectionne.getLibelleAbonnement() + " à " + 
                    usager.getPrenomUsager() + " " + usager.getNomUsager() + " ?")) {
                    
                    if (AbonnementDAO.ajouterAbonnementUtilisateur(usager.getIdUsager(), 
                        abonnementSelectionne.getIdAbonnement())) {
                        vue.afficherInformation("Abonnement attribué avec succès");
                        vue.chargerUtilisateurs();
                        dialog.dispose();
                    } else {
                        vue.afficherErreur("Erreur lors de l'attribution de l'abonnement");
                    }
                }
            }
        });
        
        btnSupprimer.addActionListener(e -> {
            if (AbonnementDAO.hasAbonnement(usager.getIdUsager(), abonnementActuel.getIdAbonnement())) {
                if (vue.demanderConfirmation("Supprimer l'abonnement de " + 
                    usager.getPrenomUsager() + " " + usager.getNomUsager() + " ?")) {
                    
                    if (AbonnementDAO.supprimerAbonnementUtilisateur(usager.getIdUsager())) {
                        vue.afficherInformation("Abonnement supprimé avec succès");
                        vue.chargerUtilisateurs();
                        dialog.dispose();
                    } else {
                        vue.afficherErreur("Erreur lors de la suppression de l'abonnement");
                    }
                }
            } else {
                vue.afficherInformation("Cet utilisateur n'a pas d'abonnement actif");
            }
        });
        
        btnFermer.addActionListener(e -> dialog.dispose());
        
        panelBoutons.add(btnAttribuer);
        panelBoutons.add(btnSupprimer);
        panelBoutons.add(btnFermer);
        panel.add(panelBoutons, gbc);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    private void voirStationnements() {
        Usager usager = vue.getUsagerSelectionne();
        if (usager == null) {
            vue.afficherInformation("Veuillez sélectionner un utilisateur");
            return;
        }
        
        // Créer une fenêtre pour afficher l'historique des stationnements
        JDialog dialog = new JDialog(vue, "Historique des Stationnements", true);
        dialog.setSize(900, 500);
        dialog.setLocationRelativeTo(vue);
        
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Tableau des stationnements
        String[] colonnes = {"ID", "Type", "Véhicule", "Lieu", "Date début", "Date fin", "Durée", "Coût", "Statut", "Paiement"};
        DefaultTableModel modelStationnements = new DefaultTableModel(colonnes, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable tableStationnements = new JTable(modelStationnements);
        
        // Charger les stationnements
        chargerStationnementsUsager(usager.getIdUsager(), modelStationnements);
        
        JScrollPane scrollPane = new JScrollPane(tableStationnements);
        
        // Panel d'information
        JPanel panelInfo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelInfo.add(new JLabel("Stationnements de " + usager.getPrenomUsager() + " " + 
                                usager.getNomUsager() + " (" + modelStationnements.getRowCount() + " stationnement(s))"));
        
        // Panel de boutons
        JPanel panelBoutons = new JPanel(new FlowLayout());
        JButton btnActualiser = new JButton("Actualiser");
        JButton btnFermer = new JButton("Fermer");
        
        btnActualiser.addActionListener(e -> {
            modelStationnements.setRowCount(0);
            chargerStationnementsUsager(usager.getIdUsager(), modelStationnements);
            vue.afficherInformation("Liste actualisée");
        });
        
        btnFermer.addActionListener(e -> dialog.dispose());
        
        panelBoutons.add(btnActualiser);
        panelBoutons.add(btnFermer);
        
        panel.add(panelInfo, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(panelBoutons, BorderLayout.SOUTH);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    private void chargerStationnementsUsager(int idUsager, DefaultTableModel model) {
        String sql = "SELECT s.*, " +
                    "COALESCE(p.libelle_parking, z.libelle_zone) as lieu, " +
                    "CASE WHEN s.duree_heures > 0 OR s.duree_minutes > 0 " +
                    "THEN CONCAT(s.duree_heures, 'h', LPAD(s.duree_minutes, 2, '0'), 'min') " +
                    "ELSE '-' END as duree, " +
                    "s.statut_paiement " +
                    "FROM Stationnement s " +
                    "LEFT JOIN Parking p ON s.id_parking = p.id_parking " +
                    "LEFT JOIN Zone z ON s.id_zone = z.id_zone " +
                    "WHERE s.id_usager = ? " +
                    "ORDER BY s.date_creation DESC";
        
        try (java.sql.Connection conn = modele.dao.MySQLConnection.getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, idUsager);
            java.sql.ResultSet rs = pstmt.executeQuery();
            
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            
            while (rs.next()) {
                java.sql.Timestamp dateCreation = rs.getTimestamp("date_creation");
                java.sql.Timestamp dateFin = rs.getTimestamp("date_fin");
                
                model.addRow(new Object[]{
                    rs.getInt("id_stationnement"),
                    rs.getString("type_stationnement"),
                    rs.getString("type_vehicule") + " - " + rs.getString("plaque_immatriculation"),
                    rs.getString("lieu"),
                    dateCreation != null ? dateFormat.format(dateCreation) : "-",
                    dateFin != null ? dateFormat.format(dateFin) : "-",
                    rs.getString("duree"),
                    String.format("%.2f €", rs.getDouble("cout")),
                    rs.getString("statut"),
                    rs.getString("statut_paiement")
                });
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void retourAdministration() {
        // Retour à la page d'administration
        String emailAdmin = getEmailAdminFromVue();
        if (emailAdmin != null) {
            new Page_Administration(emailAdmin).setVisible(true);
            vue.dispose();
        } else {
            vue.afficherErreur("Impossible de revenir à l'administration");
        }
    }
    
    private String getEmailAdminFromVue() {
        try {
            // Essaie de récupérer l'email admin de la vue
            if (vue instanceof PageGestionUtilisateurs) {
                // Tu pourrais avoir une méthode getEmailAdmin() dans PageGestionUtilisateurs
                java.lang.reflect.Method method = vue.getClass().getMethod("getEmailAdmin");
                return (String) method.invoke(vue);
            }
        } catch (Exception e) {
            // Si la méthode n'existe pas, retourne un email par défaut
        }
        return "admin@pr.com"; // Valeur par défaut
    }
}
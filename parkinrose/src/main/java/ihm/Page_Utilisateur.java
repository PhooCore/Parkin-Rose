package ihm;

import javax.swing.*;

import controleur.ControleurUtilisateur;
import modele.Usager;
import modele.dao.AbonnementDAO;
import modele.dao.UsagerDAO;
import modele.dao.PaiementDAO;
import modele.dao.StationnementDAO;
import modele.dao.ZoneDAO;
import modele.dao.ParkingDAO;
import modele.dao.VehiculeUsagerDAO;
import modele.Abonnement;
import modele.Paiement;
import modele.Stationnement;
import modele.Zone;
import modele.Parking;
import modele.VehiculeUsager;
import java.awt.*;
import java.util.List;

public class Page_Utilisateur extends JFrame {
    
    private static final long serialVersionUID = 1L;
    private String emailUtilisateur;
    private Usager usager;
    
    // Déclaration des boutons comme attributs
    private JButton btnModifierMdp;
    private JButton btnDeconnexion;
    private JButton btnRetour;
    private JButton btnGestionVehicules;
    private JLabel lblInfoVehicules;
    private JButton btnModifierAdresse;

    
    public Page_Utilisateur(String email, boolean rafraichir) {
        this.emailUtilisateur = email;
        if (rafraichir) {
            this.usager = UsagerDAO.getUsagerByEmail(email);
        } else {
            this.usager = UsagerDAO.getUsagerByEmail(email);
        }
        initialisePage();
        new ControleurUtilisateur(this);
    }
    
    public Page_Utilisateur(String email) {
        this(email, false);
    }
    
    private void initialisePage() {
        this.setTitle("Mon Compte");
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setSize(800, 700); // Augmenté la hauteur pour plus d'espace
        this.setLocationRelativeTo(null);
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);
        
        // Titre de la page
        JLabel lblTitre = new JLabel("Mon Compte", SwingConstants.CENTER);
        lblTitre.setFont(new Font("Arial", Font.BOLD, 20));
        mainPanel.add(lblTitre, BorderLayout.NORTH);
        
        // === SYSTÈME D'ONGLETS ===
        JTabbedPane onglets = new JTabbedPane();
        onglets.setFont(new Font("Arial", Font.PLAIN, 14));
        
        // Onglet 1 : Informations personnelles
        JPanel panelInfos = creerOngletInfos();
        onglets.addTab("Informations", panelInfos);
        
        // Onglet 2 : Historique des paiements
        JPanel panelPaiements = creerOngletPaiements();
        onglets.addTab("Historique des paiements", panelPaiements);
        
        // Onglet 3 : Historique des stationnements
        JPanel panelStationnements = creerOngletStationnements();
        onglets.addTab("Historique des stationnements", panelStationnements);
        
        // Onglet 4 : Gestion des véhicules
        JPanel panelVehicules = creerOngletVehicules();
        onglets.addTab("Mes véhicules", panelVehicules);
        
        mainPanel.add(onglets, BorderLayout.CENTER);
        
        // Bouton retour
        JPanel panelBoutonRetour = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelBoutonRetour.setBackground(Color.WHITE);
        btnRetour = new JButton("Retour à l'accueil");
        panelBoutonRetour.add(btnRetour);
        mainPanel.add(panelBoutonRetour, BorderLayout.SOUTH);
        
        this.setContentPane(mainPanel);
    }
    
    private JPanel creerOngletInfos() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS)); 
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);
        
        // Informations personnelles
        ajouterLigneInfo(panel, "Nom:", usager.getNomUsager());
        ajouterLigneInfo(panel, "Prénom:", usager.getPrenomUsager());
        ajouterLigneInfo(panel, "Email:", usager.getMailUsager());
        
        panel.add(Box.createVerticalStrut(20));
        
        JPanel ligneAdresse = new JPanel(new BorderLayout());
        ligneAdresse.setBackground(Color.WHITE);
        ligneAdresse.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        
        JLabel lblLibelleAdresse = new JLabel("Adresse:");
        lblLibelleAdresse.setFont(new Font("Arial", Font.BOLD, 14));
        lblLibelleAdresse.setPreferredSize(new Dimension(120, 25));
        
        String adresseComplete = UsagerDAO.getAdresseComplete(usager.getIdUsager());
        JLabel lblAdresse = new JLabel(adresseComplete);
        lblAdresse.setFont(new Font("Arial", Font.PLAIN, 14));
        
        btnModifierAdresse = new JButton("Modifier");
        btnModifierAdresse.addActionListener(e -> {
            new Page_Modifier_Adresse(emailUtilisateur).setVisible(true);
            dispose();
        });
        
        
        ligneAdresse.add(lblLibelleAdresse, BorderLayout.WEST);
        ligneAdresse.add(lblAdresse, BorderLayout.CENTER);
        ligneAdresse.add(btnModifierAdresse, BorderLayout.EAST);
        panel.add(ligneAdresse);
        
        panel.add(Box.createVerticalStrut(20));
        
        Abonnement abonnement = AbonnementDAO.getAbonnementByUsager(usager.getIdUsager());
        
        if (abonnement != null && abonnement.getIdAbonnement() != null) {
            ajouterLigneInfo(panel, "Abonnement:", abonnement.getLibelleAbonnement());
            ajouterLigneInfo(panel, "Tarif:", String.format("%.2f €", abonnement.getTarifAbonnement()));
            
            java.sql.Date dateDebut = AbonnementDAO.getDateDebutAbonnement(usager.getIdUsager());
            if (dateDebut != null) {
                ajouterLigneInfo(panel, "Depuis le:", dateDebut.toString());
            }
            
            if (abonnement.estActif()) {
                JLabel lblActif = new JLabel("✓ Actif");
                lblActif.setForeground(Color.GREEN);
                lblActif.setFont(new Font("Arial", Font.BOLD, 12));
                panel.add(lblActif);
                panel.add(Box.createVerticalStrut(5));
            }
            
            panel.add(Box.createVerticalStrut(10));
            
            JPanel panelBoutonsAbo = new JPanel(new FlowLayout(FlowLayout.LEFT));
            panelBoutonsAbo.setBackground(Color.WHITE);
            
            JButton btnChanger = new JButton("Changer d'abonnement");
            btnChanger.addActionListener(e -> changerAbonnement(abonnement));
            
            JButton btnResilier = new JButton("Résilier");
            btnResilier.setBackground(new Color(220, 80, 80));
            btnResilier.setForeground(Color.WHITE);
            btnResilier.addActionListener(e -> resilierAbonnement(abonnement));
            
            panelBoutonsAbo.add(btnChanger);
            panelBoutonsAbo.add(btnResilier);
            panel.add(panelBoutonsAbo);
            
        } else {
            // Pas d'abonnement
            JPanel ligneAbonnement = new JPanel(new BorderLayout());
            ligneAbonnement.setBackground(Color.WHITE);
            ligneAbonnement.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
            
            JLabel lblLibelle = new JLabel("Abonnement:");
            lblLibelle.setFont(new Font("Arial", Font.BOLD, 14));
            lblLibelle.setPreferredSize(new Dimension(120, 25));
            
            JLabel lblValeur = new JLabel("Aucun abonnement actif");
            lblValeur.setFont(new Font("Arial", Font.PLAIN, 14));
            lblValeur.setForeground(Color.RED);
            
            JButton btnSouscrire = new JButton("Souscrire");
            btnSouscrire.setFont(new Font("Arial", Font.PLAIN, 12));
            btnSouscrire.setBackground(new Color(0, 120, 215));
            btnSouscrire.setForeground(Color.WHITE);
            btnSouscrire.setFocusPainted(false);
            btnSouscrire.addActionListener(e -> {
                new Page_Abonnements(emailUtilisateur).setVisible(true);
                dispose();
            });
            
            ligneAbonnement.add(lblLibelle, BorderLayout.WEST);
            ligneAbonnement.add(lblValeur, BorderLayout.CENTER);
            ligneAbonnement.add(btnSouscrire, BorderLayout.EAST);
            panel.add(ligneAbonnement);
        }
        
        panel.add(Box.createVerticalStrut(20));
        
        // Ajout carte Tisseo
        String carteTisseo = UsagerDAO.getCarteTisseoByUsager(usager.getIdUsager());

        if (carteTisseo == null) {
            JPanel ligneCarte = new JPanel(new BorderLayout());
            ligneCarte.setBackground(Color.WHITE);
            ligneCarte.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

            JLabel lblLibelle = new JLabel("Carte Tisséo:");
            lblLibelle.setFont(new Font("Arial", Font.BOLD, 14));
            lblLibelle.setPreferredSize(new Dimension(120, 25));

            JLabel lblValeur = new JLabel("Aucune carte Tisséo renseignée");
            lblValeur.setFont(new Font("Arial", Font.PLAIN, 14));
            lblValeur.setForeground(Color.RED);

            JButton btnAjouter = new JButton("Ajouter une carte");
            btnAjouter.addActionListener(e -> ouvrirPopupAjoutCarteTisseo());

            ligneCarte.add(lblLibelle, BorderLayout.WEST);
            ligneCarte.add(lblValeur, BorderLayout.CENTER);
            ligneCarte.add(btnAjouter, BorderLayout.EAST);

            panel.add(ligneCarte);

        } else {
            ajouterLigneInfo(panel, "Carte Tisséo:", carteTisseo);
        }
        
        panel.add(Box.createVerticalStrut(30));
        
        // Section Véhicules (simplifiée)
        JPanel panelVehiculesInfo = new JPanel(new BorderLayout());
        panelVehiculesInfo.setBackground(Color.WHITE);
        panelVehiculesInfo.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        JLabel lblTitreVehicules = new JLabel("Véhicules enregistrés:");
        lblTitreVehicules.setFont(new Font("Arial", Font.BOLD, 14));
        
        // Compter le nombre de véhicules
        List<VehiculeUsager> vehicules = VehiculeUsagerDAO.getVehiculesByUsager(usager.getIdUsager());
        int nbVehicules = vehicules.size();
        int nbVehiculesPrincipaux = 0;
        for (VehiculeUsager v : vehicules) {
            if (v.isEstPrincipal()) {
                nbVehiculesPrincipaux++;
            }
        }
        
        JLabel lblNbVehicules = new JLabel(nbVehicules + " véhicule(s) - " + nbVehiculesPrincipaux + " principal(aux)");
        lblNbVehicules.setFont(new Font("Arial", Font.PLAIN, 12));
        lblNbVehicules.setForeground(Color.BLUE);
        
        panelVehiculesInfo.add(lblTitreVehicules, BorderLayout.WEST);
        panelVehiculesInfo.add(lblNbVehicules, BorderLayout.EAST);
        panel.add(panelVehiculesInfo);
        
        panel.add(Box.createVerticalStrut(30));
        
        // Boutons d'action
        btnGestionVehicules = new JButton("Gestion complète des véhicules");
        btnGestionVehicules.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnGestionVehicules.setBackground(new Color(70, 130, 180));
        btnGestionVehicules.setForeground(Color.WHITE);
        
        panel.add(btnGestionVehicules);
        panel.add(Box.createVerticalStrut(20));
        
        btnModifierMdp = new JButton("Modifier le mot de passe");
        btnModifierMdp.setAlignmentX(Component.CENTER_ALIGNMENT);
        // Le contrôleur gérera cet ActionListener

        btnDeconnexion = new JButton("Déconnexion");
        btnDeconnexion.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnDeconnexion.setBackground(new Color(220, 80, 80));
        btnDeconnexion.setForeground(Color.WHITE);
        // Le contrôleur gérera cet ActionListener
        
        panel.add(btnModifierMdp);
        panel.add(Box.createVerticalStrut(10));
        panel.add(btnDeconnexion);
        
        return panel;
    }
    
    private JPanel creerOngletVehicules() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);
        
        // Titre
        JLabel lblTitre = new JLabel("Gestion des véhicules", SwingConstants.CENTER);
        lblTitre.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(lblTitre, BorderLayout.NORTH);
        
        // Liste des véhicules
        DefaultListModel<VehiculeUsager> listModel = new DefaultListModel<>();
        JList<VehiculeUsager> listVehicules = new JList<>(listModel);
        listVehicules.setCellRenderer(new VehiculeRenderer());
        listVehicules.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane scrollPane = new JScrollPane(listVehicules);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Vos véhicules"));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Charger les véhicules
        if (usager != null) {
            List<VehiculeUsager> vehicules = VehiculeUsagerDAO.getVehiculesByUsager(usager.getIdUsager());
            for (VehiculeUsager v : vehicules) {
                listModel.addElement(v);
            }
        }
        
        // Panel boutons
        JPanel panelBoutons = new JPanel(new GridLayout(1, 4, 10, 0));
        panelBoutons.setBackground(Color.WHITE);
        
        JButton btnAjouter = new JButton("Ajouter");
        JButton btnModifier = new JButton("Modifier");
        JButton btnSupprimer = new JButton("Supprimer");
        JButton btnDefinirPrincipal = new JButton("Définir principal");
        
        // Écouteurs d'événements
        btnAjouter.addActionListener(e -> {
            Page_Gestion_Vehicules pageGestion = new Page_Gestion_Vehicules(emailUtilisateur);
            pageGestion.setVisible(true);
            pageGestion.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosed(java.awt.event.WindowEvent e) {
                    // Rafraîchir la liste
                    listModel.clear();
                    List<VehiculeUsager> vehicules = VehiculeUsagerDAO.getVehiculesByUsager(usager.getIdUsager());
                    for (VehiculeUsager v : vehicules) {
                        listModel.addElement(v);
                    }
                }
            });
        });
        
        btnModifier.addActionListener(e -> {
            VehiculeUsager vehicule = listVehicules.getSelectedValue();
            if (vehicule != null) {
                JOptionPane.showMessageDialog(this, 
                    "Pour modifier un véhicule, veuillez utiliser la page de gestion complète.",
                    "Information", 
                    JOptionPane.INFORMATION_MESSAGE);
                Page_Gestion_Vehicules pageGestion = new Page_Gestion_Vehicules(emailUtilisateur);
                pageGestion.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Veuillez sélectionner un véhicule à modifier", 
                    "Aucune sélection", 
                    JOptionPane.WARNING_MESSAGE);
            }
        });
        
        btnSupprimer.addActionListener(e -> {
            VehiculeUsager vehicule = listVehicules.getSelectedValue();
            if (vehicule != null) {
                int choix = JOptionPane.showConfirmDialog(this,
                    "Êtes-vous sûr de vouloir supprimer ce véhicule ?\n" +
                    "Plaque: " + vehicule.getPlaqueImmatriculation() + "\n" +
                    "Type: " + vehicule.getTypeVehicule(),
                    "Confirmation de suppression",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
                
                if (choix == JOptionPane.YES_OPTION) {
                    if (VehiculeUsagerDAO.supprimerVehicule(vehicule.getIdVehiculeUsager())) {
                        listModel.removeElement(vehicule);
                        JOptionPane.showMessageDialog(this,
                            "Véhicule supprimé avec succès",
                            "Suppression réussie",
                            JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Veuillez sélectionner un véhicule à supprimer", 
                    "Aucune sélection", 
                    JOptionPane.WARNING_MESSAGE);
            }
        });
        
        btnDefinirPrincipal.addActionListener(e -> {
            VehiculeUsager vehicule = listVehicules.getSelectedValue();
            if (vehicule != null) {
                if (vehicule.isEstPrincipal()) {
                    JOptionPane.showMessageDialog(this,
                        "Ce véhicule est déjà défini comme véhicule principal",
                        "Information",
                        JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                
                int choix = JOptionPane.showConfirmDialog(this,
                    "Définir ce véhicule comme véhicule principal ?\n\n" +
                    "Plaque: " + vehicule.getPlaqueImmatriculation() + "\n" +
                    "Type: " + vehicule.getTypeVehicule(),
                    "Définir comme véhicule principal",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
                
                if (choix == JOptionPane.YES_OPTION) {
                    if (VehiculeUsagerDAO.definirVehiculePrincipal(
                        vehicule.getIdVehiculeUsager(), usager.getIdUsager())) {
                        
                        // Rafraîchir la liste
                        listModel.clear();
                        List<VehiculeUsager> vehicules = VehiculeUsagerDAO.getVehiculesByUsager(usager.getIdUsager());
                        for (VehiculeUsager v : vehicules) {
                            listModel.addElement(v);
                        }
                        
                        JOptionPane.showMessageDialog(this,
                            "Véhicule défini comme principal avec succès",
                            "Succès",
                            JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Veuillez sélectionner un véhicule", 
                    "Aucune sélection", 
                    JOptionPane.WARNING_MESSAGE);
            }
        });
        
        panelBoutons.add(btnAjouter);
        panelBoutons.add(btnModifier);
        panelBoutons.add(btnSupprimer);
        panelBoutons.add(btnDefinirPrincipal);
        
        panel.add(panelBoutons, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel creerOngletPaiements() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        // Récupération des paiements
        List<Paiement> paiements = PaiementDAO.getPaiementsByUsager(usager.getIdUsager());
        
        // En-têtes des colonnes
        String[] colonnes = {"Date", "Montant", "Type", "Détails", "Statut"};
        Object[][] donnees = new Object[paiements.size()][5];
        
        double totalDepense = 0.0;
        double totalAbonnements = 0.0;
        double totalStationnements = 0.0;
        
        for (int i = 0; i < paiements.size(); i++) {
            Paiement p = paiements.get(i);
            
            donnees[i][0] = p.getDatePaiement().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            donnees[i][1] = String.format("%.2f €", p.getMontant());
            donnees[i][2] = p.getTypePaiement();
            
            // Détails spécifiques
            String details = "-";
            if ("ABONNEMENT".equals(p.getTypePaiement()) && p.getIdAbonnement() != null) {
                Abonnement abonnement = AbonnementDAO.getAbonnementById(p.getIdAbonnement());
                details = (abonnement != null) ? abonnement.getLibelleAbonnement() : p.getIdAbonnement();
                totalAbonnements += p.getMontant();
            } else if ("STATIONNEMENT".equals(p.getTypePaiement())) {
                totalStationnements += p.getMontant();
            }
            donnees[i][3] = details;
            
            donnees[i][4] = "Payé";
            
            totalDepense += p.getMontant();
        }
        
        // Création du tableau
        JTable table = new JTable(donnees, colonnes);
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.setRowHeight(25);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        table.setDefaultEditor(Object.class, null);
        
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Panel de statistiques
        JPanel statsPanel = new JPanel(new GridLayout(1, 5, 10, 0));
        statsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        statsPanel.setBackground(Color.WHITE);
        
        // Calcul de la date du dernier paiement
        String dernierPaiement = "Aucun";
        if (!paiements.isEmpty()) {
            dernierPaiement = paiements.get(0).getDatePaiement()
                .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }
        
        // Ajout des statistiques
        ajouterStatistique(statsPanel, "Total dépensé", String.format("%.2f €", totalDepense));
        ajouterStatistique(statsPanel, "Abonnements", String.format("%.2f €", totalAbonnements));
        ajouterStatistique(statsPanel, "Stationnements", String.format("%.2f €", totalStationnements));
        ajouterStatistique(statsPanel, "Nb paiements", String.valueOf(paiements.size()));
        ajouterStatistique(statsPanel, "Dernier", dernierPaiement);
        
        panel.add(statsPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel creerOngletStationnements() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        // Récupération des stationnements
        List<Stationnement> stationnements = StationnementDAO.getHistoriqueStationnements(usager.getIdUsager());
        
        String[] colonnes = {"Date", "Type", "Véhicule", "Zone/Parking", "Durée", "Coût", "Statut"};
        Object[][] donnees = new Object[stationnements.size()][7];
        
        for (int i = 0; i < stationnements.size(); i++) {
            Stationnement s = stationnements.get(i);
            
            donnees[i][0] = s.getDateCreation()
                .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            donnees[i][1] = s.getTypeStationnement();
            donnees[i][2] = s.getTypeVehicule() + " - " + s.getPlaqueImmatriculation();
            
            // Colonne Zone/Parking
            String zoneId = s.getIdTarification();
            if (zoneId == null || zoneId.trim().isEmpty()) {
                donnees[i][3] = "Non spécifié";
            } else {
                if ("PARKING".equals(s.getTypeStationnement())) {
                    Parking parking = ParkingDAO.getParkingById(zoneId);
                    donnees[i][3] = (parking != null) ? parking.getLibelleParking() : zoneId;
                } else {
                    Zone zone = ZoneDAO.getZoneById(zoneId);
                    donnees[i][3] = (zone != null) ? zone.getLibelleZone() : zoneId;
                }
            }
            
            // Durée
            if (s.estVoirie()) {
                donnees[i][4] = s.getDureeHeures() + "h" + s.getDureeMinutes() + "min";
            } else {
                if (s.getHeureArrivee() != null && s.getHeureDepart() != null) {
                    long minutes = java.time.Duration.between(s.getHeureArrivee(), s.getHeureDepart()).toMinutes();
                    long heures = minutes / 60;
                    long mins = minutes % 60;
                    donnees[i][4] = heures + "h" + mins + "min";
                } else {
                    donnees[i][4] = "En cours";
                }
            }
            
            donnees[i][5] = String.format("%.2f €", s.getCout());
            donnees[i][6] = s.getStatut();
        }
        
        // Création du tableau
        JTable table = new JTable(donnees, colonnes);
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.setRowHeight(25);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        table.setDefaultEditor(Object.class, null);
        
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Statistiques
        JPanel statsPanel = new JPanel(new FlowLayout());
        statsPanel.setBackground(Color.WHITE);
        
        long totalStationnements = stationnements.size();
        long stationnementsActifs = stationnements.stream()
                .filter(s -> "ACTIF".equals(s.getStatut()))
                .count();
        
        JLabel lblStats = new JLabel("Total: " + totalStationnements + " stationnement(s) | Actifs: " + stationnementsActifs);
        lblStats.setFont(new Font("Arial", Font.BOLD, 14));
        statsPanel.add(lblStats);
        
        panel.add(statsPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void ajouterLigneInfo(JPanel panel, String libelle, String valeur) {
        JPanel ligne = new JPanel(new BorderLayout());
        ligne.setBackground(Color.WHITE);
        ligne.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        
        JLabel lblLibelle = new JLabel(libelle);
        lblLibelle.setFont(new Font("Arial", Font.BOLD, 14)); 
        lblLibelle.setPreferredSize(new Dimension(120, 25)); 
        
        JLabel lblValeur = new JLabel(valeur);
        lblValeur.setFont(new Font("Arial", Font.PLAIN, 14)); 
        
        ligne.add(lblLibelle, BorderLayout.WEST);
        ligne.add(lblValeur, BorderLayout.CENTER);
        panel.add(ligne);
    }
    
    private void ajouterStatistique(JPanel panel, String libelle, String valeur) {
        JPanel statPanel = new JPanel();
        statPanel.setLayout(new BoxLayout(statPanel, BoxLayout.Y_AXIS));
        statPanel.setBackground(Color.WHITE);
        statPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        
        JLabel lblLibelle = new JLabel(libelle, SwingConstants.CENTER);
        lblLibelle.setFont(new Font("Arial", Font.PLAIN, 12));
        
        JLabel lblValeur = new JLabel(valeur, SwingConstants.CENTER);
        lblValeur.setFont(new Font("Arial", Font.BOLD, 16));
        
        statPanel.add(Box.createVerticalStrut(10));
        statPanel.add(lblLibelle);
        statPanel.add(lblValeur);
        statPanel.add(Box.createVerticalStrut(10));
        
        panel.add(statPanel);
    }
    
    private void ouvrirPopupAjoutCarteTisseo() {
        String numeroCarte = JOptionPane.showInputDialog(
            this,
            "Entrez votre numéro de carte Tisséo (Pastel) :\n" +
            "Format : 10 chiffres (ex: 1234567890)",
            "Carte Tisséo",
            JOptionPane.PLAIN_MESSAGE
        );

        if (numeroCarte != null && !numeroCarte.trim().isEmpty()) {
            if (!numeroCarte.matches("\\d{10}")) {
                JOptionPane.showMessageDialog(this,
                    "Le format est incorrect.\n" +
                    "Veuillez entrer 10 chiffres (ex: 1234567890).",
                    "Numéro invalide", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            try {
                UsagerDAO.enregistrerCarteTisseo(usager.getIdUsager(), numeroCarte.trim());
                
                JOptionPane.showMessageDialog(this,
                    "✅ Carte Tisséo enregistrée avec succès\n\n" +
                    "Numéro : " + numeroCarte + "\n" +
                    "Vous pouvez maintenant utiliser les parkings relais gratuitement.",
                    "Succès",
                    JOptionPane.INFORMATION_MESSAGE
                );

                // Rafraîchir la page
                new Page_Utilisateur(emailUtilisateur, true).setVisible(true);
                dispose();
                
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Erreur lors de l'enregistrement de la carte : " + e.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }
    
    private void retourAccueil() {
        // Retour à la page principale
        dispose();
    }
    
    private void changerAbonnement(Abonnement abonnementActuel) {
        String message = "Vous avez actuellement l'abonnement : " + abonnementActuel.getLibelleAbonnement() + "\n\n" +
                        "⚠️ En changeant d'abonnement, votre abonnement actuel sera résilié.\n" +
                        "Le montant déjà payé ne sera pas remboursé.\n\n" +
                        "Voulez-vous continuer ?";
        
        int choix = JOptionPane.showConfirmDialog(
            this,
            message,
            "Changement d'abonnement",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (choix == JOptionPane.YES_OPTION) {
            boolean supprime = AbonnementDAO.supprimerAbonnementUtilisateur(usager.getIdUsager());
            
            if (supprime) {
                JOptionPane.showMessageDialog(
                    this,
                    "Votre ancien abonnement a été résilié.\nVous allez être redirigé vers la page des abonnements.",
                    "Abonnement résilié",
                    JOptionPane.INFORMATION_MESSAGE
                );
                
                new Page_Abonnements(emailUtilisateur).setVisible(true);
                dispose();
            } else {
                JOptionPane.showMessageDialog(
                    this,
                    "Erreur lors de la résiliation de l'abonnement.\nVeuillez réessayer ou contacter le support.",
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    private void resilierAbonnement(Abonnement abonnementActuel) {
        String message = "⚠️ ATTENTION ⚠️\n\n" +
                        "Vous êtes sur le point de résilier votre abonnement :\n" +
                        abonnementActuel.getLibelleAbonnement() + " - " + 
                        String.format("%.2f €", abonnementActuel.getTarifAbonnement()) + "\n\n" +
                        "Conséquences :\n" +
                        "• Perte de tous les avantages\n" +
                        "• Aucun remboursement\n" +
                        "• Retour aux tarifs standards\n\n" +
                        "Êtes-vous sûr de vouloir résilier et perdre votre argent ?";
        
        int choix = JOptionPane.showConfirmDialog(
            this,
            message,
            "Résiliation d'abonnement",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (choix == JOptionPane.YES_OPTION) {
            int confirmation2 = JOptionPane.showConfirmDialog(
                this,
                "Dernière confirmation :\n\n" +
                "Vous allez perdre " + String.format("%.2f €", abonnementActuel.getTarifAbonnement()) + "\n\n" +
                "Confirmez-vous définitivement la résiliation ?",
                "Confirmation finale",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );
            
            if (confirmation2 == JOptionPane.YES_OPTION) {
                boolean supprime = AbonnementDAO.supprimerAbonnementUtilisateur(usager.getIdUsager());
                
                if (supprime) {
                    JOptionPane.showMessageDialog(
                        this,
                        "Votre abonnement a été résilié avec succès.\n\n" +
                        "Vous pouvez souscrire à un nouvel abonnement à tout moment.",
                        "Résiliation confirmée",
                        JOptionPane.INFORMATION_MESSAGE
                    );
                    
                    new Page_Utilisateur(emailUtilisateur, true).setVisible(true);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(
                        this,
                        "Erreur lors de la résiliation.\nVeuillez contacter le support.",
                        "Erreur",
                        JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        }
    }
    
    // GETTERS POUR LE CONTROLEUR
    public String getEmailUtilisateur() {
        return emailUtilisateur;
    }
    
    public Usager getUsager() {
        return usager;
    }
    
    public JButton getBtnModifierMdp() {
        return btnModifierMdp;
    }
    
    public JButton getBtnDeconnexion() {
        return btnDeconnexion;
    }
    
    public JButton getBtnRetour() {
        return btnRetour;
    }
    
    public JButton getBtnGestionVehicules() {
        return btnGestionVehicules;
    }
    
    public JLabel getLblInfoVehicules() {
        return lblInfoVehicules;
    }
    
    public JButton getBtnModifierAdresse() {
        return btnModifierAdresse;
    }
    
    // Renderer pour la liste des véhicules
    private class VehiculeRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, 
                                                     int index, boolean isSelected, 
                                                     boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            VehiculeUsager vehicule = (VehiculeUsager) value;
            
            StringBuilder texte = new StringBuilder("<html>");
            
            if (vehicule.isEstPrincipal()) {
                texte.append("<b>★ ").append(vehicule.getPlaqueImmatriculation()).append("</b>");
            } else {
                texte.append(vehicule.getPlaqueImmatriculation());
            }
            
            texte.append(" - ").append(vehicule.getTypeVehicule());
            
            if (vehicule.getMarque() != null && !vehicule.getMarque().isEmpty()) {
                texte.append(" ").append(vehicule.getMarque());
            }
            if (vehicule.getModele() != null && !vehicule.getModele().isEmpty()) {
                texte.append(" ").append(vehicule.getModele());
            }
            
            texte.append("</html>");
            
            setText(texte.toString());
            
            if (isSelected) {
                setBackground(new Color(220, 240, 255));
                setForeground(Color.BLACK);
            } else {
                if (vehicule.isEstPrincipal()) {
                    setBackground(new Color(255, 255, 220));
                } else {
                    setBackground(Color.WHITE);
                }
            }
            
            return this;
        }
    }
}
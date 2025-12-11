package ihm;

import javax.swing.*;
import modele.Abonnement;
import modele.dao.AbonnementDAO;
import java.awt.*;
import java.util.List;

public class Page_Abonnements extends JFrame {
    
    private String emailUtilisateur;
    private int idUsager;
    
    public Page_Abonnements(String email) {
        this.emailUtilisateur = email;
        this.idUsager = modele.dao.UsagerDAO.getUsagerByEmail(email).getIdUsager();
        initialiserPage();
    }
    
    private void initialiserPage() {
        setTitle("Abonnements disponibles");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);
        
        // Titre
        JLabel lblTitre = new JLabel("Choisissez votre abonnement", SwingConstants.CENTER);
        lblTitre.setFont(new Font("Arial", Font.BOLD, 24));
        mainPanel.add(lblTitre, BorderLayout.NORTH);
        
        // Récupérer tous les abonnements disponibles
        List<Abonnement> abonnements = AbonnementDAO.getAllAbonnements();
        
        // Panel pour les cartes d'abonnement
        JPanel panelAbonnements = new JPanel(new GridLayout(0, 1, 0, 15));
        panelAbonnements.setBackground(Color.WHITE);
        
        for (Abonnement abonnement : abonnements) {
            JPanel carte = creerCarteAbonnement(abonnement);
            panelAbonnements.add(carte);
        }
        
        JScrollPane scrollPane = new JScrollPane(panelAbonnements);
        scrollPane.setBorder(null);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Bouton retour
        JButton btnRetour = new JButton("Retour au compte");
        btnRetour.addActionListener(e -> {
            new Page_Utilisateur(emailUtilisateur, true).setVisible(true);
            dispose();
        });
        
        JPanel panelBouton = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelBouton.setBackground(Color.WHITE);
        panelBouton.add(btnRetour);
        mainPanel.add(panelBouton, BorderLayout.SOUTH);
        
        setContentPane(mainPanel);
    }
    
    private JPanel creerCarteAbonnement(Abonnement abonnement) {
        JPanel carte = new JPanel(new BorderLayout(15, 10));
        carte.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        carte.setBackground(Color.WHITE);
        
        // Partie gauche : Informations de base
        JPanel panelInfo = new JPanel(new GridLayout(0, 1, 5, 5));
        panelInfo.setBackground(Color.WHITE);
        
        JLabel lblTitre = new JLabel(abonnement.getLibelleAbonnement());
        lblTitre.setFont(new Font("Arial", Font.BOLD, 16));
        lblTitre.setForeground(new Color(0, 100, 200));
        
        JLabel lblTarif = new JLabel(String.format("%.2f €", abonnement.getTarifAbonnement()));
        lblTarif.setFont(new Font("Arial", Font.BOLD, 18));
        lblTarif.setForeground(new Color(0, 150, 0));
        
        JLabel lblId = new JLabel("Code : " + abonnement.getIdAbonnement());
        lblId.setFont(new Font("Arial", Font.ITALIC, 12));
        lblId.setForeground(Color.GRAY);
        
        panelInfo.add(lblTitre);
        panelInfo.add(lblTarif);
        panelInfo.add(lblId);
        
        // Partie droite : Bouton de sélection
        JButton btnChoisir = new JButton("Choisir cet abonnement");
        btnChoisir.setBackground(new Color(0, 120, 215));
        btnChoisir.setForeground(Color.WHITE);
        btnChoisir.setFont(new Font("Arial", Font.BOLD, 14));
        btnChoisir.setFocusPainted(false);
        
        // Vérifier si l'utilisateur a déjà cet abonnement
        if (AbonnementDAO.hasAbonnement(idUsager, abonnement.getIdAbonnement())) {
            btnChoisir.setText("Déjà souscrit");
            btnChoisir.setEnabled(false);
            btnChoisir.setBackground(Color.GRAY);
        }
        
        btnChoisir.addActionListener(e -> {
            // Ouvrir la page de paiement pour cet abonnement
            new Page_Paiement_Abonnement(emailUtilisateur, abonnement).setVisible(true);
            dispose();
        });
        
        JPanel panelBouton = new JPanel(new BorderLayout());
        panelBouton.setBackground(Color.WHITE);
        panelBouton.add(btnChoisir, BorderLayout.CENTER);
        
        carte.add(panelInfo, BorderLayout.CENTER);
        carte.add(panelBouton, BorderLayout.EAST);
        
        return carte;
    }
}
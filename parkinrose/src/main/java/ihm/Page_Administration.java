package ihm;

import javax.swing.*;
import java.awt.*;

public class Page_Administration extends JFrame {
    
    private static final long serialVersionUID = 1L;
    private String emailAdmin;
    
    public Page_Administration(String emailAdmin) {
        this.emailAdmin = emailAdmin;
        initialisePage();
    }
    
    private void initialisePage() {
        setTitle("Administration - Parkin'Rose");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(800, 500);
        setLocationRelativeTo(null);
        setResizable(false);
        
        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        mainPanel.setBackground(Color.WHITE);
        
        // === PANEL HAUT - TITRE ===
        JPanel panelTitre = new JPanel(new BorderLayout());
        panelTitre.setBackground(Color.WHITE);
        
        JLabel lblTitre = new JLabel("Panneau d'Administration", SwingConstants.CENTER);
        lblTitre.setFont(new Font("Arial", Font.BOLD, 28));
        lblTitre.setForeground(new Color(0, 102, 204));
        
        JLabel lblInfoAdmin = new JLabel("Connecté en tant que: " + emailAdmin);
        lblInfoAdmin.setFont(new Font("Arial", Font.ITALIC, 12));
        lblInfoAdmin.setForeground(Color.GRAY);
        
        panelTitre.add(lblTitre, BorderLayout.CENTER);
        panelTitre.add(lblInfoAdmin, BorderLayout.SOUTH);
        mainPanel.add(panelTitre, BorderLayout.NORTH);
        
        // === PANEL CENTRE - OPTIONS ===
        JPanel panelOptions = new JPanel(new GridLayout(2, 1, 30, 30));
        panelOptions.setBackground(Color.WHITE);
        panelOptions.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        
        // Option 1 : Gestion des utilisateurs
        JPanel panelUtilisateurs = creerPanelOption(
            "Gestion des Utilisateurs",
            new Color(70, 130, 180) // Bleu
        );
        
        panelUtilisateurs.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                ouvrirGestionUtilisateurs();
            }
        });
        
        // Option 2 : Gestion des parkings
        JPanel panelParkings = creerPanelOption(
            "Gestion des Parkings",
            new Color(60, 179, 113) // Vert
        );
        
        panelParkings.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                ouvrirCarteParkings();
            }
        });
        
        panelOptions.add(panelUtilisateurs);
        panelOptions.add(panelParkings);
        
        mainPanel.add(panelOptions, BorderLayout.CENTER);
        
        // === PANEL BAS - BOUTON RETOUR ===
        JPanel panelBas = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelBas.setBackground(Color.WHITE);
        
        JButton btnRetour = new JButton("Retour à l'accueil");
        btnRetour.setFont(new Font("Arial", Font.PLAIN, 14));
        btnRetour.setBackground(new Color(169, 169, 169));
        btnRetour.setForeground(Color.WHITE);
        btnRetour.setFocusPainted(false);
        btnRetour.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRetour.addActionListener(e -> {
            retourAccueil();
        });
        
        panelBas.add(btnRetour);
        mainPanel.add(panelBas, BorderLayout.SOUTH);
        
        setContentPane(mainPanel);
    }
    
    private JPanel creerPanelOption(String titre, Color couleur) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(couleur, 3),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        panel.setBackground(new Color(240, 240, 240));
        panel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Animation au survol
        panel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                panel.setBackground(new Color(250, 250, 250));
                panel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(couleur.brighter(), 3),
                    BorderFactory.createEmptyBorder(20, 20, 20, 20)
                ));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                panel.setBackground(new Color(240, 240, 240));
                panel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(couleur, 3),
                    BorderFactory.createEmptyBorder(20, 20, 20, 20)
                ));
            }
        });
        
        // Titre uniquement
        JLabel lblTitre = new JLabel(titre);
        lblTitre.setFont(new Font("Arial", Font.BOLD, 28));
        lblTitre.setForeground(couleur);
        
        panel.add(lblTitre);
        
        return panel;
    }
    
    private void ouvrirGestionUtilisateurs() {
        // Ouvre la page de gestion des utilisateurs
        PageGestionUtilisateurs pageGestion = new PageGestionUtilisateurs(emailAdmin);
        pageGestion.setVisible(true);
        this.dispose();
    }
    
    private void ouvrirCarteParkings() {
        // Ouvre la fenêtre avec la carte des parkings
        JFrame frameCarte = new JFrame("Administration des Parkings - Carte de Toulouse");
        frameCarte.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frameCarte.setSize(1200, 800);
        frameCarte.setLocationRelativeTo(null);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        try {
            // Charger l'image de la carte de Toulouse
            java.net.URL imageUrl = getClass().getResource("/images/Map_Toulouse.jpg");
            if (imageUrl == null) {
                throw new Exception("Image Map_Toulouse.jpg non trouvée dans /images/");
            }
            
            // Créer la carte
            CarteAdminPanel cartePanel = new CarteAdminPanel(imageUrl, emailAdmin);
            
            // Panel de contrôle en haut
            JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
            controlPanel.setBackground(Color.WHITE);
            controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            
            // Boutons d'actions
            JButton btnAjouterMode = new JButton("Mode Ajout");
            JButton btnModifierMode = new JButton("Mode Modification");
            JButton btnSupprimer = new JButton("Supprimer");
            JButton btnRetourAdmin = new JButton("← Retour à l'administration");
            
            // Style des boutons
            Color vert = new Color(60, 179, 113);
            Color orange = new Color(255, 165, 0);
            Color rouge = new Color(220, 53, 69);
            Color bleu = new Color(52, 152, 219);
            
            btnAjouterMode.setBackground(vert);
            btnModifierMode.setBackground(orange);
            btnSupprimer.setBackground(rouge);
            btnRetourAdmin.setBackground(bleu);
            
            Color textColor = Color.WHITE;
            btnAjouterMode.setForeground(textColor);
            btnModifierMode.setForeground(textColor);
            btnSupprimer.setForeground(textColor);
            btnRetourAdmin.setForeground(textColor);
            
            // Taille des boutons
            Dimension btnSize = new Dimension(180, 40);
            Dimension btnRetourSize = new Dimension(200, 40);
            
            btnAjouterMode.setPreferredSize(btnSize);
            btnModifierMode.setPreferredSize(btnSize);
            btnSupprimer.setPreferredSize(btnSize);
            btnRetourAdmin.setPreferredSize(btnRetourSize);
            
            btnAjouterMode.setFont(new Font("Arial", Font.BOLD, 14));
            btnModifierMode.setFont(new Font("Arial", Font.BOLD, 14));
            btnSupprimer.setFont(new Font("Arial", Font.BOLD, 14));
            btnRetourAdmin.setFont(new Font("Arial", Font.BOLD, 14));
            
            btnAjouterMode.setFocusPainted(false);
            btnModifierMode.setFocusPainted(false);
            btnSupprimer.setFocusPainted(false);
            btnRetourAdmin.setFocusPainted(false);
            
            // Actions
            btnAjouterMode.addActionListener(e -> cartePanel.modeAjout());
            btnModifierMode.addActionListener(e -> cartePanel.modeModification());
            btnSupprimer.addActionListener(e -> cartePanel.supprimerParkingSelectionne());
            btnRetourAdmin.addActionListener(e -> {
                frameCarte.dispose();
                new Page_Administration(emailAdmin).setVisible(true);
            });
            
            controlPanel.add(btnAjouterMode);
            controlPanel.add(btnModifierMode);
            controlPanel.add(btnSupprimer);
            controlPanel.add(btnRetourAdmin);
            
            // Panel d'information en bas
            JPanel infoPanel = new JPanel(new BorderLayout());
            infoPanel.setBackground(new Color(240, 240, 240));
            infoPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            
            JLabel lblInfo = new JLabel("<html><b>Carte interactive des parkings</b> - Cliquez sur les parkings pour les gérer</html>");
            lblInfo.setForeground(Color.DARK_GRAY);
            infoPanel.add(lblInfo, BorderLayout.WEST);
            
            JLabel lblCompteur = new JLabel("Parkings chargés: 0");
            lblCompteur.setForeground(new Color(70, 130, 180));
            lblCompteur.setFont(new Font("Arial", Font.BOLD, 12));
            infoPanel.add(lblCompteur, BorderLayout.EAST);
            
            // Mettre à jour le compteur
            cartePanel.setCompteurLabel(lblCompteur);
            
            // Assemblages
            mainPanel.add(controlPanel, BorderLayout.NORTH);
            mainPanel.add(cartePanel, BorderLayout.CENTER);
            mainPanel.add(infoPanel, BorderLayout.SOUTH);
            
        } catch (Exception e) {
            JLabel lblErreur = new JLabel("<html><center><font color='red'>Erreur: " + e.getMessage() + 
                                        "</font><br>Impossible de charger la carte</center></html>");
            lblErreur.setFont(new Font("Arial", Font.BOLD, 14));
            lblErreur.setHorizontalAlignment(SwingConstants.CENTER);
            mainPanel.add(lblErreur, BorderLayout.CENTER);
            e.printStackTrace();
        }
        
        frameCarte.setContentPane(mainPanel);
        frameCarte.setVisible(true);
        
        // Fermer la page d'administration actuelle
        this.dispose();
    }
    
    private void retourAccueil() {
        // Retour à la page principale
        Page_Principale pagePrincipale = new Page_Principale(emailAdmin);
        pagePrincipale.setVisible(true);
        dispose();
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Pour tester
            new Page_Administration("admin@pr.com").setVisible(true);
        });
    }
}
package ihm;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import modèle.Parking;
import dao.ParkingDAO;
import dao.TarifParkingDAO;


public class Page_Tous_Parkings extends JFrame {
    
    private static final long serialVersionUID = 1L;
    private String emailUtilisateur;          // Email de l'utilisateur connecté
    private List<Parking> parkings;           // Liste de tous les parkings
    private JPanel panelParkings;             // Panel pour afficher les parkings

    /**
     * Constructeur de la page affichant tous les parkings
     * @param email l'email de l'utilisateur connecté
     * @param parkings la liste des parkings à afficher
     */
    public Page_Tous_Parkings(String email, List<Parking> parkings) {
        this.emailUtilisateur = email;
        this.parkings = parkings;
        initialisePage(); // Initialisation de l'interface
    }
    
    /**
     * Initialise l'interface utilisateur de la page
     * Structure : En-tête + Liste des parkings + Boutons
     */
    private void initialisePage() {
        // Configuration de la fenêtre
        this.setTitle("Tous les parkings");
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Ne ferme que cette fenêtre
        this.setSize(800, 600);
        this.setLocationRelativeTo(null); // Centre la fenêtre
        
        // Panel principal avec bordures
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Marge de 20px
        mainPanel.setBackground(Color.WHITE);
        
        // === EN-TÊTE DE LA PAGE ===
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        
        // Bouton retour vers l'accueil
        JButton btnRetour = new JButton("← Retour");
        btnRetour.addActionListener(e -> retourAccueil());
        btnRetour.setBackground(Color.WHITE);
        btnRetour.setFocusPainted(false); // Désactive l'effet de focus
        headerPanel.add(btnRetour, BorderLayout.WEST);
        
        // Titre avec le nombre total de parkings
        JLabel lblTitre = new JLabel("Tous les parkings (" + parkings.size() + ")", SwingConstants.CENTER);
        lblTitre.setFont(new Font("Arial", Font.BOLD, 18));
        headerPanel.add(lblTitre, BorderLayout.CENTER);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // === LISTE DES PARKINGS ===
        panelParkings = new JPanel();
        panelParkings.setLayout(new BoxLayout(panelParkings, BoxLayout.Y_AXIS)); // Layout vertical
        panelParkings.setBackground(Color.WHITE);
        
        // Scroll pane pour permettre le défilement si nombreux parkings
        JScrollPane scrollPane = new JScrollPane(panelParkings);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0)); // Marge supérieure
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        afficherParkings(); // Remplissage de la liste des parkings
        
        this.setContentPane(mainPanel);
    }
    
    /**
     * Affiche tous les parkings dans le panel
     * Gère le cas où aucun parking n'est disponible
     */
    private void afficherParkings() {
        panelParkings.removeAll(); // Vide le panel avant de le remplir
        
        if (parkings.isEmpty()) {
            // === CAS AUCUN PARKING DISPONIBLE ===
            JLabel lblAucun = new JLabel("Aucun parking disponible", SwingConstants.CENTER);
            lblAucun.setFont(new Font("Arial", Font.PLAIN, 16));
            lblAucun.setForeground(Color.GRAY);
            lblAucun.setAlignmentX(Component.CENTER_ALIGNMENT); // Centrage horizontal
            panelParkings.add(lblAucun);
        } else {
            // === CAS AVEC PARKINGS ===
            // Création d'une carte pour chaque parking
            for (Parking parking : parkings) {
                panelParkings.add(creerCarteParking(parking));
                panelParkings.add(Box.createRigidArea(new Dimension(0, 10))); // Espacement entre les cartes
            }
        }
        
        // Actualisation de l'affichage
        panelParkings.revalidate();
        panelParkings.repaint();
    }
    
    /**
     * Crée une carte visuelle pour représenter un parking
     * @param parking l'objet Parking à afficher
     * @return JPanel représentant la carte du parking
     */
    private JPanel creerCarteParking(Parking parking) {
        JPanel carte = new JPanel();
        carte.setLayout(new BorderLayout());
        carte.setBackground(Color.WHITE);
        // Bordure grise avec padding interne
        carte.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)), // Bordure externe
            BorderFactory.createEmptyBorder(15, 15, 15, 15) // Marge interne
        ));
        carte.setMaximumSize(new Dimension(700, 120)); // Taille maximale fixe
        carte.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Curseur main au survol
        
        // === PANEL DES INFORMATIONS (partie gauche) ===
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(Color.WHITE);
        
        // Nom du parking (en bleu et gras)
        JLabel lblNom = new JLabel(parking.getLibelleParking());
        lblNom.setFont(new Font("Arial", Font.BOLD, 16));
        lblNom.setForeground(new Color(0, 100, 200)); // Bleu
        
        // Adresse du parking
        JLabel lblAdresse = new JLabel(parking.getAdresseParking());
        lblAdresse.setFont(new Font("Arial", Font.PLAIN, 14));
        lblAdresse.setForeground(Color.DARK_GRAY);
        
        // === PANEL DES DÉTAILS (indicateurs + infos techniques) ===
        JPanel detailsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        detailsPanel.setBackground(Color.WHITE);
        
        // Indicateur nombre de places (disponibles/total)
        JLabel lblPlaces = new JLabel("Places: " + parking.getPlacesDisponibles() + "/" + parking.getNombrePlaces());
        lblPlaces.setFont(new Font("Arial", Font.PLAIN, 12));
        
        // Indicateur hauteur maximale autorisée
        JLabel lblHauteur = new JLabel("Hauteur max: " + parking.getHauteurParking() + "m");
        lblHauteur.setFont(new Font("Arial", Font.PLAIN, 12));
        
        // === INDICATEURS SPÉCIAUX (badges colorés) ===
        
        // Indicateur parking gratuit (étoile verte)
        if (TarifParkingDAO.estParkingGratuit(parking.getIdParking())) {
            JLabel lblGratuit = new JLabel("★ GRATUIT");
            lblGratuit.setFont(new Font("Arial", Font.BOLD, 12));
            lblGratuit.setForeground(Color.GREEN.darker()); // Vert foncé
            detailsPanel.add(lblGratuit);
        }
        
        // Indicateur tarif soirée (étoile orange)
        if (TarifParkingDAO.proposeTarifSoiree(parking.getIdParking())) {
            JLabel lblSoiree = new JLabel("★ Tarif soirée");
            lblSoiree.setFont(new Font("Arial", Font.BOLD, 12));
            lblSoiree.setForeground(Color.ORANGE.darker()); // Orange foncé
            detailsPanel.add(lblSoiree);
        }
        
        // Indicateur parking relais (étoile bleue)
        if (TarifParkingDAO.estParkingRelais(parking.getIdParking())) {
            JLabel lblRelais = new JLabel("★ Parking relais");
            lblRelais.setFont(new Font("Arial", Font.BOLD, 12));
            lblRelais.setForeground(Color.BLUE.darker()); // Bleu foncé
            detailsPanel.add(lblRelais);
        }
        
        // Ajout des informations techniques aux indicateurs
        detailsPanel.add(lblPlaces);
        detailsPanel.add(lblHauteur);
        
        // === ASSEMBLAGE DU PANEL D'INFORMATIONS ===
        infoPanel.add(lblNom);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 5))); // Espacement
        infoPanel.add(lblAdresse);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 5))); // Espacement
        infoPanel.add(detailsPanel);
        
        carte.add(infoPanel, BorderLayout.CENTER);
        
        // === BOUTON D'ACTION (partie droite) ===
        JButton btnSelect = new JButton("Stationner ici");
        btnSelect.setPreferredSize(new Dimension(120, 35)); // Taille fixe
        btnSelect.addActionListener(e -> selectionnerParking(parking));
        
        carte.add(btnSelect, BorderLayout.EAST);
        
        return carte;
    }
    
    /**
     * Gère la sélection d'un parking par l'utilisateur
     * Affiche une confirmation avec les détails du parking avant redirection
     * @param parking le parking sélectionné
     */
    private void selectionnerParking(Parking parking) {
        // Message de confirmation avec les détails du parking
        int choix = JOptionPane.showConfirmDialog(this,
            "Voulez-vous préparer un stationnement pour :\n" +
            parking.getLibelleParking() + "\n" +
            parking.getAdresseParking() + "\n\n" +
            "Places disponibles: " + parking.getPlacesDisponibles() + "/" + parking.getNombrePlaces() + "\n" +
            "Hauteur maximale: " + parking.getHauteurParking() + "m",
            "Confirmation",
            JOptionPane.YES_NO_OPTION);
            
        if (choix == JOptionPane.YES_OPTION) {
            // CORRECTION : Passage des paramètres nécessaires au constructeur
            Page_Garer_Parking pageParking = new Page_Garer_Parking();//email et parking
            pageParking.setVisible(true);
            dispose(); // Ferme la page actuelle
        }
    }
    
    /**
     * Retourne à la page principale (accueil)
     */
    private void retourAccueil() {
        Page_Principale pagePrincipale = new Page_Principale(emailUtilisateur);
        pagePrincipale.setVisible(true);
        dispose(); // Ferme la page actuelle
    }
}
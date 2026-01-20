package ihm;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;

import modele.Parking;
import modele.dao.FavoriDAO;
import modele.dao.TarifParkingDAO;

public class Page_Favoris extends JFrame {

    private static final long serialVersionUID = 1L;


    private static final Color COULEUR_FOND = Color.WHITE;
    private static final Color COULEUR_PRIMAIRE = new Color(0, 100, 200);
    private static final Color COULEUR_SECONDAIRE = Color.DARK_GRAY;
    private static final Color COULEUR_BORDURE = new Color(200, 200, 200);

    private static final Dimension DIMENSION_CARTE = new Dimension(800, 140);
    private static final Dimension DIMENSION_BOUTON = new Dimension(140, 35);

    private static final Font POLICE_TITRE = new Font("Arial", Font.BOLD, 18);
    private static final Font POLICE_NOM = new Font("Arial", Font.BOLD, 16);
    private static final Font POLICE_ADRESSE = new Font("Arial", Font.PLAIN, 14);
    private static final Font POLICE_DETAIL = new Font("Arial", Font.PLAIN, 12);


    private final String emailUtilisateur;
    private final int idUsager;

    private List<Parking> parkingsFavoris;


    private JPanel panelResultats;
    private JLabel lblTitre;

    private static final ImageIcon COEUR_REMPLI =
            new ImageIcon(Page_Favoris.class.getResource("/images/coeurRempli.png"));


    public Page_Favoris(String emailUtilisateur, int idUsager) {
        this.emailUtilisateur = emailUtilisateur;
        this.idUsager = idUsager;
        this.parkingsFavoris = new ArrayList<>();

        chargerParkingsFavoris();
        initialiserPage();
    }


    private void initialiserPage() {
        setTitle("Mes parkings favoris");
        setSize(900, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(COULEUR_FOND);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        mainPanel.add(creerHeader(), BorderLayout.NORTH);
        mainPanel.add(creerPanelResultats(), BorderLayout.CENTER);

        setContentPane(mainPanel);
        afficherParkingsFavoris();
    }


    private JPanel creerHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(COULEUR_FOND);

        JButton btnRetour = new JButton("← Retour");
        btnRetour.addActionListener(e -> dispose());

        lblTitre = new JLabel("Mes parkings favoris", SwingConstants.CENTER);
        lblTitre.setFont(POLICE_TITRE);

        header.add(btnRetour, BorderLayout.WEST);
        header.add(lblTitre, BorderLayout.CENTER);

        return header;
    }


    private void chargerParkingsFavoris() {
        try {
            parkingsFavoris = FavoriDAO
                    .getInstance()
                    .getParkingsFavoris(idUsager);
        } catch (Exception e) {
            parkingsFavoris = new ArrayList<>();
            JOptionPane.showMessageDialog(
                    this,
                    "Erreur lors du chargement des parkings favoris",
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }


    private JScrollPane creerPanelResultats() {
        panelResultats = new JPanel();
        panelResultats.setLayout(new BoxLayout(panelResultats, BoxLayout.Y_AXIS));
        panelResultats.setBackground(COULEUR_FOND);

        JScrollPane scroll = new JScrollPane(panelResultats);
        scroll.setBorder(BorderFactory.createEmptyBorder());

        return scroll;
    }

    private void afficherParkingsFavoris() {
        panelResultats.removeAll();

        if (parkingsFavoris.isEmpty()) {
            afficherAucunFavori();
        } else {
            for (Parking parking : parkingsFavoris) {
                panelResultats.add(creerCarteParking(parking));
                panelResultats.add(Box.createVerticalStrut(10));
            }
        }

        panelResultats.revalidate();
        panelResultats.repaint();
    }

    private void afficherAucunFavori() {
        JLabel lbl = new JLabel("Vous n'avez encore aucun parking en favori.");
        lbl.setFont(new Font("Arial", Font.PLAIN, 16));
        lbl.setForeground(Color.GRAY);
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        panelResultats.add(Box.createVerticalGlue());
        panelResultats.add(lbl);
        panelResultats.add(Box.createVerticalGlue());
    }


    private JPanel creerCarteParking(Parking parking) {
        JPanel carte = new JPanel(new BorderLayout());
        carte.setBackground(COULEUR_FOND);
        carte.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COULEUR_BORDURE),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        carte.setMaximumSize(DIMENSION_CARTE);

        carte.add(creerPanelInformations(parking), BorderLayout.CENTER);
        carte.add(creerPanelBoutons(parking), BorderLayout.EAST);

        return carte;
    }

    private JPanel creerPanelInformations(Parking parking) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(COULEUR_FOND);

        JLabel lblNom = new JLabel(parking.getLibelleParking());
        lblNom.setFont(POLICE_NOM);
        lblNom.setForeground(COULEUR_PRIMAIRE);

        JLabel lblAdresse = new JLabel(parking.getAdresseParking());
        lblAdresse.setFont(POLICE_ADRESSE);
        lblAdresse.setForeground(COULEUR_SECONDAIRE);

        JLabel lblPlaces = new JLabel(
                parking.getPlacesDisponibles() + "/" +
                parking.getNombrePlaces() + " places"
        );
        lblPlaces.setFont(POLICE_DETAIL);

        JLabel lblHauteur = new JLabel(
                "Hauteur max : " + parking.getHauteurParking() + " m"
        );
        lblHauteur.setFont(POLICE_DETAIL);

        panel.add(lblNom);
        panel.add(Box.createVerticalStrut(5));
        panel.add(lblAdresse);
        panel.add(Box.createVerticalStrut(5));
        panel.add(lblPlaces);
        panel.add(lblHauteur);

        return panel;
    }


    private JPanel creerPanelBoutons(Parking parking) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(COULEUR_FOND);

        JButton btnStationner = new JButton("Stationner");
        btnStationner.setPreferredSize(DIMENSION_BOUTON);
        btnStationner.addActionListener(e -> selectionnerParking(parking));

        JButton btnSupprimer = new JButton(redimensionner(COEUR_REMPLI));

        btnSupprimer.setBorderPainted(false);
        btnSupprimer.setContentAreaFilled(false);
        btnSupprimer.setFocusPainted(false);
        btnSupprimer.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSupprimer.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnSupprimer.setPreferredSize(new Dimension(32, 32));

        btnSupprimer.addActionListener(e -> supprimerFavori(parking));

        panel.add(btnStationner);
        panel.add(Box.createVerticalStrut(10));
        panel.add(btnSupprimer);

        return panel;
    }
    
    private ImageIcon redimensionner(ImageIcon icon) {
        Image img = icon.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH);
        return new ImageIcon(img);
    }



    private void supprimerFavori(Parking parking) {
        int choix = JOptionPane.showConfirmDialog(
                this,
                "Retirer ce parking de vos favoris ?",
                "Confirmation",
                JOptionPane.YES_NO_OPTION
        );

        if (choix == JOptionPane.YES_OPTION) {
            FavoriDAO.getInstance()
                     .supprimerFavori(idUsager, parking.getIdParking());
            chargerParkingsFavoris();
            afficherParkingsFavoris();
        }
    }

    private void selectionnerParking(Parking parking) {
        new Page_Garer_Parking(emailUtilisateur, parking);

        dispose();
    }


}

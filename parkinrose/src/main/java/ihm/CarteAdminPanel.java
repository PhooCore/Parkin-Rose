package ihm;

import javax.swing.*;
import modele.Parking;
import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;

/**
 * PANEL ADMINISTRATEUR DE LA CARTE INTERACTIVE
 * ============================================
 * Sous-classe spécialisée de CartePanel pour les administrateurs.
 * 
 * FONCTIONNALITÉS AJOUTÉES :
 * - Ajout de nouveaux parkings sur la carte
 * - Modification des parkings existants
 * - Suppression de parkings
 * - Modes de fonctionnement spécifiques
 * 
 * DESIGN PATTERN : Héritage avec surcharge des comportements utilisateur
 */
public class CarteAdminPanel extends CartePanel {
    
    // ========== ÉNUMÉRATION DES MODES ==========
    /**
     * Modes de fonctionnement du panel admin
     * - NAVIGATION : Mode par défaut (déplacement, zoom)
     * - AJOUT : Permet d'ajouter un parking par clic sur carte
     * - MODIFICATION : Permet de modifier un parking existant
     */
    public enum ModeCarte {
        NAVIGATION,
        AJOUT,
        MODIFICATION
    }
    
    // ========== VARIABLES D'INSTANCE ==========
    private ModeCarte mode = ModeCarte.NAVIGATION;  // Mode actuel
    private JLabel compteurLabel;                   // Label pour afficher le nombre de parkings
    private Point dragStart = null;                 // Point de départ pour le déplacement
    
    /**
     * CONSTRUCTEUR
     * @param imageUrl URL de l'image de la carte
     * @param emailAdmin Email de l'administrateur
     */
    public CarteAdminPanel(java.net.URL imageUrl, String emailAdmin) throws java.io.IOException {
        super(imageUrl, emailAdmin);  // Appelle le constructeur parent
        configurerInteractionsAdmin(); // Configure les interactions spécifiques admin
    }
    
    /**
     * CONFIGURE LES INTERACTIONS SPÉCIFIQUES AU MODE ADMIN
     * Remplace complètement les interactions du parent
     */
    private void configurerInteractionsAdmin() {
        // NETTOYAGE DES LISTENERS EXISTANTS (hérités du parent)
        for (MouseListener listener : getMouseListeners()) removeMouseListener(listener);
        for (MouseMotionListener listener : getMouseMotionListeners()) removeMouseMotionListener(listener);
        for (MouseWheelListener listener : getMouseWheelListeners()) removeMouseWheelListener(listener);
        
        // ========== GESTION DES CLICS ==========
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    // DÉBUT DU DÉPLACEMENT
                    dragStart = e.getPoint();
                    setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                    
                    // RECHERCHE DU PARKING CLIQUE
                    Parking parkingClique = getParkingAtScreenPoint(e.getPoint());
                    
                    if (parkingClique != null) {
                        // CLIC SUR UN PARKING EXISTANT
                        if (mode == ModeCarte.MODIFICATION) {
                            modifierParking(parkingClique);     // Mode modification
                        } else {
                            selectionnerParking(parkingClique); // Mode navigation
                        }
                    } else if (mode == ModeCarte.AJOUT) {
                        // CLIC SUR LA CARTE (pas de parking) EN MODE AJOUT
                        ajouterParking(e.getX(), e.getY());
                    }
                }
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    dragStart = null;
                    // RESTAURE LE CURSEUR SELON LE MODE
                    if (mode == ModeCarte.AJOUT) {
                        setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR)); // Curseur croix
                    } else if (mode == ModeCarte.MODIFICATION) {
                        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));     // Curseur main
                    } else {
                        setCursor(Cursor.getDefaultCursor());                          // Curseur normal
                    }
                }
            }
        });
        
        // ========== GESTION DU DÉPLACEMENT ==========
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (dragStart != null) {
                    // CALCUL DU DÉPLACEMENT
                    int dx = e.getX() - dragStart.x;
                    int dy = e.getY() - dragStart.y;
                    
                    // MISE À JOUR DE LA TRANSLATION (compensée par le zoom)
                    translateX += dx / zoom;
                    translateY += dy / zoom;
                    
                    dragStart = e.getPoint();
                    repaint();
                }
            }
            
            @Override
            public void mouseMoved(MouseEvent e) {
                // GESTION DU CURSEUR ET DES INFOBULLES
                Parking parkingSurvol = getParkingAtScreenPoint(e.getPoint());
                if (parkingSurvol != null) {
                    // SURVOL D'UN PARKING
                    if (mode == ModeCarte.MODIFICATION) {
                        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    } else {
                        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    }
                    String info = parkingSurvol.getLibelleParking() + 
                                 " - " + parkingSurvol.getPlacesDisponibles() + "/" + 
                                 parkingSurvol.getNombrePlaces() + " places";
                    setToolTipText(info);
                } else if (mode == ModeCarte.AJOUT) {
                    // MODE AJOUT (pas de parking sous la souris)
                    setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                } else {
                    // MODE NAVIGATION
                    setCursor(Cursor.getDefaultCursor());
                    setToolTipText(null);
                }
            }
        });
        
        // ========== GESTION DU ZOOM (identique au parent) ==========
        addMouseWheelListener(e -> {
            Point mousePoint = e.getPoint();
            double oldZoom = zoom;
            
            if (e.getPreciseWheelRotation() < 0) {
                zoom = Math.min(5.0, zoom * 1.2); // Zoom in (max 5x)
            } else {
                zoom = Math.max(0.5, zoom * 0.8); // Zoom out (min 0.5x)
            }
            
            // Ajustement pour zoomer vers la souris
            double zoomFactor = zoom / oldZoom;
            translateX = mousePoint.x / zoom - (mousePoint.x / oldZoom - translateX) * zoomFactor;
            translateY = mousePoint.y / zoom - (mousePoint.y / oldZoom - translateY) * zoomFactor;
            
            repaint();
        });
    }
    
    // ========== Surcharge des méthodes parent ==========
    
    /**
     * SURCHARGE : EMPÊCHE LE STATIONNEMENT
     * L'admin ne peut pas se garer, seulement sélectionner
     */
    @Override
    protected void onParkingClicked(Parking parking) {
        selectionnerParking(parking); // Remplace la fonctionnalité utilisateur
    }
    
    /**
     * SURCHARGE : NE FAIT RIEN
     * L'admin ne peut pas ouvrir la page de stationnement
     */
    @Override
    protected void ouvrirPageStationnement(Parking parking) {
        // Méthode vide - désactive la fonctionnalité
    }
    
    // ========== GESTION DU COMPTEUR ==========
    
    /**
     * ATTACHE UN LABEL POUR AFFICHER LE NOMBRE DE PARKINGS
     * @param label JLabel à mettre à jour
     */
    public void setCompteurLabel(JLabel label) {
        this.compteurLabel = label;
        updateCompteur();
    }
    
    /**
     * MET À JOUR LE COMPTEUR DE PARKINGS
     */
    private void updateCompteur() {
        if (compteurLabel != null) {
            compteurLabel.setText("Parkings chargés: " + getParkings().size());
        }
    }
    
    /**
     * SURCHARGE : MET À JOUR LE COMPTEUR LORS DU RAFRAÎCHISSEMENT
     */
    @Override
    public void rafraichirParkings() {
        super.rafraichirParkings();
        updateCompteur();
        repaint();
    }
    
    // ========== FONCTIONNALITÉS ADMIN ==========
    
    /**
     * AJOUTE UN NOUVEAU PARKING À LA POSITION CLIQUÉE
     * @param screenX Coordonnée X écran
     * @param screenY Coordonnée Y écran
     */
    private void ajouterParking(int screenX, int screenY) {
        // CONVERSION COORDONNÉES ÉCRAN -> IMAGE
        Point imagePoint = screenToImageCoordinates(new Point(screenX, screenY));
        
        // CALCUL POSITION RELATIVE (0.0 à 1.0)
        float relativeX = (float)imagePoint.x / getOriginalImageWidth();
        float relativeY = (float)imagePoint.y / getOriginalImageHeight();
        
        // VÉRIFICATION : CLIC DANS L'IMAGE ?
        if (relativeX < 0 || relativeX > 1 || relativeY < 0 || relativeY > 1) {
            JOptionPane.showMessageDialog(this,
                "Veuillez cliquer sur la carte",
                "Position invalide",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // ========== FORMULAIRE D'AJOUT ==========
        JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        
        // CHAMPS DU FORMULAIRE
        JTextField txtLibelle = new JTextField();
        JTextField txtAdresse = new JTextField();
        JTextField txtPlaces = new JTextField("100");
        JTextField txtHauteur = new JTextField("2.00");
        JCheckBox chkTarifSoiree = new JCheckBox();
        JCheckBox chkMoto = new JCheckBox();
        JTextField txtPlacesMoto = new JTextField("0");
        JCheckBox chkRelais = new JCheckBox();
        
        // CHAMP ID (auto-généré mais modifiable)
        JTextField txtId = new JTextField();
        txtId.setEditable(true);
        txtId.setBackground(new Color(240, 240, 240));
        
        // LISTENER POUR GÉNÉRATION AUTOMATIQUE D'ID
        txtLibelle.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateIdFromLibelle(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateIdFromLibelle(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateIdFromLibelle(); }
            
            private void updateIdFromLibelle() {
                String libelle = txtLibelle.getText().trim();
                if (!libelle.isEmpty()) {
                    // GÉNÉRATION DE L'ID À PARTIR DU LIBELLÉ
                    String suggestedId = libelle.toUpperCase()
                        .replaceAll(" ", "_")
                        .replaceAll("[^A-Z0-9_]", "")
                        .replaceAll("__+", "_");
                    
                    // AJOUT DU PRÉFIXE PARK_
                    if (!suggestedId.startsWith("PARK_")) {
                        suggestedId = "PARK_" + suggestedId;
                    }
                    
                    txtId.setText(suggestedId);
                }
            }
        });
        
        // AJOUT DES CHAMPS AU PANEL
        formPanel.add(new JLabel("Nom/Libellé:")); formPanel.add(txtLibelle);
        formPanel.add(new JLabel("ID Parking (auto-généré):")); formPanel.add(txtId);
        formPanel.add(new JLabel("Adresse:")); formPanel.add(txtAdresse);
        formPanel.add(new JLabel("Nombre de places:")); formPanel.add(txtPlaces);
        formPanel.add(new JLabel("Hauteur maximale (m):")); formPanel.add(txtHauteur);
        formPanel.add(new JLabel("Tarif soirée:")); formPanel.add(chkTarifSoiree);
        formPanel.add(new JLabel("Parking moto:")); formPanel.add(chkMoto);
        formPanel.add(new JLabel("Places moto:")); formPanel.add(txtPlacesMoto);
        formPanel.add(new JLabel("Est un relais:")); formPanel.add(chkRelais);
        
        // AFFICHAGE DE LA BOÎTE DE DIALOGUE
        int result = JOptionPane.showConfirmDialog(this, formPanel, 
            "Ajouter un nouveau parking à la position sélectionnée", JOptionPane.OK_CANCEL_OPTION);
        
        if (result == JOptionPane.OK_OPTION) {
            try {
                // RÉCUPÉRATION DES DONNÉES
                String libelle = txtLibelle.getText().trim();
                String id = txtId.getText().trim();
                String adresse = txtAdresse.getText().trim();
                int places = Integer.parseInt(txtPlaces.getText());
                double hauteur = Double.parseDouble(txtHauteur.getText());
                boolean tarifSoiree = chkTarifSoiree.isSelected();
                boolean hasMoto = chkMoto.isSelected();
                int placesMoto = hasMoto ? Integer.parseInt(txtPlacesMoto.getText()) : 0;
                boolean estRelais = chkRelais.isSelected();
                
                // GÉNÉRATION D'ID SI VIDE
                if (id.isEmpty()) {
                    id = generateParkingIdFromName(libelle);
                }
                
                // VÉRIFICATION DE L'UNICITÉ DE L'ID
                if (modele.dao.ParkingDAO.idParkingExiste(id)) {
                    JOptionPane.showMessageDialog(this,
                        "L'ID " + id + " existe déjà. Veuillez en choisir un autre.\n" +
                        "Suggestion: " + generateUniqueParkingId(id),
                        "ID dupliqué", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // CRÉATION DU PARKING
                modele.Parking nouveauParking = new modele.Parking(id, libelle, adresse, 
                    places, places, hauteur, tarifSoiree, hasMoto, placesMoto, placesMoto,
                    estRelais, relativeX, relativeY);
                
                // ENREGISTREMENT EN BASE
                if (modele.dao.ParkingDAO.ajouterParking(nouveauParking)) {
                    rafraichirParkings();
                    
                    JOptionPane.showMessageDialog(this, 
                        "Parking ajouté avec succès!\n" +
                        "Nom: " + libelle + "\n" +
                        "ID: " + id, "Succès", 
                        JOptionPane.INFORMATION_MESSAGE);
                    
                    // RETOUR AU MODE NAVIGATION
                    mode = ModeCarte.NAVIGATION;
                    setCursor(Cursor.getDefaultCursor());
                }
                
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, 
                    "Veuillez entrer des valeurs numériques valides", 
                    "Erreur de format", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            // ANNULATION : RETOUR AU MODE NAVIGATION
            mode = ModeCarte.NAVIGATION;
            setCursor(Cursor.getDefaultCursor());
        }
    }
    
    /**
     * GÉNÈRE UN ID DE PARKING À PARTIR DU NOM
     * @param libelle Nom du parking
     * @return ID généré (ex: "PARK_NOM_PARKING")
     */
    private String generateParkingIdFromName(String libelle) {
        if (libelle == null || libelle.isEmpty()) {
            return modele.dao.ParkingDAO.genererNouvelIdParking();
        }
        
        String id = libelle.toUpperCase()
            .replaceAll(" ", "_")
            .replaceAll("[^A-Z0-9_]", "")
            .replaceAll("__+", "_")
            .trim();
        
        // LIMITATION DE LONGUEUR
        if (id.length() > 50) {
            id = id.substring(0, 50);
        }
        
        // AJOUT DU PRÉFIXE
        if (!id.startsWith("PARK_")) {
            id = "PARK_" + id;
        }
        
        return id;
    }
    
    /**
     * GÉNÈRE UN ID UNIQUE EN CAS DE CONFLIT
     * @param baseId ID de base
     * @return ID unique avec suffixe numérique
     */
    private String generateUniqueParkingId(String baseId) {
        for (int i = 1; i <= 100; i++) {
            String candidate = baseId + "_" + i;
            if (!modele.dao.ParkingDAO.idParkingExiste(candidate)) {
                return candidate;
            }
        }
        return modele.dao.ParkingDAO.genererNouvelIdParking();
    }
    
    /**
     * SÉLECTIONNE ET AFFICHE LES INFORMATIONS D'UN PARKING
     * @param parking Parking à afficher
     */
    private void selectionnerParking(Parking parking) {
        setParkingSelectionne(parking);
        repaint();
        
        // FORMATAGE DES INFORMATIONS
        DecimalFormat df = new DecimalFormat("#.##");
        String info = String.format(
            "<html><div style='width:300px;'><h3>%s</h3>" +
            "<b>ID:</b> %s<br><b>Adresse:</b> %s<br>" +
            "<b>Capacité:</b> %d places (%d disponibles)<br>" +
            "<b>Hauteur max:</b> %s m<br><b>Tarif soirée:</b> %s<br>" +
            "<b>Parking moto:</b> %s<br><b>Est un relais:</b> %s",
            parking.getLibelleParking(),
            parking.getIdParking(),
            parking.getAdresseParking(),
            parking.getNombrePlaces(),
            parking.getPlacesDisponibles(),
            df.format(parking.getHauteurParking()),
            parking.hasTarifSoiree() ? "Oui" : "Non",
            parking.hasMoto() ? "Oui" : "Non",
            parking.isEstRelais() ? "Oui" : "Non"
        );
        
        // INFOS SUPPLÉMENTAIRES POUR PARKING MOTO
        if (parking.hasMoto()) {
            info += String.format("<br><b>Places moto:</b> %d (%d disponibles)",
                parking.getPlacesMoto(),
                parking.getPlacesMotoDisponibles());
        }
        
        // POSITION SUR LA CARTE
        if (parking.getPositionX() != null) {
            info += String.format("<br><b>Position:</b> (%.2f, %.2f)",
                parking.getPositionX(), parking.getPositionY());
        }
        
        info += "</div></html>";
        
        // AFFICHAGE
        JOptionPane.showMessageDialog(this, info, "Information Parking", 
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * MODIFIE UN PARKING EXISTANT
     * @param parking Parking à modifier
     */
    private void modifierParking(Parking parking) {
        // FORMULAIRE DE MODIFICATION
        JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        
        // CHAMPS PRÉ-REMPLIS AVEC LES DONNÉES EXISTANTES
        JTextField txtLibelle = new JTextField(parking.getLibelleParking());
        JTextField txtAdresse = new JTextField(parking.getAdresseParking());
        JTextField txtPlaces = new JTextField(String.valueOf(parking.getNombrePlaces()));
        JTextField txtPlacesDispo = new JTextField(String.valueOf(parking.getPlacesDisponibles()));
        JTextField txtHauteur = new JTextField(String.valueOf(parking.getHauteurParking()));
        JCheckBox chkTarifSoiree = new JCheckBox("", parking.hasTarifSoiree());
        JCheckBox chkMoto = new JCheckBox("", parking.hasMoto());
        JTextField txtPlacesMoto = new JTextField(String.valueOf(parking.getPlacesMoto()));
        JTextField txtPlacesMotoDispo = new JTextField(String.valueOf(parking.getPlacesMotoDisponibles()));
        JCheckBox chkRelais = new JCheckBox("", parking.isEstRelais());
        
        // AJOUT DES CHAMPS
        formPanel.add(new JLabel("Nom/Libellé:")); formPanel.add(txtLibelle);
        formPanel.add(new JLabel("Adresse:")); formPanel.add(txtAdresse);
        formPanel.add(new JLabel("Nombre de places:")); formPanel.add(txtPlaces);
        formPanel.add(new JLabel("Places disponibles:")); formPanel.add(txtPlacesDispo);
        formPanel.add(new JLabel("Hauteur maximale (m):")); formPanel.add(txtHauteur);
        formPanel.add(new JLabel("Tarif soirée:")); formPanel.add(chkTarifSoiree);
        formPanel.add(new JLabel("Parking moto:")); formPanel.add(chkMoto);
        formPanel.add(new JLabel("Places moto:")); formPanel.add(txtPlacesMoto);
        formPanel.add(new JLabel("Places moto dispo:")); formPanel.add(txtPlacesMotoDispo);
        formPanel.add(new JLabel("Est un relais:")); formPanel.add(chkRelais);
        
        // AFFICHAGE DE LA BOÎTE DE DIALOGUE
        int result = JOptionPane.showConfirmDialog(this, formPanel, 
            "Modifier le parking: " + parking.getLibelleParking(), 
            JOptionPane.OK_CANCEL_OPTION);
        
        if (result == JOptionPane.OK_OPTION) {
            try {
                // MISE À JOUR DES DONNÉES
                parking.setLibelleParking(txtLibelle.getText().trim());
                parking.setAdresseParking(txtAdresse.getText().trim());
                parking.setNombrePlaces(Integer.parseInt(txtPlaces.getText()));
                parking.setPlacesDisponibles(Integer.parseInt(txtPlacesDispo.getText()));
                parking.setHauteurParking(Double.parseDouble(txtHauteur.getText()));
                parking.setTarifSoiree(chkTarifSoiree.isSelected());
                parking.setHasMoto(chkMoto.isSelected());
                parking.setEstRelais(chkRelais.isSelected());
                
                // GESTION SPÉCIFIQUE PARKING MOTO
                if (parking.hasMoto()) {
                    parking.setPlacesMoto(Integer.parseInt(txtPlacesMoto.getText()));
                    parking.setPlacesMotoDisponibles(Integer.parseInt(txtPlacesMotoDispo.getText()));
                }
                
                // ENREGISTREMENT EN BASE
                if (modele.dao.ParkingDAO.mettreAJourParking(parking)) {
                    rafraichirParkings();
                    JOptionPane.showMessageDialog(this, 
                        "Parking modifié avec succès!", "Succès", 
                        JOptionPane.INFORMATION_MESSAGE);
                }
                
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, 
                    "Veuillez entrer des valeurs numériques valides", 
                    "Erreur de format", JOptionPane.ERROR_MESSAGE);
            }
        }
        
        // RETOUR AU MODE NAVIGATION
        mode = ModeCarte.NAVIGATION;
        setCursor(Cursor.getDefaultCursor());
    }
    
    /**
     * SUPPRIME LE PARKING SÉLECTIONNÉ
     * Vérifie d'abord qu'aucun stationnement n'est en cours
     */
    public void supprimerParkingSelectionne() {
        Parking parkingSelectionne = getParkingSelectionne();
        if (parkingSelectionne != null) {
            // CONFIRMATION DE SUPPRESSION
            int confirmation = JOptionPane.showConfirmDialog(this,
                "<html><b>Confirmation de suppression</b><br><br>" +
                "Voulez-vous vraiment supprimer le parking :<br>" +
                "<b>" + parkingSelectionne.getLibelleParking() + "</b><br>" +
                "ID: " + parkingSelectionne.getIdParking() + "<br>" +
                "Adresse: " + parkingSelectionne.getAdresseParking() + "<br><br>" +
                "<font color='red'><b>Cette action est irréversible !</b></font></html>",
                "Suppression de parking",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
            
            if (confirmation == JOptionPane.YES_OPTION) {
                try {
                    // VÉRIFICATION : STATIONNEMENTS EN COURS ?
                    if (modele.dao.StationnementDAO.hasStationnementEnCours(parkingSelectionne.getIdParking())) {
                        JOptionPane.showMessageDialog(this,
                            "<html><b>Impossible de supprimer ce parking</b><br><br>" +
                            "Il y a encore des stationnements en cours dans ce parking.<br>" +
                            "Veuillez attendre que tous les stationnements soient terminés.</html>",
                            "Suppression impossible",
                            JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    
                    // SUPPRESSION EN BASE
                    if (modele.dao.ParkingDAO.supprimerParking(parkingSelectionne.getIdParking())) {
                        rafraichirParkings();
                        setParkingSelectionne(null);
                        
                        JOptionPane.showMessageDialog(this,
                            "<html><b>Parking supprimé avec succès !</b><br><br>" +
                            "Le parking a été supprimé de la base de données.</html>",
                            "Suppression réussie",
                            JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this,
                        "<html><b>Erreur lors de la suppression :</b><br>" + e.getMessage() + "</html>",
                        "Erreur",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this,
                "<html><b>Aucun parking sélectionné</b><br><br>" +
                "Veuillez d'abord sélectionner un parking en cliquant dessus.</html>",
                "Sélection requise",
                JOptionPane.WARNING_MESSAGE);
        }
    }
    
    // ========== GESTION DES MODES ==========
    
    /**
     * ACTIVE LE MODE AJOUT
     * Change le curseur et affiche un message d'instruction
     */
    public void modeAjout() {
        mode = ModeCarte.AJOUT;
        setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        JOptionPane.showMessageDialog(this,
            "Mode Ajout activé. Cliquez sur la carte pour ajouter un nouveau parking.\n" +
            "La position du clic servira à positionner le marqueur.",
            "Mode Ajout", JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * ACTIVE LE MODE MODIFICATION
     * Change le curseur et affiche un message d'instruction
     */
    public void modeModification() {
        mode = ModeCarte.MODIFICATION;
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        JOptionPane.showMessageDialog(this,
            "Mode Modification activé. Cliquez sur un parking pour le modifier.",
            "Mode Modification", JOptionPane.INFORMATION_MESSAGE);
    }
    
    // ========== SURCHARGE DU DESSIN ==========
    
    /**
     * SURCHARGE : AJOUTE L'AFFICHAGE DU MODE ACTUEL
     * Dessine un panneau d'information en haut à gauche
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        Graphics2D g2d = (Graphics2D) g;
        
        // ========== PANEL D'INFORMATION MODE ==========
        g2d.setColor(new Color(255, 255, 255, 200));
        g2d.fillRoundRect(5, 5, 220, 50, 10, 10);
        g2d.setColor(new Color(0, 0, 0, 100));
        g2d.drawRoundRect(5, 5, 220, 50, 10, 10);
        
        // TEXTE DU MODE (avec couleur adaptée)
        String modeText = "Mode: " + mode.toString();
        Color modeColor;
        switch (mode) {
            case AJOUT:
                modeColor = new Color(40, 167, 69); // VERT pour ajout
                break;
            case MODIFICATION:
                modeColor = new Color(255, 193, 7); // ORANGE pour modification
                break;
            default:
                modeColor = new Color(0, 123, 255); // BLEU pour navigation
        }
        
        g2d.setColor(modeColor);
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.drawString(modeText, 10, 25);
        
        // COMPTEUR DE PARKINGS
        g2d.setColor(Color.DARK_GRAY);
        g2d.setFont(new Font("Arial", Font.PLAIN, 10));
        g2d.drawString("Parkings: " + getParkings().size(), 10, 40);
        
        // INSTRUCTIONS CONTEXTUELLES
        String instruction = "";
        switch (mode) {
            case AJOUT:
                instruction = "Cliquez pour ajouter";
                break;
            case MODIFICATION:
                instruction = "Cliquez sur un parking";
                break;
            default:
                instruction = "Clic-glisser: déplacer";
                break;
        }
        g2d.drawString(instruction, 10, 55);
    }
}
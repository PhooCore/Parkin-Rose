

package ihm;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import dao.TarifDAO;
import modèle.Tarif;
import java.util.List;

public class Page_Garer_Voirie extends JFrame {
    private static final long serialVersionUID = 1L;
    private JPanel contentPanel;
    private JTextField txtNom, txtPrenom, txtEmail, txtPlaque;
    private JComboBox<String> comboZone, comboHeures, comboMinutes;
    private JLabel lblCout;
    private JRadioButton radioVoiture, radioMoto, radioCamion;
    private ButtonGroup groupeTypeVehicule;
    private List<Tarif> listeTarifs;

    public Page_Garer_Voirie() {
        initializeUI();
        initializeData();
        initializeEventListeners();
    }
    
    private void initializeUI() {
        this.setTitle("Stationnement en Voirie");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(800, 600);
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        
        contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        this.setContentPane(contentPanel);
        
        // Titre
        JLabel lblTitre = new JLabel("Préparer un Stationnement en voirie", SwingConstants.CENTER);
        lblTitre.setFont(new Font("Arial", Font.BOLD, 18));
        contentPanel.add(lblTitre, BorderLayout.NORTH);
        
        // Panel principal
        JPanel panelPrincipal = new JPanel();
        panelPrincipal.setLayout(new BoxLayout(panelPrincipal, BoxLayout.Y_AXIS));
        
        // Informations personnelles
        JPanel panelInfos = new JPanel();
        panelInfos.setLayout(new GridLayout(3, 2, 10, 10));
        panelInfos.setBorder(BorderFactory.createTitledBorder("Vos informations"));
        
        panelInfos.add(new JLabel("Nom:"));
        txtNom = new JTextField();
        panelInfos.add(txtNom);
        
        panelInfos.add(new JLabel("Prénom:"));
        txtPrenom = new JTextField();
        panelInfos.add(txtPrenom);
        
        panelInfos.add(new JLabel("Email:"));
        txtEmail = new JTextField();
        panelInfos.add(txtEmail);
        
        panelPrincipal.add(panelInfos);
        panelPrincipal.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // Véhicule
        JPanel panelVehicule = new JPanel();
        panelVehicule.setLayout(new BorderLayout());
        panelVehicule.setBorder(BorderFactory.createTitledBorder("Véhicule"));
        
        JPanel panelType = new JPanel(new FlowLayout(FlowLayout.LEFT));
        groupeTypeVehicule = new ButtonGroup();
        
        radioVoiture = new JRadioButton("Voiture", true);
        radioMoto = new JRadioButton("Moto");
        radioCamion = new JRadioButton("Camion");
        
        groupeTypeVehicule.add(radioVoiture);
        groupeTypeVehicule.add(radioMoto);
        groupeTypeVehicule.add(radioCamion);
        
        panelType.add(radioVoiture);
        panelType.add(radioMoto);
        panelType.add(radioCamion);
        
        panelVehicule.add(panelType, BorderLayout.NORTH);
        
        JPanel panelPlaque = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelPlaque.add(new JLabel("Plaque:"));
        txtPlaque = new JTextField(10);
        panelPlaque.add(txtPlaque);
        
        panelVehicule.add(panelPlaque, BorderLayout.SOUTH);
        
        panelPrincipal.add(panelVehicule);
        panelPrincipal.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // Zone et durée
        JPanel panelStationnement = new JPanel();
        panelStationnement.setLayout(new GridLayout(3, 2, 10, 10));
        panelStationnement.setBorder(BorderFactory.createTitledBorder("Stationnement"));
        
        panelStationnement.add(new JLabel("Zone:"));
        comboZone = new JComboBox<>();
        panelStationnement.add(comboZone);
        
        panelStationnement.add(new JLabel("Durée:"));
        JPanel panelDuree = new JPanel(new FlowLayout(FlowLayout.LEFT));
        String[] heures = {"0", "1", "2", "3", "4", "5", "6", "7", "8"};
        String[] minutes = {"0", "15", "30", "45"};
        
        comboHeures = new JComboBox<>(heures);
        comboMinutes = new JComboBox<>(minutes);
        
        panelDuree.add(comboHeures);
        panelDuree.add(new JLabel("h"));
        panelDuree.add(comboMinutes);
        panelDuree.add(new JLabel("min"));
        panelStationnement.add(panelDuree);
        
        panelStationnement.add(new JLabel("Coût:"));
        lblCout = new JLabel("0.00 €");
        lblCout.setFont(new Font("Arial", Font.BOLD, 14));
        panelStationnement.add(lblCout);
        
        panelPrincipal.add(panelStationnement);
        
        contentPanel.add(panelPrincipal, BorderLayout.CENTER);
        
        // Boutons
        JPanel panelBoutons = new JPanel(new FlowLayout());
        
        JButton btnAnnuler = new JButton("Annuler");
        JButton btnValider = new JButton("Valider");
        
        panelBoutons.add(btnAnnuler);
        panelBoutons.add(btnValider);
        
        contentPanel.add(panelBoutons, BorderLayout.SOUTH);
    }
    

    private void initializeData() {
        // Charger les tarifs depuis la base de données
        listeTarifs = TarifDAO.TouslesTarifs();
        
        // ComboBox avec les tarifs
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        for (Tarif tarif : listeTarifs) {
            model.addElement(tarif.getAffichage());
        }
        comboZone.setModel(model);
    }
    
    private void initializeEventListeners() {
        // Calcul du coût
        ItemListener calculateurCout = new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                calculerCout();
            }
        };
        
        comboZone.addItemListener(calculateurCout);
        comboHeures.addItemListener(calculateurCout);
        comboMinutes.addItemListener(calculateurCout);
        
        // Bouton Annuler
        JButton btnAnnuler = (JButton) ((JPanel) contentPanel.getComponent(2)).getComponent(0);
        btnAnnuler.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Page_Test pageTest = new Page_Test("", "", "");
                pageTest.setVisible(true);
                dispose();
            }
        });
        
        // Bouton Valider
        JButton btnValider = (JButton) ((JPanel) contentPanel.getComponent(2)).getComponent(1);
        btnValider.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (validerFormulaire()) {
                    afficherConfirmation();
                }
            }
        });
    }
    
    private void calculerCout() {
        try {
            int heures = Integer.parseInt(comboHeures.getSelectedItem().toString());
            int minutes = Integer.parseInt(comboMinutes.getSelectedItem().toString());
            int dureeTotaleMinutes = (heures * 60) + minutes;
            

            int index = comboZone.getSelectedIndex();
            if (index >= 0 && index < listeTarifs.size()) {
                Tarif tarif = listeTarifs.get(index);
                double cout = tarif.calculerCout(dureeTotaleMinutes);
                lblCout.setText(String.format("%.2f €", cout));
            }
            
        } catch (Exception e) {
            lblCout.setText("0.00 €");
        }
    }
    
    private boolean validerFormulaire() {
        if (txtNom.getText().trim().isEmpty() ||
            txtPrenom.getText().trim().isEmpty() ||
            txtEmail.getText().trim().isEmpty() ||
            txtPlaque.getText().trim().isEmpty()) {
            
            JOptionPane.showMessageDialog(this,
                "Veuillez remplir tous les champs",
                "Champs manquants",
                JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        String email = txtEmail.getText().trim();
        if (!email.contains("@") || !email.contains(".")) {
            JOptionPane.showMessageDialog(this,
                "Email invalide",
                "Erreur",
                JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        if (!validerDureeMaximale()) {
            return false;
        }
        
        return true;
    }
    
    private boolean validerDureeMaximale() {
        int heures = Integer.parseInt(comboHeures.getSelectedItem().toString());
        int minutes = Integer.parseInt(comboMinutes.getSelectedItem().toString());
        int dureeTotaleMinutes = (heures * 60) + minutes;
        
        int index = comboZone.getSelectedIndex();
        if (index >= 0 && index < listeTarifs.size()) {
            Tarif tarif = listeTarifs.get(index);
            if (dureeTotaleMinutes > tarif.getDureeMaxMinutes()) {
                JOptionPane.showMessageDialog(this,
                    "Durée maximale dépassée pour " + tarif.getNomZone() + 
                    " (max: " + formatDuree(tarif.getDureeMaxMinutes()) + ")",
                    "Erreur",
                    JOptionPane.WARNING_MESSAGE);
                return false;
            }
        }
        
        return true;
    }
    
    private String formatDuree(int minutes) {
        int heures = minutes / 60;
        int mins = minutes % 60;
        if (mins == 0) {
            return heures + "h";
        } else {
            return heures + "h" + mins + "min";
        }
    }
    
    private void afficherConfirmation() {
        int index = comboZone.getSelectedIndex();
        String nomZone = "";
        if (index >= 0 && index < listeTarifs.size()) {
            nomZone = listeTarifs.get(index).getNomZone();
        }
        
        String message = "Stationnement confirmé :\n\n" +
            "Nom: " + txtNom.getText() + "\n" +
            "Prénom: " + txtPrenom.getText() + "\n" +
            "Email: " + txtEmail.getText() + "\n" +
            "Véhicule: " + getTypeVehicule() + " - " + txtPlaque.getText() + "\n" +
            "Zone: " + nomZone + "\n" +
            "Durée: " + comboHeures.getSelectedItem() + "h" + comboMinutes.getSelectedItem() + "min\n" +
            "Coût: " + lblCout.getText();
        
        JOptionPane.showMessageDialog(this,
            message,
            "Confirmation",
            JOptionPane.INFORMATION_MESSAGE);
        
        Page_Test pageTest = new Page_Test(txtEmail.getText(), txtNom.getText(), txtPrenom.getText());
        pageTest.setVisible(true);
        dispose();
    }
    
    private String getTypeVehicule() {
        if (radioVoiture.isSelected()) return "Voiture";
        if (radioMoto.isSelected()) return "Moto";
        return "Camion";
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Page_Garer_Voirie().setVisible(true);
        });
    }
}
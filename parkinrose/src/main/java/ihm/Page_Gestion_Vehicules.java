package ihm;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import modele.VehiculeUsager;
import modele.dao.VehiculeUsagerDAO;
import modele.dao.UsagerDAO;

public class Page_Gestion_Vehicules extends JFrame {
    private String emailUtilisateur;
    private JList<VehiculeUsager> listVehicules;
    private DefaultListModel<VehiculeUsager> listModel;
    private JButton btnAjouter, btnSupprimer, btnDefinirPrincipal;

    public Page_Gestion_Vehicules(String email) {
        this.emailUtilisateur = email;
        initialisePage();
        chargerVehicules();
    }

    private void initialisePage() {
        setTitle("Mes Véhicules");
        setSize(600, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Titre
        JLabel lblTitre = new JLabel("Mes Véhicules", SwingConstants.CENTER);
        lblTitre.setFont(new Font("Arial", Font.BOLD, 16));
        contentPanel.add(lblTitre, BorderLayout.NORTH);

        // Liste des véhicules
        listModel = new DefaultListModel<>();
        listVehicules = new JList<>(listModel);
        listVehicules.setCellRenderer(new VehiculeRenderer());
        JScrollPane scrollPane = new JScrollPane(listVehicules);
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        // Panel boutons
        JPanel panelBoutons = new JPanel(new FlowLayout());
        btnAjouter = new JButton("Ajouter un véhicule");
        btnSupprimer = new JButton("Supprimer");
        btnDefinirPrincipal = new JButton("Définir comme principal");

        btnAjouter.addActionListener(e -> ajouterVehicule());
        btnSupprimer.addActionListener(e -> supprimerVehicule());
        btnDefinirPrincipal.addActionListener(e -> definirPrincipal());

        panelBoutons.add(btnAjouter);
        panelBoutons.add(btnSupprimer);
        panelBoutons.add(btnDefinirPrincipal);

        contentPanel.add(panelBoutons, BorderLayout.SOUTH);

        setContentPane(contentPanel);
    }

    private void chargerVehicules() {
        listModel.clear();
        modele.Usager usager = UsagerDAO.getUsagerByEmail(emailUtilisateur);
        if (usager != null) {
            List<VehiculeUsager> vehicules = VehiculeUsagerDAO.getVehiculesByUsager(usager.getIdUsager());
            for (VehiculeUsager v : vehicules) {
                listModel.addElement(v);
            }
        }
    }

    private void ajouterVehicule() {
        // Ouvrir une boîte de dialogue pour ajouter un véhicule
        JDialog dialog = new JDialog(this, "Ajouter un véhicule", true);
        dialog.setLayout(new GridLayout(5, 2, 5, 5));
        
        JTextField txtPlaque = new JTextField();
        JComboBox<String> comboType = new JComboBox<>(new String[]{"Voiture", "Moto", "Camion"});
        JTextField txtMarque = new JTextField();
        JTextField txtModele = new JTextField();
        JCheckBox chkPrincipal = new JCheckBox("Véhicule principal");
        
        dialog.add(new JLabel("Plaque d'immatriculation:"));
        dialog.add(txtPlaque);
        dialog.add(new JLabel("Type:"));
        dialog.add(comboType);
        dialog.add(new JLabel("Marque (optionnel):"));
        dialog.add(txtMarque);
        dialog.add(new JLabel("Modèle (optionnel):"));
        dialog.add(txtModele);
        dialog.add(new JLabel(""));
        dialog.add(chkPrincipal);
        
        JButton btnValider = new JButton("Valider");
        btnValider.addActionListener(e -> {
            String plaque = txtPlaque.getText().trim().toUpperCase();
            if (!plaque.matches("[A-Z]{2}-\\d{3}-[A-Z]{2}")) {
                JOptionPane.showMessageDialog(dialog, "Format de plaque invalide (AA-123-AA)", 
                    "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            modele.Usager usager = UsagerDAO.getUsagerByEmail(emailUtilisateur);
            if (usager != null) {
                VehiculeUsager vehicule = new VehiculeUsager(
                    usager.getIdUsager(),
                    plaque,
                    (String) comboType.getSelectedItem()
                );
                vehicule.setMarque(txtMarque.getText().trim());
                vehicule.setModele(txtModele.getText().trim());
                vehicule.setEstPrincipal(chkPrincipal.isSelected());
                
                if (VehiculeUsagerDAO.ajouterVehicule(vehicule)) {
                    chargerVehicules();
                    dialog.dispose();
                }
            }
        });
        
        dialog.add(btnValider);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void supprimerVehicule() {
        VehiculeUsager vehicule = listVehicules.getSelectedValue();
        if (vehicule != null) {
            int choix = JOptionPane.showConfirmDialog(this, 
                "Supprimer le véhicule " + vehicule.getPlaqueImmatriculation() + " ?",
                "Confirmation", JOptionPane.YES_NO_OPTION);
            
            if (choix == JOptionPane.YES_OPTION) {
                if (VehiculeUsagerDAO.supprimerVehicule(vehicule.getIdVehiculeUsager())) {
                    chargerVehicules();
                }
            }
        }
    }

    private void definirPrincipal() {
        VehiculeUsager vehicule = listVehicules.getSelectedValue();
        if (vehicule != null) {
            modele.Usager usager = UsagerDAO.getUsagerByEmail(emailUtilisateur);
            if (usager != null) {
                if (VehiculeUsagerDAO.definirVehiculePrincipal(
                    vehicule.getIdVehiculeUsager(), usager.getIdUsager())) {
                    chargerVehicules();
                }
            }
        }
    }

    // Renderer personnalisé pour la JList
    private class VehiculeRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, 
                                                     int index, boolean isSelected, 
                                                     boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            VehiculeUsager vehicule = (VehiculeUsager) value;
            
            String texte = vehicule.getPlaqueImmatriculation() + " - " + vehicule.getTypeVehicule();
            if (vehicule.getMarque() != null && !vehicule.getMarque().isEmpty()) {
                texte += " " + vehicule.getMarque();
            }
            if (vehicule.getModele() != null && !vehicule.getModele().isEmpty()) {
                texte += " " + vehicule.getModele();
            }
            if (vehicule.isEstPrincipal()) {
                texte += " ★";
                setFont(getFont().deriveFont(Font.BOLD));
            }
            
            setText(texte);
            return this;
        }
    }
}
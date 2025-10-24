

package ihm;

import java.awt.*;
import javax.swing.*;
import dao.UsagerDAO;
import modèle.Usager;

public class Page_Inscription extends JFrame {

    private static final long serialVersionUID = 1L;
    private JTextField textFieldNom;
    private JTextField textFieldPrenom;
    private JTextField textFieldEmail;
    private JPasswordField passwordField;
    private JPasswordField passwordFieldConfirm;

    public Page_Inscription() {
        setTitle("Création de compte");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setLayout(new BorderLayout(0, 0));
        setContentPane(mainPanel);

        JPanel panelRetour = new JPanel();
        panelRetour.setBackground(Color.WHITE);
        FlowLayout flowLayout = (FlowLayout) panelRetour.getLayout();
        flowLayout.setAlignment(FlowLayout.LEFT);
        mainPanel.add(panelRetour, BorderLayout.NORTH);
        
        JButton btnRetour = new JButton("← Retour");
        btnRetour.addActionListener(e -> retourLogin());
        btnRetour.setFont(new Font("Arial", Font.PLAIN, 14));
        btnRetour.setBackground(Color.WHITE);
        btnRetour.setFocusPainted(false);
        panelRetour.add(btnRetour);

        JPanel formPanel = new JPanel();
        formPanel.setBackground(Color.WHITE);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        

        JLabel lblTitre = new JLabel("Créer un compte");
        lblTitre.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblTitre.setFont(new Font("Arial", Font.BOLD, 28));
        formPanel.add(lblTitre);
        
        Component verticalStrut = Box.createRigidArea(new Dimension(0, 30));
        formPanel.add(verticalStrut);
        

        JPanel panelNom = new JPanel();
        panelNom.setBackground(Color.WHITE);
        panelNom.setMaximumSize(new Dimension(500, 50));
        formPanel.add(panelNom);
        panelNom.setLayout(new BoxLayout(panelNom, BoxLayout.X_AXIS));
        
        Component horizontalStrut = Box.createRigidArea(new Dimension(10, 0));
        panelNom.add(horizontalStrut);
        
        JLabel lblNom = new JLabel("Nom");
        lblNom.setPreferredSize(new Dimension(120, 30));
        lblNom.setMinimumSize(new Dimension(120, 30));
        lblNom.setMaximumSize(new Dimension(120, 30));
        lblNom.setFont(new Font("Arial", Font.PLAIN, 16));
        panelNom.add(lblNom);
        
        Component horizontalStrut_1 = Box.createRigidArea(new Dimension(20, 0));
        panelNom.add(horizontalStrut_1);
        
        textFieldNom = new JTextField();
        textFieldNom.setPreferredSize(new Dimension(300, 40));
        textFieldNom.setMinimumSize(new Dimension(300, 40));
        textFieldNom.setMaximumSize(new Dimension(300, 40));
        textFieldNom.setFont(new Font("Arial", Font.PLAIN, 16));
        textFieldNom.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        panelNom.add(textFieldNom);
        
        Component horizontalStrut_2 = Box.createRigidArea(new Dimension(10, 0));
        panelNom.add(horizontalStrut_2);
        
        Component verticalStrut_1 = Box.createRigidArea(new Dimension(0, 15));
        formPanel.add(verticalStrut_1);
        

        JPanel panelPrenom = new JPanel();
        panelPrenom.setBackground(Color.WHITE);
        panelPrenom.setMaximumSize(new Dimension(500, 50));
        formPanel.add(panelPrenom);
        panelPrenom.setLayout(new BoxLayout(panelPrenom, BoxLayout.X_AXIS));
        
        Component horizontalStrut_3 = Box.createRigidArea(new Dimension(10, 0));
        panelPrenom.add(horizontalStrut_3);
        
        JLabel lblPrenom = new JLabel("Prénom");
        lblPrenom.setPreferredSize(new Dimension(120, 30));
        lblPrenom.setMinimumSize(new Dimension(120, 30));
        lblPrenom.setMaximumSize(new Dimension(120, 30));
        lblPrenom.setFont(new Font("Arial", Font.PLAIN, 16));
        panelPrenom.add(lblPrenom);
        
        Component horizontalStrut_4 = Box.createRigidArea(new Dimension(20, 0));
        panelPrenom.add(horizontalStrut_4);
        
        textFieldPrenom = new JTextField();
        textFieldPrenom.setPreferredSize(new Dimension(300, 40));
        textFieldPrenom.setMinimumSize(new Dimension(300, 40));
        textFieldPrenom.setMaximumSize(new Dimension(300, 40));
        textFieldPrenom.setFont(new Font("Arial", Font.PLAIN, 16));
        textFieldPrenom.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        panelPrenom.add(textFieldPrenom);
        
        Component horizontalStrut_5 = Box.createRigidArea(new Dimension(10, 0));
        panelPrenom.add(horizontalStrut_5);
        
        Component verticalStrut_2 = Box.createRigidArea(new Dimension(0, 15));
        formPanel.add(verticalStrut_2);
        

        JPanel panelEmail = new JPanel();
        panelEmail.setBackground(Color.WHITE);
        panelEmail.setMaximumSize(new Dimension(500, 50));
        formPanel.add(panelEmail);
        panelEmail.setLayout(new BoxLayout(panelEmail, BoxLayout.X_AXIS));
        
        Component horizontalStrut_6 = Box.createRigidArea(new Dimension(10, 0));
        panelEmail.add(horizontalStrut_6);
        
        JLabel lblEmail = new JLabel("Email");
        lblEmail.setPreferredSize(new Dimension(120, 30));
        lblEmail.setMinimumSize(new Dimension(120, 30));
        lblEmail.setMaximumSize(new Dimension(120, 30));
        lblEmail.setFont(new Font("Arial", Font.PLAIN, 16));
        panelEmail.add(lblEmail);
        
        Component horizontalStrut_7 = Box.createRigidArea(new Dimension(20, 0));
        panelEmail.add(horizontalStrut_7);
        
        textFieldEmail = new JTextField();
        textFieldEmail.setPreferredSize(new Dimension(300, 40));
        textFieldEmail.setMinimumSize(new Dimension(300, 40));
        textFieldEmail.setMaximumSize(new Dimension(300, 40));
        textFieldEmail.setFont(new Font("Arial", Font.PLAIN, 16));
        textFieldEmail.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        panelEmail.add(textFieldEmail);
        
        Component horizontalStrut_8 = Box.createRigidArea(new Dimension(10, 0));
        panelEmail.add(horizontalStrut_8);
        
        Component verticalStrut_3 = Box.createRigidArea(new Dimension(0, 15));
        formPanel.add(verticalStrut_3);
        

        JPanel panelPassword = new JPanel();
        panelPassword.setBackground(Color.WHITE);
        panelPassword.setMaximumSize(new Dimension(500, 50));
        formPanel.add(panelPassword);
        panelPassword.setLayout(new BoxLayout(panelPassword, BoxLayout.X_AXIS));
        
        Component horizontalStrut_9 = Box.createRigidArea(new Dimension(10, 0));
        panelPassword.add(horizontalStrut_9);
        
        JLabel lblPassword = new JLabel("Mot de passe");
        lblPassword.setPreferredSize(new Dimension(120, 30));
        lblPassword.setMinimumSize(new Dimension(120, 30));
        lblPassword.setMaximumSize(new Dimension(120, 30));
        lblPassword.setFont(new Font("Arial", Font.PLAIN, 16));
        panelPassword.add(lblPassword);
        
        Component horizontalStrut_10 = Box.createRigidArea(new Dimension(20, 0));
        panelPassword.add(horizontalStrut_10);
        
        passwordField = new JPasswordField();
        passwordField.setPreferredSize(new Dimension(300, 40));
        passwordField.setMinimumSize(new Dimension(300, 40));
        passwordField.setMaximumSize(new Dimension(300, 40));
        passwordField.setFont(new Font("Arial", Font.PLAIN, 16));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        panelPassword.add(passwordField);
        
        Component horizontalStrut_11 = Box.createRigidArea(new Dimension(10, 0));
        panelPassword.add(horizontalStrut_11);
        
        Component verticalStrut_4 = Box.createRigidArea(new Dimension(0, 15));
        formPanel.add(verticalStrut_4);
        

        JPanel panelConfirm = new JPanel();
        panelConfirm.setBackground(Color.WHITE);
        panelConfirm.setMaximumSize(new Dimension(500, 50));
        formPanel.add(panelConfirm);
        panelConfirm.setLayout(new BoxLayout(panelConfirm, BoxLayout.X_AXIS));
        
        Component horizontalStrut_12 = Box.createRigidArea(new Dimension(10, 0));
        panelConfirm.add(horizontalStrut_12);
        
        JLabel lblConfirm = new JLabel("Confirmation");
        lblConfirm.setPreferredSize(new Dimension(120, 30));
        lblConfirm.setMinimumSize(new Dimension(120, 30));
        lblConfirm.setMaximumSize(new Dimension(120, 30));
        lblConfirm.setFont(new Font("Arial", Font.PLAIN, 16));
        panelConfirm.add(lblConfirm);
        
        Component horizontalStrut_13 = Box.createRigidArea(new Dimension(20, 0));
        panelConfirm.add(horizontalStrut_13);
        
        passwordFieldConfirm = new JPasswordField();
        passwordFieldConfirm.setPreferredSize(new Dimension(300, 40));
        passwordFieldConfirm.setMinimumSize(new Dimension(300, 40));
        passwordFieldConfirm.setMaximumSize(new Dimension(300, 40));
        passwordFieldConfirm.setFont(new Font("Arial", Font.PLAIN, 16));
        passwordFieldConfirm.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        panelConfirm.add(passwordFieldConfirm);
        
        Component horizontalStrut_14 = Box.createRigidArea(new Dimension(10, 0));
        panelConfirm.add(horizontalStrut_14);
        
        Component verticalStrut_5 = Box.createRigidArea(new Dimension(0, 30));
        formPanel.add(verticalStrut_5);
        

        JButton btnCreerCompte = new JButton("CRÉER MON COMPTE");
        btnCreerCompte.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnCreerCompte.addActionListener(e -> creerCompte());
        btnCreerCompte.setFont(new Font("Arial", Font.BOLD, 18));
        btnCreerCompte.setBackground(new Color(80, 80, 80));
        btnCreerCompte.setForeground(Color.WHITE);
        btnCreerCompte.setFocusPainted(false);
        btnCreerCompte.setBorder(BorderFactory.createEmptyBorder(12, 30, 12, 30));
        formPanel.add(btnCreerCompte);
        
        Component verticalGlue = Box.createVerticalGlue();
        formPanel.add(verticalGlue);
    }

    private void retourLogin() {
        Page_Authentification loginPage = new Page_Authentification();
        loginPage.setVisible(true);
        this.dispose();
    }

    private void creerCompte() {
        // Récupération des valeurs AVEC LES BONS NOMS DE VARIABLES
        String nom = textFieldNom.getText().trim();
        String prenom = textFieldPrenom.getText().trim();
        String email = textFieldEmail.getText().trim();
        
        // Récupération des mots de passe AVEC LES BONS NOMS DE VARIABLES
        String motDePasse = new String(passwordField.getPassword());
        String confirmation = new String(passwordFieldConfirm.getPassword());

        // Validation
        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || motDePasse.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Veuillez remplir tous les champs", 
                "Erreur", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!motDePasse.equals(confirmation)) {
            JOptionPane.showMessageDialog(this, 
                "Les mots de passe ne correspondent pas", 
                "Erreur", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (motDePasse.length() < 4) {
            JOptionPane.showMessageDialog(this, 
                "Le mot de passe doit contenir au moins 4 caractères", 
                "Erreur", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Vérifier si l'email existe déjà
        if (UsagerDAO.emailExisteDeja(email)) {
            JOptionPane.showMessageDialog(this, 
                "Cet email est déjà utilisé", 
                "Erreur", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Créer l'objet Usager
        Usager nouvelUsager = new Usager(nom, prenom, email, motDePasse);

        // Sauvegarder en base
        boolean succes = UsagerDAO.ajouterUsager(nouvelUsager);

        if (succes) {
            JOptionPane.showMessageDialog(this, 
                "Compte créé avec succès !", 
                "Succès", 
                JOptionPane.INFORMATION_MESSAGE);
            
            // Rediriger vers la page de connexion
            Page_Authentification authPage = new Page_Authentification();
            authPage.setVisible(true);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, 
                "Erreur lors de la création du compte", 
                "Erreur", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    Page_Bienvenue frame = new Page_Bienvenue();
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}

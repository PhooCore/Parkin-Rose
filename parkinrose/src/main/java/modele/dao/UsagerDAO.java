package modele.dao;
import modele.Usager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsagerDAO {
    

    /**
     * Vérifie si un email existe déjà dans la base de données
     * Utilisé lors de l'inscription pour éviter les doublons
     * 
     * @param email l'adresse email à vérifier
     * @return true si l'email existe déjà, false sinon
     */
    public static boolean emailExisteDeja(String email) {
        // Requête SQL pour compter les utilisateurs avec cet email
        String sql = "SELECT mail_usager FROM Usager WHERE mail_usager = ?";
        
        try (
            Connection conn = MySQLConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setString(1, email); // 1er ? : email à vérifier
            
            try (ResultSet rs = stmt.executeQuery()) {
                // Si rs.next() retourne true, c'est qu'un enregistrement a été trouvé
                return rs.next();
            }

        } catch (SQLException e) {
            System.err.println("Erreur vérification email: " + e.getMessage());
            return false; // En cas d'erreur, on considère que l'email n'existe pas
        }
    }

    /**
     * Récupère un utilisateur par son adresse email
     * Utilisé pour la connexion et pour récupérer les informations de l'utilisateur connecté
     * 
     * @param email l'adresse email de l'utilisateur recherché
     * @return l'objet Usager correspondant, ou null si non trouvé
     */
    public static Usager getUsagerByEmail(String email) {
        // Requête SQL pour sélectionner un utilisateur par son email
        String sql = "SELECT * FROM Usager WHERE mail_usager = ?";
        
        try (
            Connection conn = MySQLConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setString(1, email); // 1er ? : email de l'utilisateur recherché
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // Création d'un nouvel objet Usager
                    Usager usager = new Usager();
                    
                    // Remplissage de l'objet avec les données de la base
                    usager.setIdUsager(rs.getInt("id_usager"));              // ID auto-généré
                    usager.setNomUsager(rs.getString("nom_usager"));         // Nom de famille
                    usager.setPrenomUsager(rs.getString("prenom_usager"));   // Prénom
                    usager.setMailUsager(rs.getString("mail_usager"));       // Email
                    usager.setMotDePasse(rs.getString("mot_de_passe"));      // Mot de passe
                    usager.setNumeroCarteTisseo(rs.getString("numero_carte_tisseo"));
                    usager.setAdmin(rs.getBoolean("is_admin"));
                    
                    return usager;
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de l'usager: " + e.getMessage());
            e.printStackTrace();
        }
        return null; // Aucun utilisateur trouvé
    }
    /**
     * Modifie le mot de passe d'un utilisateur
     * @param email l'email de l'utilisateur
     * @param nouveauMotDePasse le nouveau mot de passe
     * @return true si la modification a réussi, false sinon
     */
    public static boolean modifierMotDePasse(String email, String nouveauMotDePasse) {
        String sql = "UPDATE Usager SET mot_de_passe = ? WHERE mail_usager = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, nouveauMotDePasse);
            stmt.setString(2, email);
            
            int lignesAffectees = stmt.executeUpdate();
            
            if (lignesAffectees > 0) {
                System.out.println("Mot de passe modifié pour l'utilisateur: " + email);
                return true;
            } else {
                System.out.println("Aucun utilisateur trouvé avec l'email: " + email);
                return false;
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la modification du mot de passe: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }


    /**
     * Récupère tous les utilisateurs
     */
    public static List<Usager> getAllUsagers() {
        List<Usager> usagers = new ArrayList<>();
        String sql = "SELECT * FROM Usager ORDER BY nom_usager, prenom_usager";
        
        try (Connection conn = MySQLConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Usager usager = mapResultSetToUsager(rs);
                usagers.add(usager);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur récupération utilisateurs: " + e.getMessage());
            e.printStackTrace();
        }
        
        return usagers;
    }
    
    /**
     * Vérifie si un email existe déjà
     */
    public static boolean emailExiste(String email) {
        String sql = "SELECT COUNT(*) FROM Usager WHERE mail_usager = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur vérification email: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Ajoute un nouvel utilisateur
     */
    public static boolean ajouterUsager(Usager usager) {
        String sql = "INSERT INTO Usager (nom_usager, prenom_usager, mail_usager, mot_de_passe, is_admin) " +
                    "VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, usager.getNomUsager());
            stmt.setString(2, usager.getPrenomUsager());
            stmt.setString(3, usager.getMailUsager());
            stmt.setString(4, usager.getMotDePasse()); // Devrait être hashé en production
            stmt.setBoolean(5, usager.isAdmin());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur ajout utilisateur: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Modifie un utilisateur existant
     */
    public static boolean modifierUsager(Usager usager) {
        StringBuilder sql = new StringBuilder("UPDATE Usager SET nom_usager = ?, prenom_usager = ?, " +
                                            "mail_usager = ?, is_admin = ?");
        
        // Ajouter le mot de passe seulement s'il a été modifié
        boolean mdpModifie = usager.getMotDePasse() != null && !usager.getMotDePasse().isEmpty();
        if (mdpModifie) {
            sql.append(", mot_de_passe = ?");
        }
        
        sql.append(" WHERE id_usager = ?");
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            stmt.setString(1, usager.getNomUsager());
            stmt.setString(2, usager.getPrenomUsager());
            stmt.setString(3, usager.getMailUsager());
            stmt.setBoolean(4, usager.isAdmin());
            
            int paramIndex = 5;
            if (mdpModifie) {
                stmt.setString(paramIndex++, usager.getMotDePasse());
            }
            
            stmt.setInt(paramIndex, usager.getIdUsager());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur modification utilisateur: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Récupère la carte Tisséo d'un utilisateur
     */
    public static String getCarteTisseoByUsager(int idUsager) {
        String sql = "SELECT numero_carte_tisseo FROM Usager WHERE id_usager = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idUsager);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getString("numero_carte_tisseo");
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur récupération carte Tisséo: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Enregistre la carte Tisséo d'un utilisateur
     */
    public static boolean enregistrerCarteTisseo(int idUsager, String numeroCarte) {
        String sql = "UPDATE Usager SET numero_carte_tisseo = ? WHERE id_usager = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, numeroCarte);
            stmt.setInt(2, idUsager);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur enregistrement carte Tisséo: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Mappe un ResultSet vers un objet Usager
     */
    private static Usager mapResultSetToUsager(ResultSet rs) throws SQLException {
        Usager usager = new Usager();
        usager.setIdUsager(rs.getInt("id_usager"));
        usager.setNomUsager(rs.getString("nom_usager"));
        usager.setPrenomUsager(rs.getString("prenom_usager"));
        usager.setMailUsager(rs.getString("mail_usager"));
        usager.setMotDePasse(rs.getString("mot_de_passe"));
        usager.setNumeroCarteTisseo(rs.getString("numero_carte_tisseo"));
        usager.setAdmin(rs.getBoolean("is_admin"));
        return usager;
    }
    		
    	
}
    

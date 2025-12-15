package modele.dao;

import modele.VehiculeUsager;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VehiculeUsagerDAO {
    
    /**
     * Récupère tous les véhicules d'un utilisateur
     */
    public static List<VehiculeUsager> getVehiculesByUsager(int idUsager) {
        List<VehiculeUsager> vehicules = new ArrayList<>();
        String sql = "SELECT * FROM Vehicule_Usager WHERE id_usager = ? ORDER BY est_principal DESC, date_ajout DESC";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idUsager);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                vehicules.add(mapResultSetToVehicule(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur récupération véhicules: " + e.getMessage());
            e.printStackTrace();
        }
        
        return vehicules;
    }
    
    /**
     * Récupère le véhicule principal d'un utilisateur
     */
    public static VehiculeUsager getVehiculePrincipal(int idUsager) {
        String sql = "SELECT * FROM Vehicule_Usager WHERE id_usager = ? AND est_principal = TRUE LIMIT 1";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idUsager);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToVehicule(rs);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur récupération véhicule principal: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Ajoute un nouveau véhicule pour un utilisateur
     */
    public static boolean ajouterVehicule(VehiculeUsager vehicule) {
        String sql = "INSERT INTO Vehicule_Usager (id_usager, plaque_immatriculation, type_vehicule, marque, modele, est_principal) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
        
        Connection conn = null;
        try {
            conn = MySQLConnection.getConnection();
            conn.setAutoCommit(false);
            
            // Si c'est le véhicule principal, désactiver l'ancien principal
            if (vehicule.isEstPrincipal()) {
                String sqlUpdate = "UPDATE Vehicule_Usager SET est_principal = FALSE WHERE id_usager = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sqlUpdate)) {
                    stmt.setInt(1, vehicule.getIdUsager());
                    stmt.executeUpdate();
                }
            }
            
            // Ajouter le nouveau véhicule
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, vehicule.getIdUsager());
                stmt.setString(2, vehicule.getPlaqueImmatriculation());
                stmt.setString(3, vehicule.getTypeVehicule());
                stmt.setString(4, vehicule.getMarque());
                stmt.setString(5, vehicule.getModele());
                stmt.setBoolean(6, vehicule.isEstPrincipal());
                
                int rows = stmt.executeUpdate();
                conn.commit();
                return rows > 0;
            }
            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Erreur rollback: " + ex.getMessage());
                }
            }
            System.err.println("Erreur ajout véhicule: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Erreur fermeture connexion: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Vérifie si une plaque existe déjà pour un utilisateur
     */
    public static boolean plaqueExistePourUsager(int idUsager, String plaque) {
        String sql = "SELECT COUNT(*) FROM Vehicule_Usager WHERE id_usager = ? AND plaque_immatriculation = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idUsager);
            stmt.setString(2, plaque.toUpperCase());
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur vérification plaque: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Supprime un véhicule
     */
    public static boolean supprimerVehicule(int idVehiculeUsager) {
        String sql = "DELETE FROM Vehicule_Usager WHERE id_vehicule_usager = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idVehiculeUsager);
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur suppression véhicule: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Définit un véhicule comme principal
     */
    public static boolean definirVehiculePrincipal(int idVehiculeUsager, int idUsager) {
        String sql = "UPDATE Vehicule_Usager SET est_principal = CASE " +
                    "WHEN id_vehicule_usager = ? THEN TRUE ELSE FALSE END " +
                    "WHERE id_usager = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idVehiculeUsager);
            stmt.setInt(2, idUsager);
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur définition véhicule principal: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Mappe le ResultSet vers un objet VehiculeUsager
     */
    private static VehiculeUsager mapResultSetToVehicule(ResultSet rs) throws SQLException {
        VehiculeUsager vehicule = new VehiculeUsager();
        vehicule.setIdVehiculeUsager(rs.getInt("id_vehicule_usager"));
        vehicule.setIdUsager(rs.getInt("id_usager"));
        vehicule.setPlaqueImmatriculation(rs.getString("plaque_immatriculation"));
        vehicule.setTypeVehicule(rs.getString("type_vehicule"));
        vehicule.setMarque(rs.getString("marque"));
        vehicule.setModele(rs.getString("modele"));
        vehicule.setDateAjout(rs.getDate("date_ajout").toLocalDate());
        vehicule.setEstPrincipal(rs.getBoolean("est_principal"));
        return vehicule;
    }
}
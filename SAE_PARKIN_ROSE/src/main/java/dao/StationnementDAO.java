



package dao;

import modèle.Stationnement;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StationnementDAO {
    
	//Crée un nouveau stationnement
    public static boolean creerStationnement(Stationnement stationnement) {
        String sql = "INSERT INTO Stationnement (id_usager, type_vehicule, plaque_immatriculation, " +
                    "zone, duree_heures, duree_minutes, cout, date_creation, statut) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, NOW(), ?)";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, stationnement.getIdUsager());
            stmt.setString(2, stationnement.getTypeVehicule());
            stmt.setString(3, stationnement.getPlaqueImmatriculation());
            stmt.setString(4, stationnement.getZone());
            stmt.setInt(5, stationnement.getDureeHeures());
            stmt.setInt(6, stationnement.getDureeMinutes());
            stmt.setDouble(7, stationnement.getCout());
            stmt.setString(8, stationnement.getStatut());
            
            int rowsInserted = stmt.executeUpdate();
            
            if (rowsInserted > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        stationnement.setIdStationnement(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la création du stationnement: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    //Récupère un stationnement par son ID
    public static Stationnement getStationnementById(int idStationnement) {
        String sql = "SELECT * FROM Stationnement WHERE id_stationnement = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idStationnement);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToStationnement(rs);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération du stationnement: " + e.getMessage());
        }
        return null;
    }
    
    //Récupère tous les stationnements d'un usager
    public static List<Stationnement> getStationnementsByUsager(int idUsager) {
        List<Stationnement> stationnements = new ArrayList<>();
        String sql = "SELECT * FROM Stationnement WHERE id_usager = ? ORDER BY date_creation DESC";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idUsager);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    stationnements.add(mapResultSetToStationnement(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des stationnements: " + e.getMessage());
        }
        return stationnements;
    }
    
    //Met à jour le statut d'un stationnement
    public static boolean mettreAJourStatut(int idStationnement, String nouveauStatut) {
        String sql = "UPDATE Stationnement SET statut = ? WHERE id_stationnement = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, nouveauStatut);
            stmt.setInt(2, idStationnement);
            
            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour du statut: " + e.getMessage());
        }
        return false;
    }
    
     //Vérifie si un véhicule a déjà un stationnement actif
    public static boolean vehiculeAStationnementActif(String plaqueImmatriculation) {
        String sql = "SELECT COUNT(*) FROM Stationnement WHERE plaque_immatriculation = ? AND statut IN ('RESERVE', 'ACTIF')";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, plaqueImmatriculation);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la vérification du véhicule: " + e.getMessage());
        }
        return false;
    }

    //Convertit un ResultSet en objet Stationnement
    private static Stationnement mapResultSetToStationnement(ResultSet rs) throws SQLException {
        Stationnement stationnement = new Stationnement();
        stationnement.setIdStationnement(rs.getInt("id_stationnement"));
        stationnement.setIdUsager(rs.getInt("id_usager"));
        stationnement.setTypeVehicule(rs.getString("type_vehicule"));
        stationnement.setPlaqueImmatriculation(rs.getString("plaque_immatriculation"));
        stationnement.setZone(rs.getString("zone"));
        stationnement.setDureeHeures(rs.getInt("duree_heures"));
        stationnement.setDureeMinutes(rs.getInt("duree_minutes"));
        stationnement.setCout(rs.getDouble("cout"));
        stationnement.setDateCreation(rs.getTimestamp("date_creation").toLocalDateTime());
        stationnement.setStatut(rs.getString("statut"));
        return stationnement;
    }
}
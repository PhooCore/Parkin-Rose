package modele.dao;

import modele.Vehicule;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VehiculeDAO {
    
    /**
     * Récupère tous les véhicules d'un utilisateur
     * @param idUsager ID de l'utilisateur
     * @return Liste des véhicules de l'utilisateur
     */
    public static List<Vehicule> getVehiculesByUsager(int idUsager) {
        List<Vehicule> vehicules = new ArrayList<>();
        String sql = "SELECT v.* FROM Vehicule v " +
                    "INNER JOIN Posseder p ON v.id_vehicule = p.id_vehicule " +
                    "WHERE p.id_usager = ? ORDER BY v.plaque_immatriculation";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, idUsager);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Vehicule vehicule = mapResultSetToVehicule(rs);
                vehicules.add(vehicule);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur récupération véhicules par usager: " + e.getMessage());
            e.printStackTrace();
        }
        return vehicules;
    }
    
    /**
     * Récupère un véhicule par son ID
     * @param idVehicule ID du véhicule
     * @return Le véhicule correspondant, ou null si non trouvé
     */
    public static Vehicule getVehiculeById(String idVehicule) {
        String sql = "SELECT * FROM Vehicule WHERE id_vehicule = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, idVehicule);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToVehicule(rs);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur récupération véhicule par ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Récupère un véhicule par sa plaque d'immatriculation
     * @param plaqueImmatriculation La plaque d'immatriculation
     * @return Le véhicule correspondant, ou null si non trouvé
     */
    public static Vehicule getVehiculeByPlaque(String plaqueImmatriculation) {
        String sql = "SELECT * FROM Vehicule WHERE plaque_immatriculation = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, plaqueImmatriculation);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToVehicule(rs);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur récupération véhicule par plaque: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Ajoute un nouveau véhicule et l'associe à un utilisateur
     * @param idUsager ID de l'utilisateur
     * @param vehicule Le véhicule à ajouter
     * @return true si l'ajout a réussi, false sinon
     */
    public static boolean ajouterVehiculeUtilisateur(int idUsager, Vehicule vehicule) {
        Connection conn = null;
        try {
            conn = MySQLConnection.getConnection();
            conn.setAutoCommit(false);
            
            // Générer un ID unique pour le véhicule
            String idVehicule = genererIdVehicule();
            vehicule.setIdVehicule(idVehicule);
            
            // 1. Insérer le véhicule dans la table Vehicule
            String sqlVehicule = "INSERT INTO Vehicule (id_vehicule, plaque_immatriculation) VALUES (?, ?)";
            try (PreparedStatement pstmtVehicule = conn.prepareStatement(sqlVehicule)) {
                pstmtVehicule.setString(1, idVehicule);
                pstmtVehicule.setString(2, vehicule.getPlaqueImmatriculation());
                pstmtVehicule.executeUpdate();
            }
            
            // 2. Créer la relation dans la table Posseder
            String sqlPosseder = "INSERT INTO Posseder (id_usager, id_vehicule) VALUES (?, ?)";
            try (PreparedStatement pstmtPosseder = conn.prepareStatement(sqlPosseder)) {
                pstmtPosseder.setInt(1, idUsager);
                pstmtPosseder.setString(2, idVehicule);
                pstmtPosseder.executeUpdate();
            }
            
            conn.commit();
            System.out.println("Véhicule ajouté avec succès: " + vehicule.getPlaqueImmatriculation());
            return true;
            
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                System.err.println("Erreur rollback: " + ex.getMessage());
            }
            System.err.println("Erreur ajout véhicule: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                System.err.println("Erreur fermeture connexion: " + e.getMessage());
            }
        }
    }
    
    /**
     * Supprime un véhicule et sa relation avec l'utilisateur
     * @param idUsager ID de l'utilisateur
     * @param plaqueImmatriculation Plaque du véhicule à supprimer
     * @return true si la suppression a réussi, false sinon
     */
    public static boolean supprimerVehiculeUtilisateur(int idUsager, String plaqueImmatriculation) {
        Connection conn = null;
        try {
            conn = MySQLConnection.getConnection();
            conn.setAutoCommit(false);
            
            // 1. Récupérer l'ID du véhicule à partir de la plaque
            String idVehicule = getIdVehiculeByPlaque(conn, plaqueImmatriculation);
            if (idVehicule == null) {
                conn.rollback();
                System.err.println("Véhicule non trouvé: " + plaqueImmatriculation);
                return false;
            }
            
            // 2. Vérifier que le véhicule appartient bien à l'utilisateur
            if (!verifierAppartenance(conn, idUsager, idVehicule)) {
                conn.rollback();
                System.err.println("Le véhicule n'appartient pas à cet utilisateur");
                return false;
            }
            
            // 3. Vérifier si le véhicule est utilisé dans des stationnements actifs
            if (estUtiliseDansStationnements(conn, idVehicule)) {
                conn.rollback();
                System.err.println("Le véhicule est utilisé dans des stationnements actifs");
                return false;
            }
            
            // 4. Supprimer la relation dans Posseder
            String sqlSupprimerPosseder = "DELETE FROM Posseder WHERE id_usager = ? AND id_vehicule = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlSupprimerPosseder)) {
                pstmt.setInt(1, idUsager);
                pstmt.setString(2, idVehicule);
                pstmt.executeUpdate();
            }
            
            // 5. Supprimer le véhicule
            String sqlSupprimerVehicule = "DELETE FROM Vehicule WHERE id_vehicule = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlSupprimerVehicule)) {
                pstmt.setString(1, idVehicule);
                int rowsAffected = pstmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    conn.commit();
                    System.out.println("Véhicule supprimé avec succès: " + plaqueImmatriculation);
                    return true;
                } else {
                    conn.rollback();
                    return false;
                }
            }
            
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                System.err.println("Erreur rollback: " + ex.getMessage());
            }
            System.err.println("Erreur suppression véhicule: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                System.err.println("Erreur fermeture connexion: " + e.getMessage());
            }
        }
    }
    
    /**
     * Vérifie si une plaque existe déjà dans la base
     * @param plaqueImmatriculation La plaque à vérifier
     * @return true si la plaque existe, false sinon
     */
    public static boolean plaqueExiste(String plaqueImmatriculation) {
        String sql = "SELECT COUNT(*) FROM Vehicule WHERE plaque_immatriculation = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, plaqueImmatriculation.toUpperCase());
            ResultSet rs = pstmt.executeQuery();
            
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
     * Met à jour les informations d'un véhicule
     * @param vehicule Le véhicule avec les nouvelles informations
     * @return true si la mise à jour a réussi, false sinon
     */
    public static boolean updateVehicule(Vehicule vehicule) {
        String sql = "UPDATE Vehicule SET plaque_immatriculation = ? WHERE id_vehicule = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, vehicule.getPlaqueImmatriculation());
            pstmt.setString(2, vehicule.getIdVehicule());
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur mise à jour véhicule: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Récupère tous les véhicules (pour l'administration)
     * @return Liste de tous les véhicules
     */
    public static List<Vehicule> getAllVehicules() {
        List<Vehicule> vehicules = new ArrayList<>();
        String sql = "SELECT v.*, u.nom_usager, u.prenom_usager FROM Vehicule v " +
                    "LEFT JOIN Posseder p ON v.id_vehicule = p.id_vehicule " +
                    "LEFT JOIN Usager u ON p.id_usager = u.id_usager " +
                    "ORDER BY v.plaque_immatriculation";
        
        try (Connection conn = MySQLConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Vehicule vehicule = mapResultSetToVehicule(rs);
                // Ajouter les informations propriétaire si disponibles
                String proprietaire = rs.getString("nom_usager") + " " + rs.getString("prenom_usager");
                if (proprietaire != null && !proprietaire.trim().isEmpty()) {
                    // Stocker le propriétaire dans l'objet si nécessaire
                    // Vous pourriez ajouter un champ proprietaire dans la classe Vehicule
                }
                vehicules.add(vehicule);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur récupération tous les véhicules: " + e.getMessage());
            e.printStackTrace();
        }
        return vehicules;
    }
    
    /**
     * Récupère les véhicules sans propriétaire (orphelins)
     * @return Liste des véhicules orphelins
     */
    public static List<Vehicule> getVehiculesOrphelins() {
        List<Vehicule> vehicules = new ArrayList<>();
        String sql = "SELECT v.* FROM Vehicule v " +
                    "LEFT JOIN Posseder p ON v.id_vehicule = p.id_vehicule " +
                    "WHERE p.id_usager IS NULL";
        
        try (Connection conn = MySQLConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Vehicule vehicule = mapResultSetToVehicule(rs);
                vehicules.add(vehicule);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur récupération véhicules orphelins: " + e.getMessage());
            e.printStackTrace();
        }
        return vehicules;
    }
    
    /**
     * Compte le nombre de véhicules d'un utilisateur
     * @param idUsager ID de l'utilisateur
     * @return Nombre de véhicules
     */
    public static int countVehiculesByUsager(int idUsager) {
        String sql = "SELECT COUNT(*) FROM Posseder WHERE id_usager = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, idUsager);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur comptage véhicules: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }
    
    // Méthodes privées utilitaires
    
    private static Vehicule mapResultSetToVehicule(ResultSet rs) throws SQLException {
        Vehicule vehicule = new Vehicule();
        vehicule.setIdVehicule(rs.getString("id_vehicule"));
        vehicule.setPlaqueImmatriculation(rs.getString("plaque_immatriculation"));
        // Le type de véhicule n'est pas stocké dans la base, on le déduit
        vehicule.setTypeVehicule(vehicule.determinerTypeVehicule(vehicule.getPlaqueImmatriculation()));
        return vehicule;
    }
    
    private static String genererIdVehicule() {
        return "VEH_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }
    
    private static String getIdVehiculeByPlaque(Connection conn, String plaque) throws SQLException {
        String sql = "SELECT id_vehicule FROM Vehicule WHERE plaque_immatriculation = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, plaque);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("id_vehicule");
            }
        }
        return null;
    }
    
    private static boolean verifierAppartenance(Connection conn, int idUsager, String idVehicule) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Posseder WHERE id_usager = ? AND id_vehicule = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idUsager);
            pstmt.setString(2, idVehicule);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }
    
    private static boolean estUtiliseDansStationnements(Connection conn, String idVehicule) throws SQLException {
        // Cette méthode vérifie si le véhicule est référencé dans des stationnements
        // Note: Dans votre schéma, les stationnements stockent la plaque, pas l'ID du véhicule
        // Donc on vérifie via la plaque
        
        // D'abord récupérer la plaque
        String plaque = getPlaqueById(conn, idVehicule);
        if (plaque == null) return false;
        
        // Vérifier dans les stationnements actifs
        String sql = "SELECT COUNT(*) FROM Stationnement WHERE plaque_immatriculation = ? AND statut = 'ACTIF'";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, plaque);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }
    
    private static String getPlaqueById(Connection conn, String idVehicule) throws SQLException {
        String sql = "SELECT plaque_immatriculation FROM Vehicule WHERE id_vehicule = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, idVehicule);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("plaque_immatriculation");
            }
        }
        return null;
    }
    
    /**
     * Nettoie les véhicules orphelins (sans propriétaire)
     * @return Nombre de véhicules supprimés
     */
    public static int nettoyerVehiculesOrphelins() {
        List<Vehicule> vehiculesOrphelins = getVehiculesOrphelins();
        int count = 0;
        
        for (Vehicule vehicule : vehiculesOrphelins) {
            // Vérifier si le véhicule n'est pas utilisé dans des stationnements
            if (!vehiculeUtilise(vehicule.getPlaqueImmatriculation())) {
                String sql = "DELETE FROM Vehicule WHERE id_vehicule = ?";
                
                try (Connection conn = MySQLConnection.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    
                    pstmt.setString(1, vehicule.getIdVehicule());
                    int rowsAffected = pstmt.executeUpdate();
                    
                    if (rowsAffected > 0) {
                        count++;
                        System.out.println("Véhicule orphelin supprimé: " + vehicule.getPlaqueImmatriculation());
                    }
                    
                } catch (SQLException e) {
                    System.err.println("Erreur suppression véhicule orphelin: " + e.getMessage());
                }
            }
        }
        
        return count;
    }
    
    private static boolean vehiculeUtilise(String plaque) {
        String sql = "SELECT COUNT(*) FROM Stationnement WHERE plaque_immatriculation = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, plaque);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur vérification utilisation véhicule: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * Recherche des véhicules par plaque (pour l'administration)
     * @param recherche Terme de recherche
     * @return Liste des véhicules correspondants
     */
    public static List<Vehicule> rechercherVehicules(String recherche) {
        List<Vehicule> vehicules = new ArrayList<>();
        String sql = "SELECT v.*, u.nom_usager, u.prenom_usager FROM Vehicule v " +
                    "LEFT JOIN Posseder p ON v.id_vehicule = p.id_vehicule " +
                    "LEFT JOIN Usager u ON p.id_usager = u.id_usager " +
                    "WHERE v.plaque_immatriculation LIKE ? " +
                    "ORDER BY v.plaque_immatriculation";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, "%" + recherche + "%");
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Vehicule vehicule = mapResultSetToVehicule(rs);
                vehicules.add(vehicule);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur recherche véhicules: " + e.getMessage());
            e.printStackTrace();
        }
        return vehicules;
    }
}
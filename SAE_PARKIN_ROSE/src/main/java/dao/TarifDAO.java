

package dao;

import modèle.Tarif;
import java.sql.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class TarifDAO {
    
    public static List<Tarif> TouslesTarifs() {
        List<Tarif> tarifs = new ArrayList<>();
        String sql = "SELECT id_tarification, tarif_par_heure, duree_max FROM Tarification";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                String idTarification = rs.getString("id_tarification");
                double tarifParHeure = rs.getDouble("tarif_par_heure");
                Time dureeMaxTime = rs.getTime("duree_max");
                LocalTime dureeMax = dureeMaxTime.toLocalTime();
                
                Tarif tarif = new Tarif(idTarification, tarifParHeure, dureeMax);
                tarifs.add(tarif);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des tarifs: " + e.getMessage());
            e.printStackTrace();
        }
        
        return tarifs;
    }
}
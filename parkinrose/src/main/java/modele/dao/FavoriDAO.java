package modele.dao;

import modele.Favori;
import modele.Parking;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FavoriDAO extends DaoModele<Favori> {

    private static FavoriDAO instance;

    private FavoriDAO() {}

    public static FavoriDAO getInstance() {
        if (instance == null) {
            instance = new FavoriDAO();
        }
        return instance;
    }

    @Override
    protected Favori creerInstance(ResultSet rs) throws SQLException {
        return new Favori(
            rs.getInt("id_usager"),
            rs.getString("id_parking")
        );
    }


    @Override
    public void create(Favori favori) throws SQLException {
        String sql = "INSERT INTO Favori (id_usager, id_parking) VALUES (?, ?)";

        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, favori.getIdUsager());
            stmt.setString(2, favori.getIdParking());
            stmt.executeUpdate();
        }
    }

    @Override
    public void delete(Favori favori) throws SQLException {
        String sql = "DELETE FROM Favori WHERE id_usager = ? AND id_parking = ?";

        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, favori.getIdUsager());
            stmt.setString(2, favori.getIdParking());
            stmt.executeUpdate();
        }
    }

    @Override
    public void update(Favori obj) throws SQLException {
    	//inutile en sah
        throw new UnsupportedOperationException("update non supporté pour Favori");
    }

    @Override
    public Favori findById(String... id) throws SQLException {
        if (id.length < 2) return null;

        String sql = "SELECT * FROM Favori WHERE id_usager = ? AND id_parking = ?";

        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, Integer.parseInt(id[0]));
            stmt.setString(2, id[1]);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return creerInstance(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<Favori> findAll() throws SQLException {
        String sql = "SELECT * FROM Favoris";
        List<Favori> liste = new ArrayList<>();

        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                liste.add(creerInstance(rs));
            }
        }
        return liste;
    }


    /* ================= Méthodes métier ================= */

    /**
     * Ajouter un parking en favori
     */
    
    public boolean ajouterFavori(int idUsager, String idParking) {
        try {
            create(new Favori(idUsager, idParking));
            return true;
        } catch (SQLException e) {
            System.err.println("Erreur ajout favori: " + e.getMessage());
            return false;
        }
    }

    /**
     * Supprimer un favori
     */
    
    public boolean supprimerFavori(int idUsager, String idParking) {
        try {
            delete(new Favori(idUsager, idParking));
            return true;
        } catch (SQLException e) {
            System.err.println("Erreur suppression favori: " + e.getMessage());
            return false;
        }
    }

    /*
     * Vérifier si un parking est déjà en favori
     */
    
    
    public boolean estFavori(int idUsager, String idParking) throws SQLException {
        return findById(String.valueOf(idUsager), idParking) != null;
    }

    /**
    * Récupérer les parkings favoris d'un utilisateur
    */
    
    
    public List<String> getFavorisUtilisateur(int idUsager) throws SQLException {
        String sql = "SELECT id_parking FROM Favori WHERE id_usager = ?";
        List<String> favoris = new ArrayList<>();

        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUsager);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    favoris.add(rs.getString("id_parking"));
                }
            }
        }
        return favoris;
    }
    
    public List<Parking> getParkingsFavoris(int idUsager) throws SQLException {
        List<Parking> parkings = new ArrayList<>();
        List<String> ids = getFavorisUtilisateur(idUsager);

        ParkingDAO parkingDAO = ParkingDAO.getInstance();

        for (String idParking : ids) {
            Parking p = parkingDAO.findById(idParking);
            if (p != null) {
                parkings.add(p);
            }
        }
        return parkings;
    }

}

package modele.dao;

import modele.dao.requetes.Requete;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public abstract class DaoModele<T> implements Dao<T> {
    
    protected abstract T creerInstance(ResultSet curseur) throws SQLException;
    
    protected List<T> select(PreparedStatement prSt) throws SQLException {
        List<T> resultat = new ArrayList<>();
        ResultSet rs = prSt.executeQuery();
        while (rs.next()) {
            T instance = creerInstance(rs);
            resultat.add(instance);
        }
        rs.close();
        prSt.close();
        return resultat;
    }
    
    protected int miseAJour(Requete<T> req, T donnee) throws SQLException {
        Connection conn = MySQLConnection.getConnection();
        PreparedStatement prSt = conn.prepareStatement(req.requete());
        req.parametres(prSt, donnee);
        int rowsAffected = prSt.executeUpdate();
        prSt.close();
        return rowsAffected;
    }
    
    protected List<T> find(Requete<T> req, String... id) throws SQLException {
        Connection conn = MySQLConnection.getConnection();
        PreparedStatement prSt = conn.prepareStatement(req.requete());
        req.parametres(prSt, id);
        return select(prSt);
    }
    
    protected T findById(Requete<T> req, String... id) throws SQLException {
        List<T> resultats = find(req, id);
        if (resultats.isEmpty()) {
            return null;
        }
        return resultats.get(0);
    }
    
    protected int miseAJourReturnId(Requete<T> req, T donnee) throws SQLException {
        Connection conn = MySQLConnection.getConnection();
        PreparedStatement prSt = conn.prepareStatement(req.requete(), Statement.RETURN_GENERATED_KEYS);
        req.parametres(prSt, donnee);
        int rowsAffected = prSt.executeUpdate();
        
        if (donnee instanceof modele.Feedback) {
            ResultSet rs = prSt.getGeneratedKeys();
            if (rs.next()) {
                modele.Feedback feedback = (modele.Feedback) donnee;
                feedback.setIdFeedback(rs.getInt(1));
            }
            rs.close();
        }
        
        prSt.close();
        return rowsAffected;
    }
}
package modele.test;

import org.junit.Test;

import modele.Paiement;

import static org.junit.Assert.*;
import java.time.LocalDateTime;

public class PaiementTest {
    
    @Test
    public void testConstructeurComplet() {
        Paiement paiement = new Paiement(
            "DUPONT Jean", 
            "1234567812345678", 
            "123", 
            25.50, 
            1
        );
        
        assertEquals("DUPONT Jean", paiement.getNomCarte());
        assertEquals("1234567812345678", paiement.getNumeroCarte());
        assertEquals("123", paiement.getCodeSecretCarte());
        assertEquals(25.50, paiement.getMontant(), 0.001);
        assertEquals(1, paiement.getIdUsager());
        assertEquals("ABO_SIMPLE", paiement.getIdAbonnement());
        assertNotNull(paiement.getIdPaiement());
        assertTrue(paiement.getIdPaiement().startsWith("PAY_"));
        assertNotNull(paiement.getDatePaiement());
        assertEquals("CARTE", paiement.getMethodePaiement());
        assertEquals("REUSSI", paiement.getStatut());
    }
    
    @Test
    public void testConstructeurVide() {
        Paiement paiement = new Paiement();
        assertNotNull(paiement);
    }
    
    @Test
    public void testSetters() {
        Paiement paiement = new Paiement();
        LocalDateTime now = LocalDateTime.now();
        
        paiement.setIdPaiement("PAY_TEST");
        paiement.setNomCarte("MARTIN Marie");
        paiement.setNumeroCarte("8765432187654321");
        paiement.setCodeSecretCarte("456");
        paiement.setIdAbonnement("ABO_ANNUEL");
        paiement.setMontant(135.00);
        paiement.setIdUsager(2);
        paiement.setDatePaiement(now);
        paiement.setMethodePaiement("PAYPAL");
        paiement.setStatut("EN_ATTENTE");
        
        assertEquals("PAY_TEST", paiement.getIdPaiement());
        assertEquals("MARTIN Marie", paiement.getNomCarte());
        assertEquals("8765432187654321", paiement.getNumeroCarte());
        assertEquals("456", paiement.getCodeSecretCarte());
        assertEquals("ABO_ANNUEL", paiement.getIdAbonnement());
        assertEquals(135.00, paiement.getMontant(), 0.001);
        assertEquals(2, paiement.getIdUsager());
        assertEquals(now, paiement.getDatePaiement());
        assertEquals("PAYPAL", paiement.getMethodePaiement());
        assertEquals("EN_ATTENTE", paiement.getStatut());
    }
    
    @Test
    public void testGenerationIdPaiement() {
        Paiement paiement1 = new Paiement("Test1", "1111", "111", 10.0, 1);
        
        try {
            Thread.sleep(10); 
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        Paiement paiement2 = new Paiement("Test2", "2222", "222", 20.0, 2);
        
        assertNotNull(paiement1.getIdPaiement());
        assertNotNull(paiement2.getIdPaiement());
        assertTrue("L'ID du premier paiement doit commencer par PAY_", 
                   paiement1.getIdPaiement().startsWith("PAY_"));
        assertTrue("L'ID du deuxième paiement doit commencer par PAY_", 
                   paiement2.getIdPaiement().startsWith("PAY_"));
        
        assertNotEquals("Les IDs de paiement doivent être différents car ils sont générés à des moments différents", 
                       paiement1.getIdPaiement(), paiement2.getIdPaiement());
    }
}
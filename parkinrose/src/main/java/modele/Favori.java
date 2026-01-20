package modele;

public class Favori {
    private int idUsager;
    private String idParking;

    public Favori(int idUsager, String idParking) {
        this.idUsager = idUsager;
        this.idParking = idParking;
    }

	public int getIdUsager() {
		return idUsager;
	}

	public void setIdUsager(int idUsager) {
		this.idUsager = idUsager;
	}

	public String getIdParking() {
		return idParking;
	}

	public void setIdParking(String idParking) {
		this.idParking = idParking;
	}
    
    

}

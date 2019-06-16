package basex;

import java.util.List;

import cat.iam.ad.uf3.IPais;

public class Pais implements IPais {

	private String nom;
	private long longitudFronteres;
	private List<String> grupsEtnics;
	
	public Pais(){
	}

	@Override
	public String getNom() {
		return nom;
	}

	@Override
	public void setNom(String nom) {
		this.nom = nom;
	}

	@Override
	public long getLongitudFronteres() {
		return longitudFronteres;
	}

	@Override
	public void setLongitudFronteres(long longitud) {
		this.longitudFronteres = longitud;
	}

	@Override
	public List<String> getGrupsEtnics() {
		return grupsEtnics;
	}

	@Override
	public void setGrupsEtnics(List<String> grups) {
		this.grupsEtnics = grups;
	}

	@Override
	public String creaDescripcio() {
		String etnies = "";
		for (String etnia : grupsEtnics) {
			etnies = etnies + "\t" + etnia + "\n";
		}
		return nom.replaceAll("[\r\n]+", "") + " - " + longitudFronteres + " km de fronteres\n" + etnies;
	}

}

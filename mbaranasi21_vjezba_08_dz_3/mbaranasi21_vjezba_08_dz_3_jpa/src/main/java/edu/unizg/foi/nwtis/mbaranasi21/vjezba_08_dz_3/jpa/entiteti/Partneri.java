package edu.unizg.foi.nwtis.mbaranasi21.vjezba_08_dz_3.jpa.entiteti;

import java.io.Serializable;
import jakarta.persistence.*;
import java.util.List;


/**
 * The persistent class for the PARTNERI database table.
 * 
 */
@Entity
@NamedQuery(name="Partneri.findAll", query="SELECT p FROM Partneri p")
public class Partneri implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	private int id;

	private String adminkod;

	private String adresa;

	private double gpsduzina;

	private double gpssirina;

	private int mreznavrata;

	private int mreznavratakraj;

	private String naziv;

	private String sigurnosnikod;

	private String vrstakuhinje;

	//bi-directional many-to-one association to Obracuni
	@OneToMany(mappedBy="partneri")
	private List<Obracuni> obracunis;

	public Partneri() {
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getAdminkod() {
		return this.adminkod;
	}

	public void setAdminkod(String adminkod) {
		this.adminkod = adminkod;
	}

	public String getAdresa() {
		return this.adresa;
	}

	public void setAdresa(String adresa) {
		this.adresa = adresa;
	}

	public double getGpsduzina() {
		return this.gpsduzina;
	}

	public void setGpsduzina(double gpsduzina) {
		this.gpsduzina = gpsduzina;
	}

	public double getGpssirina() {
		return this.gpssirina;
	}

	public void setGpssirina(double gpssirina) {
		this.gpssirina = gpssirina;
	}

	public int getMreznavrata() {
		return this.mreznavrata;
	}

	public void setMreznavrata(int mreznavrata) {
		this.mreznavrata = mreznavrata;
	}

	public int getMreznavratakraj() {
		return this.mreznavratakraj;
	}

	public void setMreznavratakraj(int mreznavratakraj) {
		this.mreznavratakraj = mreznavratakraj;
	}

	public String getNaziv() {
		return this.naziv;
	}

	public void setNaziv(String naziv) {
		this.naziv = naziv;
	}

	public String getSigurnosnikod() {
		return this.sigurnosnikod;
	}

	public void setSigurnosnikod(String sigurnosnikod) {
		this.sigurnosnikod = sigurnosnikod;
	}

	public String getVrstakuhinje() {
		return this.vrstakuhinje;
	}

	public void setVrstakuhinje(String vrstakuhinje) {
		this.vrstakuhinje = vrstakuhinje;
	}

	public List<Obracuni> getObracunis() {
		return this.obracunis;
	}

	public void setObracunis(List<Obracuni> obracunis) {
		this.obracunis = obracunis;
	}

	public Obracuni addObracuni(Obracuni obracuni) {
		getObracunis().add(obracuni);
		obracuni.setPartneri(this);

		return obracuni;
	}

	public Obracuni removeObracuni(Obracuni obracuni) {
		getObracunis().remove(obracuni);
		obracuni.setPartneri(null);

		return obracuni;
	}

}
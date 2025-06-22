package edu.unizg.foi.nwtis.mbaranasi21.vjezba_08_dz_3.jpa.entiteti;

import java.io.Serializable;
import jakarta.persistence.*;
import java.sql.Timestamp;


/**
 * The persistent class for the ZAPISI database table.
 * 
 */
@Entity
@NamedQuery(name="Zapisi.findAll", query="SELECT z FROM Zapisi z")
public class Zapisi implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(
		    name="ZAPISI_ID_GENERATOR",
		    sequenceName="ZAPISI_ID",
		    allocationSize=1
		)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="ZAPISI_ID_GENERATOR")
	private int id;

	private String adresaracunala;

	private String ipadresaracunala;

	private String korisnickoime;

	private String opisrada;

	private Timestamp vrijeme;

	public Zapisi() {
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getAdresaracunala() {
		return this.adresaracunala;
	}

	public void setAdresaracunala(String adresaracunala) {
		this.adresaracunala = adresaracunala;
	}

	public String getIpadresaracunala() {
		return this.ipadresaracunala;
	}

	public void setIpadresaracunala(String ipadresaracunala) {
		this.ipadresaracunala = ipadresaracunala;
	}

	public String getKorisnickoime() {
		return this.korisnickoime;
	}

	public void setKorisnickoime(String korisnickoime) {
		this.korisnickoime = korisnickoime;
	}

	public String getOpisrada() {
		return this.opisrada;
	}

	public void setOpisrada(String opisrada) {
		this.opisrada = opisrada;
	}

	public Timestamp getVrijeme() {
		return this.vrijeme;
	}

	public void setVrijeme(Timestamp vrijeme) {
		this.vrijeme = vrijeme;
	}

}
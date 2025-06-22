package edu.unizg.foi.nwtis.mbaranasi21.vjezba_08_dz_3.jf;

import java.io.Serializable;
import edu.unizg.foi.nwtis.podaci.Korisnik;
import edu.unizg.foi.nwtis.mbaranasi21.vjezba_08_dz_3.jpa.pomocnici.KorisniciFacade;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * Backing bean za prikaz detalja korisnika
 * 
 * @author mbaranasi21
 */
@RequestScoped
@Named("korisnikDetalji")
public class KorisnikDetalji implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Inject
    private KorisniciFacade korisniciFacade;
    
    private String korisnickoIme;
    private Korisnik korisnik;
    private String poruka;
    private String porukaKlasa;

    /**
     * Učitava korisnika iz baze podataka na temelju korisničkog imena
     */
    public void ucitajKorisnika() {
        if (korisnickoIme != null && !korisnickoIme.trim().isEmpty()) {
            try {
                var korisnikEntitet = korisniciFacade.find(korisnickoIme.trim());
                if (korisnikEntitet != null) {
                    korisnik = korisniciFacade.pretvori(korisnikEntitet);
                    poruka = "Korisnik uspješno učitan.";
                    porukaKlasa = "uspjeh";
                } else {
                    korisnik = null;
                    poruka = "Korisnik s tim korisničkim imenom nije pronađen.";
                    porukaKlasa = "greska";
                }
            } catch (Exception e) {
                korisnik = null;
                poruka = "Greška pri dohvaćanju korisnika: " + e.getMessage();
                porukaKlasa = "greska";
            }
        } else {
            korisnik = null;
            poruka = "Unesite korisničko ime.";
            porukaKlasa = "greska";
        }
    }

    public String getKorisnickoIme() {
        return korisnickoIme;
    }

    public void setKorisnickoIme(String korisnickoIme) {
        this.korisnickoIme = korisnickoIme;
    }

    public Korisnik getKorisnik() {
        return korisnik;
    }

    public void setKorisnik(Korisnik korisnik) {
        this.korisnik = korisnik;
    }

    public String getPoruka() {
        return poruka;
    }

    public void setPoruka(String poruka) {
        this.poruka = poruka;
    }

    public String getPorukaKlasa() {
        return porukaKlasa;
    }

    public void setPorukaKlasa(String porukaKlasa) {
        this.porukaKlasa = porukaKlasa;
    }
}
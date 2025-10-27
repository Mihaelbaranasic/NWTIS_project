package edu.unizg.foi.nwtis.mbaranasi21.vjezba_08_dz_3.jf;

import java.io.Serializable;
import edu.unizg.foi.nwtis.podaci.Korisnik;
import edu.unizg.foi.nwtis.mbaranasi21.vjezba_08_dz_3.jpa.pomocnici.KorisniciFacade;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.transaction.Transactional;

/**
 * Backing bean za dodavanje novog korisnika
 * 
 * @author mbaranasi21
 */
@RequestScoped
@Named("noviKorisnik")
public class NoviKorisnik implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Inject
    private KorisniciFacade korisniciFacade;
    
    private String korisnik;
    private String lozinka;
    private String ime;
    private String prezime;
    private String email;
    private String poruka;
    private String porukaKlasa;

    /**
     * Dodaje novog korisnika u bazu podataka
     * 
     * @return navigacija (null za ostanak na istoj stranici)
     */
    @Transactional
    public String dodajKorisnika() {
        try {
            Korisnik noviKorisnik = new Korisnik(korisnik, lozinka, prezime, ime, email);
            
            boolean uspjeh = korisniciFacade.dodaj(noviKorisnik);
            
            if (uspjeh) {
                poruka = "Korisnik je uspješno dodan!";
                porukaKlasa = "uspjeh";
                ocistiPolja();
            } else {
                poruka = "Greška pri dodavanju korisnika. Korisničko ime možda već postoji.";
                porukaKlasa = "greska";
            }
        } catch (Exception e) {
            poruka = "Greška pri dodavanju korisnika: " + e.getMessage();
            porukaKlasa = "greska";
        }
        return null;
    }

    /**
     * Čisti sva polja forme
     */
    private void ocistiPolja() {
        korisnik = null;
        lozinka = null;
        ime = null;
        prezime = null;
        email = null;
    }

    public String getKorisnik() {
        return korisnik;
    }

    public void setKorisnik(String korisnik) {
        this.korisnik = korisnik;
    }

    public String getLozinka() {
        return lozinka;
    }

    public void setLozinka(String lozinka) {
        this.lozinka = lozinka;
    }

    public String getIme() {
        return ime;
    }

    public void setIme(String ime) {
        this.ime = ime;
    }

    public String getPrezime() {
        return prezime;
    }

    public void setPrezime(String prezime) {
        this.prezime = prezime;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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
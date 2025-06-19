package edu.unizg.foi.nwtis.mbaranasi21.vjezba_08_dz_3.jf;

import java.io.Serializable;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import edu.unizg.foi.nwtis.podaci.Korisnik;
import edu.unizg.foi.nwtis.mbaranasi21.vjezba_08_dz_3.ServisPartnerKlijent;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

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
    @RestClient
    private ServisPartnerKlijent servisPartner;
    
    private String korisnik;
    private String lozinka;
    private String ime;
    private String prezime;
    private String email;
    private String poruka;
    private String porukaKlasa;

    /**
     * Dodaje novog korisnika putem REST servisa
     */
    public String dodajKorisnika() {
        try {
            Korisnik noviKorisnik = new Korisnik(korisnik, lozinka, prezime, ime, email);
            
            var odgovor = servisPartner.postKorisnik(noviKorisnik);
            int status = odgovor.getStatus();
            
            if (status == 201) {
                poruka = "Korisnik je uspješno dodan!";
                porukaKlasa = "uspjeh";
                ocistiPolja();
            } else {
                poruka = "Greška pri dodavanju korisnika. Status: " + status;
                porukaKlasa = "greska";
            }
        } catch (Exception e) {
            poruka = "Greška pri komunikaciji s REST servisom: " + e.getMessage();
            porukaKlasa = "greska";
        }
        return null;
    }

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
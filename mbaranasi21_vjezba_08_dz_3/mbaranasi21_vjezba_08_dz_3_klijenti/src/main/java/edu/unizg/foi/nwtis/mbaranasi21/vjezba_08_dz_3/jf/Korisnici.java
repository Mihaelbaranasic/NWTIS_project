package edu.unizg.foi.nwtis.mbaranasi21.vjezba_08_dz_3.jf;

import java.io.Serializable;
import java.util.List;
import edu.unizg.foi.nwtis.podaci.Korisnik;
import edu.unizg.foi.nwtis.mbaranasi21.vjezba_08_dz_3.jpa.pomocnici.KorisniciFacade;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * Backing bean za pregled korisnika s pretraživanjem
 * 
 * @author mbaranasi21
 */
@RequestScoped
@Named("korisnici")
public class Korisnici implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Inject
    private KorisniciFacade korisniciFacade;
    
    private String ime;
    private String prezime;
    private List<Korisnik> korisnici;
    private String poruka;
    private String porukaKlasa;
    private boolean pretrazeno = false;

    /**
     * Pretražuje korisnike po imenu i prezimenu
     */
    public String pretraziKorisnike() {
        try {
            String imePattern = (ime != null && !ime.trim().isEmpty()) ? "%" + ime.trim() + "%" : "%";
            String prezimePattern = (prezime != null && !prezime.trim().isEmpty()) ? "%" + prezime.trim() + "%" : "%";
            
            var korisniciFacade = this.korisniciFacade.findAll(prezimePattern, imePattern);
            korisnici = this.korisniciFacade.pretvori(korisniciFacade);
            
            poruka = "Pronađeno je " + korisnici.size() + " korisnika.";
            porukaKlasa = "uspjeh";
            pretrazeno = true;
        } catch (Exception e) {
            poruka = "Greška pri pretraživanju korisnika: " + e.getMessage();
            porukaKlasa = "greska";
            pretrazeno = true;
        }
        return null;
    }

    /**
     * Prikazuje sve korisnike
     */
    public String prikaziSveKorisnike() {
        try {
            var korisniciEntiteti = korisniciFacade.findAll();
            korisnici = korisniciFacade.pretvori(korisniciEntiteti);
            
            poruka = "Prikazano je " + korisnici.size() + " korisnika.";
            porukaKlasa = "uspjeh";
            pretrazeno = true;
            
            ime = null;
            prezime = null;
        } catch (Exception e) {
            poruka = "Greška pri dohvaćanju korisnika: " + e.getMessage();
            porukaKlasa = "greska";
            pretrazeno = true;
        }
        return null;
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

    public List<Korisnik> getKorisnici() {
        return korisnici;
    }

    public void setKorisnici(List<Korisnik> korisnici) {
        this.korisnici = korisnici;
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

    public boolean isPretrazeno() {
        return pretrazeno;
    }

    public void setPretrazeno(boolean pretrazeno) {
        this.pretrazeno = pretrazeno;
    }
}
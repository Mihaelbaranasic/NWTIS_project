package edu.unizg.foi.nwtis.mbaranasi21.vjezba_08_dz_3.jf;

import java.io.Serializable;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import edu.unizg.foi.nwtis.podaci.Korisnik;
import edu.unizg.foi.nwtis.mbaranasi21.vjezba_08_dz_3.ServisPartnerKlijent;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * Backing bean za prikaz detalja korisnika putem REST servisa
 * 
 * @author mbaranasi21
 */
@RequestScoped
@Named("korisnikDetalji")
public class KorisnikDetalji implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Inject
    @RestClient
    private ServisPartnerKlijent servisPartner;
    
    private String korisnickoIme;
    private Korisnik korisnik;

    /**
     * Učitava korisnika putem REST servisa na temelju korisničkog imena
     */
    public void ucitajKorisnika() {
        if (korisnickoIme != null && !korisnickoIme.trim().isEmpty()) {
            try {
                var odgovor = servisPartner.getKorisnici();
                if (odgovor.getStatus() == 200) {
                    System.out.println("Tražim korisnika: " + korisnickoIme);
                }
            } catch (Exception e) {
                System.err.println("Greška pri dohvaćanju korisnika: " + e.getMessage());
            }
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
}
package edu.unizg.foi.nwtis.mbaranasi21.vjezba_08_dz_3.jf;

import java.io.Serializable;
import java.util.List;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import edu.unizg.foi.nwtis.podaci.Jelovnik;
import edu.unizg.foi.nwtis.mbaranasi21.vjezba_08_dz_3.ServisPartnerKlijent;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.core.GenericType;

/**
 * Backing bean za pregled jelovnika
 *
 * @author mbaranasi21
 */
@RequestScoped
@Named("jelovnik")
public class JelovnikBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    @RestClient
    private ServisPartnerKlijent servisPartner;

    @Inject
    private PrijavaKorisnika prijavaKorisnika;

    private List<Jelovnik> stavkeJelovnika;
    private String poruka;
    private String porukaKlasa;
    private boolean ucitan = false;

    /**
     * Učitava jelovnik putem REST servisa
     */
    public String ucitajJelovnik() {
        try {
            String korisnickoIme = prijavaKorisnika.getKorisnickoIme();
            String lozinka = prijavaKorisnika.getLozinka();

            // DEBUG ispisi
            System.out.println("=== DEBUG JelovnikBean ucitajJelovnik ===");
            System.out.println("Korisnik iz beana: '" + korisnickoIme + "'");
            System.out.println("Lozinka iz beana: '" + (lozinka != null ? "postoji (" + lozinka.length() + " znakova)" : "null") + "'");
            System.out.println("Prijavljen status: " + prijavaKorisnika.isPrijavljen());

            if (korisnickoIme == null || lozinka == null) {
                poruka = "Korisnik nije pravilno prijavljen";
                porukaKlasa = "greska";
                ucitan = true;
                return null;
            }

            System.out.println("Pozivam servisPartner.getJelovnik()...");
            var odgovor = servisPartner.getJelovnik(korisnickoIme, lozinka);
            int status = odgovor.getStatus();

            System.out.println("Primljen HTTP status: " + status);

            if (status == 200) {
                stavkeJelovnika = odgovor.readEntity(new GenericType<List<Jelovnik>>() {});
                poruka = "Jelovnik je uspješno učitan.";
                porukaKlasa = "uspjeh";
                System.out.println("Jelovnik uspješno učitan, broj stavki: " + 
                    (stavkeJelovnika != null ? stavkeJelovnika.size() : "null"));
            } else if (status == 401) {
                poruka = "Neautorizirani pristup - problem s autentifikacijom";
                porukaKlasa = "greska";
                System.out.println("AUTENTIFIKACIJA NEUSPJEŠNA - status 401");
            } else {
                poruka = "Greška pri dohvaćanju jelovnika. Status: " + status;
                porukaKlasa = "greska";
                System.out.println("Neočekivani HTTP status: " + status);
            }
            ucitan = true;
        } catch (Exception e) {
            poruka = "Greška pri komunikaciji s REST servisom: " + e.getMessage();
            porukaKlasa = "greska";
            ucitan = true;
            
            System.out.println("EXCEPTION u JelovnikBean: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public List<Jelovnik> getStavkeJelovnika() {
        return stavkeJelovnika;
    }

    public void setStavkeJelovnika(List<Jelovnik> stavkeJelovnika) {
        this.stavkeJelovnika = stavkeJelovnika;
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

    public boolean isUcitan() {
        return ucitan;
    }

    public void setUcitan(boolean ucitan) {
        this.ucitan = ucitan;
    }
}
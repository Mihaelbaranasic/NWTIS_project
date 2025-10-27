package edu.unizg.foi.nwtis.mbaranasi21.vjezba_08_dz_3.jf;

import java.io.Serializable;
import java.util.List;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import edu.unizg.foi.nwtis.podaci.KartaPica;
import edu.unizg.foi.nwtis.mbaranasi21.vjezba_08_dz_3.ServisPartnerKlijent;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.core.GenericType;

/**
 * Backing bean za pregled karte pića
 * 
 * @author mbaranasi21
 */
@RequestScoped
@Named("kartaPica")
public class KartaPicaBean implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Inject
    @RestClient
    private ServisPartnerKlijent servisPartner;
    
    @Inject
    private PrijavaKorisnika prijavaKorisnika;
    
    private List<KartaPica> stavkeKarte;
    private String poruka;
    private String porukaKlasa;
    private boolean ucitana = false;

    /**
     * Učitava kartu pića putem REST servisa
     */
    public String ucitajKartuPica() {
        try {
            String korisnickoIme = prijavaKorisnika.getKorisnickoIme();
            String lozinka = prijavaKorisnika.getLozinka();
            
            var odgovor = servisPartner.getKartaPica(korisnickoIme, lozinka);
            int status = odgovor.getStatus();
            
            if (status == 200) {
                stavkeKarte = odgovor.readEntity(new GenericType<List<KartaPica>>() {});
                poruka = "Karta pića je uspješno učitana.";
                porukaKlasa = "uspjeh";
            } else if (status == 401) {
                poruka = "Greška pri komunikaciji s REST servisom: Unknown error, status code 401";
                porukaKlasa = "greska";
            } else {
                poruka = "Greška pri dohvaćanju karte pića. Status: " + status;
                porukaKlasa = "greska";
            }
            ucitana = true;
        } catch (Exception e) {
            poruka = "Greška pri komunikaciji s REST servisom: " + e.getMessage();
            porukaKlasa = "greska";
            ucitana = true;
        }
        return null;
    }

    public List<KartaPica> getStavkeKarte() {
        return stavkeKarte;
    }

    public void setStavkeKarte(List<KartaPica> stavkeKarte) {
        this.stavkeKarte = stavkeKarte;
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

    public boolean isUcitana() {
        return ucitana;
    }

    public void setUcitana(boolean ucitana) {
        this.ucitana = ucitana;
    }
}
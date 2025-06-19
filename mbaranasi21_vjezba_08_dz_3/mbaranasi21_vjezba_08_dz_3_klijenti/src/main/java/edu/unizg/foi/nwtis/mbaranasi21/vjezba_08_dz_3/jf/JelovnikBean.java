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
    
    private List<Jelovnik> stavkeJelovnika;
    private String poruka;
    private String porukaKlasa;
    private boolean ucitan = false;

    /**
     * Učitava jelovnik putem REST servisa
     */
    public String ucitajJelovnik() {
        try {
            var odgovor = servisPartner.getJelovnik();
            int status = odgovor.getStatus();
            
            if (status == 200) {
                stavkeJelovnika = odgovor.readEntity(new GenericType<List<Jelovnik>>() {});
                poruka = "Jelovnik je uspješno učitan.";
                porukaKlasa = "uspjeh";
            } else {
                poruka = "Greška pri dohvaćanju jelovnika. Status: " + status;
                porukaKlasa = "greska";
            }
            ucitan = true;
        } catch (Exception e) {
            poruka = "Greška pri komunikaciji s REST servisom: " + e.getMessage();
            porukaKlasa = "greska";
            ucitan = true;
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
package edu.unizg.foi.nwtis.mbaranasi21.vjezba_08_dz_3.jf;

import java.io.Serializable;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import edu.unizg.foi.nwtis.mbaranasi21.vjezba_08_dz_3.ServisPartnerKlijent;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * Backing bean za aktiviranje spavanja Partner servisa
 * Spavanje ne zahtijeva autentifikaciju jer je administrativna funkcija
 * 
 * @author mbaranasi21
 */
@RequestScoped
@Named("spavanje")
public class Spavanje implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Inject
    @RestClient
    private ServisPartnerKlijent servisPartner;
    
    private int vrijeme;
    private String poruka;
    private String porukaKlasa;

    /**
     * Aktivira spavanje Partner servisa
     * Spavanje ne zahtijeva autentifikaciju
     */
    public String aktivirajSpavanje() {
        try {
            var odgovor = servisPartner.getSpavanje(vrijeme);
            int status = odgovor.getStatus();
            
            if (status == 200) {
                poruka = "Spavanje je aktivirano na " + vrijeme + " sekundi.";
                porukaKlasa = "uspjeh";
                vrijeme = 0;
            } else {
                poruka = "Greška pri aktiviranju spavanja. Status: " + status;
                porukaKlasa = "greska";
            }
        } catch (Exception e) {
            poruka = "Greška pri komunikaciji s REST servisom: " + e.getMessage();
            porukaKlasa = "greska";
            e.printStackTrace();
        }
        return null;
    }

    public int getVrijeme() {
        return vrijeme;
    }

    public void setVrijeme(int vrijeme) {
        this.vrijeme = vrijeme;
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
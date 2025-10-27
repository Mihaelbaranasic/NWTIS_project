package edu.unizg.foi.nwtis.mbaranasi21.vjezba_08_dz_3.jf;

import java.io.Serializable;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import edu.unizg.foi.nwtis.mbaranasi21.vjezba_08_dz_3.ServisPartnerKlijent;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * Backing bean za provjeru rada poslužitelja Partner
 * 
 * @author mbaranasi21
 */
@RequestScoped
@Named("provjera")
public class Provjera implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Inject
    @RestClient
    private ServisPartnerKlijent servisPartner;
    
    private String statusPoruka;
    private String statusKlasa;

    /**
     * Provjerava status poslužitelja Partner
     */
    public String provjeriStatus() {
        try {
            var odgovor = servisPartner.headPosluzitelj();
            int status = odgovor.getStatus();
            
            if (status == 200) {
                statusPoruka = "Poslužitelj Partner je dostupan i radi ispravno.";
                statusKlasa = "uspjeh";
            } else {
                statusPoruka = "Poslužitelj Partner nije dostupan. Status: " + status;
                statusKlasa = "greska";
            }
        } catch (Exception e) {
            statusPoruka = "Greška pri komunikaciji s poslužiteljem: " + e.getMessage();
            statusKlasa = "greska";
        }
        return null;
    }

    public String getStatusPoruka() {
        return statusPoruka;
    }

    public void setStatusPoruka(String statusPoruka) {
        this.statusPoruka = statusPoruka;
    }

    public String getStatusKlasa() {
        return statusKlasa;
    }

    public void setStatusKlasa(String statusKlasa) {
        this.statusKlasa = statusKlasa;
    }
}
package edu.unizg.foi.nwtis.mbaranasi21.vjezba_08_dz_3.jf;

import java.io.Serializable;
import edu.unizg.foi.nwtis.podaci.Partner;
import edu.unizg.foi.nwtis.mbaranasi21.vjezba_08_dz_3.jpa.pomocnici.PartneriFacade;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * Backing bean za prikaz detalja partnera
 * 
 * @author mbaranasi21
 */
@RequestScoped
@Named("partnerDetalji")
public class PartnerDetalji implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Inject
    private PartneriFacade partneriFacade;
    
    private int partnerId;
    private Partner partner;

    /**
     * Učitava partnera na temelju ID-a
     */
    public void ucitajPartnera() {
        if (partnerId > 0) {
            try {
                var partnerEntitet = partneriFacade.find(partnerId);
                if (partnerEntitet != null) {
                    partner = partneriFacade.pretvori(partnerEntitet);
                }
            } catch (Exception e) {
                System.err.println("Greška pri dohvaćanju partnera: " + e.getMessage());
            }
        }
    }

    public int getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(int partnerId) {
        this.partnerId = partnerId;
    }

    public Partner getPartner() {
        return partner;
    }

    public void setPartner(Partner partner) {
        this.partner = partner;
    }
}
package edu.unizg.foi.nwtis.mbaranasi21.vjezba_08_dz_3.jf;

import java.io.Serializable;
import java.util.List;
import edu.unizg.foi.nwtis.podaci.Partner;
import edu.unizg.foi.nwtis.mbaranasi21.vjezba_08_dz_3.jpa.pomocnici.PartneriFacade;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * Backing bean za pregled partnera iz baze podataka
 * 
 * @author mbaranasi21
 */
@RequestScoped
@Named("partneri")
public class Partneri implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Inject
    private PartneriFacade partneriFacade;
    
    private List<Partner> partneri;

    @PostConstruct
    public void init() {
        ucitajPartnere();
    }

    /**
     * Dohvaća partnere iz baze podataka putem JPA
     */
    public void ucitajPartnere() {
        try {
            var partneriEntiteti = partneriFacade.findAll();
            partneri = partneriFacade.pretvori(partneriEntiteti);
        } catch (Exception e) {
            System.err.println("Greška pri dohvaćanju partnera iz baze: " + e.getMessage());
        }
    }

    public List<Partner> getPartneri() {
        return partneri;
    }

    public void setPartneri(List<Partner> partneri) {
        this.partneri = partneri;
    }
}
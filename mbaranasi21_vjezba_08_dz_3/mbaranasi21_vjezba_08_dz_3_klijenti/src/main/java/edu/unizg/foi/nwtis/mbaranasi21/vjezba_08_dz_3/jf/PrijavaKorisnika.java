package edu.unizg.foi.nwtis.mbaranasi21.vjezba_08_dz_3.jf;

import java.io.Serializable;
import edu.unizg.foi.nwtis.mbaranasi21.vjezba_08_dz_3.jpa.pomocnici.KorisniciFacade;
import edu.unizg.foi.nwtis.mbaranasi21.vjezba_08_dz_3.jpa.pomocnici.ZapisiFacade;
import edu.unizg.foi.nwtis.mbaranasi21.vjezba_08_dz_3.jpa.entiteti.Zapisi;
import edu.unizg.foi.nwtis.podaci.Korisnik;
import edu.unizg.foi.nwtis.podaci.Partner;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.security.enterprise.SecurityContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import java.sql.Timestamp;

/**
 * Backing bean za prijavu korisnika s autentifikacijom na bazi kontejnera
 * 
 * @author mbaranasi21
 */
@SessionScoped
@Named("prijavaKorisnika")
public class PrijavaKorisnika implements Serializable {
    
    private static final long serialVersionUID = -1826447622277477398L;
    
    private String korisnickoIme;
    private String lozinka;
    private boolean prijavljen = false;
    private String poruka = "";
    private Korisnik korisnik;
    private Partner odabraniPartner;
    private boolean partnerOdabran = false;
    
    @Inject
    private KorisniciFacade korisniciFacade;
    
    @Inject
    private ZapisiFacade zapisiFacade;
    
    @Inject
    private SecurityContext securityContext;

    /**
     * Dohvaća korisničko ime
     * 
     * @return korisničko ime
     */
    public String getKorisnickoIme() {
        return korisnickoIme;
    }
    
    /**
     * Postavlja korisničko ime
     * 
     * @param korisnickoIme korisničko ime
     */
    public void setKorisnickoIme(String korisnickoIme) {
        this.korisnickoIme = korisnickoIme;
    }
    
    /**
     * Dohvaća lozinku
     * 
     * @return lozinka
     */
    public String getLozinka() {
        return lozinka;
    }
    
    /**
     * Postavlja lozinku
     * 
     * @param lozinka lozinka
     */
    public void setLozinka(String lozinka) {
        this.lozinka = lozinka;
    }
    
    /**
     * Dohvaća ime korisnika
     * 
     * @return ime korisnika ili prazan string
     */
    public String getIme() {
        return this.korisnik != null ? this.korisnik.ime() : "";
    }
    
    /**
     * Dohvaća prezime korisnika
     * 
     * @return prezime korisnika ili prazan string
     */
    public String getPrezime() {
        return this.korisnik != null ? this.korisnik.prezime() : "";
    }
    
    /**
     * Dohvaća email korisnika
     * 
     * @return email korisnika ili prazan string
     */
    public String getEmail() {
        return this.korisnik != null ? this.korisnik.email() : "";
    }
    
    /**
     * Provjerava je li korisnik prijavljen
     * 
     * @return true ako je korisnik prijavljen
     */
    public boolean isPrijavljen() {
        if (!this.prijavljen) {
            provjeriPrijavuKorisnika();
        }
        return this.prijavljen;
    }
    
    /**
     * Dohvaća poruku
     * 
     * @return poruka
     */
    public String getPoruka() {
        return poruka;
    }
    
    /**
     * Dohvaća odabrani partner
     * 
     * @return odabrani partner
     */
    public Partner getOdabraniPartner() {
        return odabraniPartner;
    }
    
    /**
     * Postavlja odabrani partner
     * 
     * @param odabraniPartner partner za postavljanje
     */
    public void setOdabraniPartner(Partner odabraniPartner) {
        this.odabraniPartner = odabraniPartner;
    }
    
    /**
     * Provjerava je li partner odabran
     * 
     * @return true ako je partner odabran
     */
    public boolean isPartnerOdabran() {
        return partnerOdabran;
    }
    
    /**
     * Postavlja status odabira partnera
     * 
     * @param partnerOdabran status odabira partnera
     */
    public void setPartnerOdabran(boolean partnerOdabran) {
        this.partnerOdabran = partnerOdabran;
    }

    /**
     * Provjera prijave korisnika nakon inicijalizacije beans
     */
    @PostConstruct
    private void provjeriPrijavuKorisnika() {
        if (this.securityContext.getCallerPrincipal() != null) {
            var korIme = this.securityContext.getCallerPrincipal().getName();
            var korisnikEntitet = this.korisniciFacade.find(korIme);
            if (korisnikEntitet != null) {
                this.korisnik = this.korisniciFacade.pretvori(korisnikEntitet);
                this.prijavljen = true;
                this.korisnickoIme = korIme;
                this.lozinka = this.korisnik.lozinka();
                
                dodajZapisPrijave();
            }
        }
    }

    /**
     * Dodaje zapis o prijavi korisnika u bazu
     */
    @Transactional
    private void dodajZapisPrijave() {
        try {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            HttpServletRequest request = (HttpServletRequest) facesContext.getExternalContext().getRequest();
            
            Zapisi zapis = new Zapisi();
            zapis.setVrijeme(new Timestamp(System.currentTimeMillis()));
            zapis.setKorisnickoime(this.korisnickoIme);
            zapis.setAdresaracunala(request.getRemoteHost());
            zapis.setIpadresaracunala(request.getRemoteAddr());
            zapis.setOpisrada("Nevidljiva prijava korisnika na bazi kontejnera");
            
            zapisiFacade.create(zapis);
        } catch (Exception e) {
            System.err.println("Greška pri dodavanju zapisa prijave: " + e.getMessage());
        }
    }

    /**
     * Dodaje zapis o odjavi korisnika u bazu
     */
    @Transactional
    private void dodajZapisOdjave() {
        try {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            HttpServletRequest request = (HttpServletRequest) facesContext.getExternalContext().getRequest();
            
            Zapisi zapis = new Zapisi();
            zapis.setVrijeme(new Timestamp(System.currentTimeMillis()));
            zapis.setKorisnickoime(this.korisnickoIme);
            zapis.setAdresaracunala(request.getRemoteHost());
            zapis.setIpadresaracunala(request.getRemoteAddr());
            zapis.setOpisrada("Odjava korisnika");
            
            zapisiFacade.create(zapis);
        } catch (Exception e) {
            System.err.println("Greška pri dodavanju zapisa odjave: " + e.getMessage());
        }
    }

    /**
     * Odjavljuje korisnika iz sustava
     * 
     * @return navigacija na početnu stranicu
     */
    @Transactional
    public String odjavaKorisnika() {
        if (this.prijavljen) {
            dodajZapisOdjave();
            
            this.prijavljen = false;
            this.partnerOdabran = false;
            this.odabraniPartner = null;
            this.korisnik = null;
            this.korisnickoIme = null;
            this.lozinka = null;
            this.poruka = "";
            
            FacesContext facesContext = FacesContext.getCurrentInstance();
            facesContext.getExternalContext().invalidateSession();
            return "/index.xhtml?faces-redirect=true";
        }
        return "";
    }
}
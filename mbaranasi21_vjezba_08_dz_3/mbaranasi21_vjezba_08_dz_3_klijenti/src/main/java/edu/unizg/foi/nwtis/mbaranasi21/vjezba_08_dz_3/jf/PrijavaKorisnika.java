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
import java.sql.Timestamp;

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
    RestConfiguration restConfiguration;
    
    @Inject
    KorisniciFacade korisniciFacade;
    
    @Inject
    ZapisiFacade zapisiFacade;
    
    @Inject
    private SecurityContext securityContext;

    public String getKorisnickoIme() {
        return korisnickoIme;
    }
    
    public void setKorisnickoIme(String korisnickoIme) {
        this.korisnickoIme = korisnickoIme;
    }
    
    public String getLozinka() {
        return lozinka;
    }
    
    public void setLozinka(String lozinka) {
        this.lozinka = lozinka;
    }
    
    public String getIme() {
        return this.korisnik != null ? this.korisnik.ime() : "";
    }
    
    public String getPrezime() {
        return this.korisnik != null ? this.korisnik.prezime() : "";
    }
    
    public String getEmail() {
        return this.korisnik != null ? this.korisnik.email() : "";
    }
    
    public boolean isPrijavljen() {
        if (!this.prijavljen) {
            provjeriPrijavuKorisnika();
        }
        return this.prijavljen;
    }
    
    public String getPoruka() {
        return poruka;
    }
    
    public Partner getOdabraniPartner() {
        return odabraniPartner;
    }
    
    public void setOdabraniPartner(Partner odabraniPartner) {
        this.odabraniPartner = odabraniPartner;
    }
    
    public boolean isPartnerOdabran() {
        return partnerOdabran;
    }
    
    public void setPartnerOdabran(boolean partnerOdabran) {
        this.partnerOdabran = partnerOdabran;
    }

    @PostConstruct
    private void provjeriPrijavuKorisnika() {
        if (this.securityContext.getCallerPrincipal() != null) {
            var korIme = this.securityContext.getCallerPrincipal().getName();
            this.korisnik = this.korisniciFacade.pretvori(this.korisniciFacade.find(korIme));
            if (this.korisnik != null) {
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

    public String odjavaKorisnika() {
        if (this.prijavljen) {
            dodajZapisOdjave();
            
            this.prijavljen = false;
            this.partnerOdabran = false;
            this.odabraniPartner = null;
            
            FacesContext facesContext = FacesContext.getCurrentInstance();
            facesContext.getExternalContext().invalidateSession();
            return "/index.xhtml?faces-redirect=true";
        }
        return "";
    }
}
package edu.unizg.foi.nwtis.mbaranasi21.vjezba_08_dz_3.jf;

import java.io.Serializable;
import edu.unizg.foi.nwtis.mbaranasi21.vjezba_08_dz_3.jpa.pomocnici.KorisniciFacade;
import edu.unizg.foi.nwtis.podaci.Korisnik;
import edu.unizg.foi.nwtis.podaci.Partner;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.security.enterprise.SecurityContext;

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
    return this.korisnik.ime();
  }

  public String getPrezime() {
    return this.korisnik.prezime();
  }

  public String getEmail() {
    return this.korisnik.email();
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
      }
    }
  }

  public String odjavaKorisnika() {
    if (this.prijavljen) {
      this.prijavljen = false;

      FacesContext facesContext = FacesContext.getCurrentInstance();
      facesContext.getExternalContext().invalidateSession();

      return "/index.xhtml?faces-redirect=true";
    }
    return "";
  }


}
package edu.unizg.foi.nwtis.mbaranasi21.vjezba_08_dz_3.jf;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import edu.unizg.foi.nwtis.podaci.Partner;
import edu.unizg.foi.nwtis.podaci.Obracun;
import edu.unizg.foi.nwtis.mbaranasi21.vjezba_08_dz_3.jpa.pomocnici.PartneriFacade;
import edu.unizg.foi.nwtis.mbaranasi21.vjezba_08_dz_3.jpa.pomocnici.ObracuniFacade;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * Backing bean za pregled obračuna
 * 
 * @author mbaranasi21
 */
@RequestScoped
@Named("obracuni")
public class Obracuni implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Inject
    private ObracuniFacade obracuniFacade;
    
    @Inject
    private PartneriFacade partneriFacade;
    
    private List<Partner> partneri;
    private List<Obracun> obracuni;
    private int odabraniPartnerId;
    private String datumOd;
    private String datumDo;
    private String poruka;
    private String porukaKlasa;
    private boolean pretrazeno = false;
    
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @PostConstruct
    public void init() {
        ucitajPartnere();
    }

    /**
     * Učitava partnere iz baze podataka
     */
    private void ucitajPartnere() {
        try {
            var partneriEntiteti = partneriFacade.findAll();
            partneri = partneriFacade.pretvori(partneriEntiteti);
        } catch (Exception e) {
            System.err.println("Greška pri dohvaćanju partnera: " + e.getMessage());
        }
    }

    /**
     * Pretražuje obračune na temelju kriterija
     */
    public String pretraziObracune() {
        try {
            if (odabraniPartnerId == 0) {
                poruka = "Morate odabrati partnera.";
                porukaKlasa = "greska";
                return null;
            }
            
            long vrijemeOd = parseDate(datumOd);
            long vrijemeDo = parseDate(datumDo);
            
            if (vrijemeOd == 0 || vrijemeDo == 0) {
                poruka = "Neispravan format datuma. Koristite yyyy-MM-dd.";
                porukaKlasa = "greska";
                return null;
            }
            
            var obracuniEntiteti = obracuniFacade.findByPartnerAndTimeRange(odabraniPartnerId, vrijemeOd, vrijemeDo);
            obracuni = obracuniFacade.pretvori(obracuniEntiteti);
            
            poruka = "Pronađeno je " + obracuni.size() + " obračuna.";
            porukaKlasa = "uspjeh";
            pretrazeno = true;
            
        } catch (Exception e) {
            poruka = "Greška pri pretraživanju obračuna: " + e.getMessage();
            porukaKlasa = "greska";
            pretrazeno = true;
        }
        return null;
    }

    private long parseDate(String datum) {
        if (datum == null || datum.trim().isEmpty()) {
            return System.currentTimeMillis();
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            return sdf.parse(datum).getTime();
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Formatira vrijeme za prikaz
     */
    public String formatirajVrijeme(long vrijeme) {
        return dateFormat.format(new Date(vrijeme));
    }

    public List<Partner> getPartneri() {
        return partneri;
    }

    public void setPartneri(List<Partner> partneri) {
        this.partneri = partneri;
    }

    public List<Obracun> getObracuni() {
        return obracuni;
    }

    public void setObracuni(List<Obracun> obracuni) {
        this.obracuni = obracuni;
    }

    public int getOdabraniPartnerId() {
        return odabraniPartnerId;
    }

    public void setOdabraniPartnerId(int odabraniPartnerId) {
        this.odabraniPartnerId = odabraniPartnerId;
    }

    public String getDatumOd() {
        return datumOd;
    }

    public void setDatumOd(String datumOd) {
        this.datumOd = datumOd;
    }

    public String getDatumDo() {
        return datumDo;
    }

    public void setDatumDo(String datumDo) {
        this.datumDo = datumDo;
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

    public boolean isPretrazeno() {
        return pretrazeno;
    }

    public void setPretrazeno(boolean pretrazeno) {
        this.pretrazeno = pretrazeno;
    }
}
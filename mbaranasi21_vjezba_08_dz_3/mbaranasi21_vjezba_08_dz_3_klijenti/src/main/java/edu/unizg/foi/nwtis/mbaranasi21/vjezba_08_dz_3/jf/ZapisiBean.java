package edu.unizg.foi.nwtis.mbaranasi21.vjezba_08_dz_3.jf;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import edu.unizg.foi.nwtis.podaci.Korisnik;
import edu.unizg.foi.nwtis.podaci.Zapis; // POJO klasa
import edu.unizg.foi.nwtis.mbaranasi21.vjezba_08_dz_3.jpa.pomocnici.KorisniciFacade;
import edu.unizg.foi.nwtis.mbaranasi21.vjezba_08_dz_3.jpa.pomocnici.ZapisiFacade;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * Backing bean za pregled zapisa rada korisnika
 * 
 * @author mbaranasi21
 */
@RequestScoped
@Named("zapisi")
public class ZapisiBean implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Inject
    private ZapisiFacade zapisiFacade;
    
    @Inject
    private KorisniciFacade korisniciFacade;
    
    private List<Korisnik> korisnici;
    private List<Zapis> zapisi;
    private String odabraniKorisnik;
    private String datumOd;
    private String datumDo;
    private String poruka;
    private String porukaKlasa;
    private boolean pretrazeno = false;
    
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @PostConstruct
    public void init() {
        ucitajKorisnike();
    }

    /**
     * Učitava korisnike iz baze podataka
     */
    private void ucitajKorisnike() {
        try {
            var korisniciEntiteti = korisniciFacade.findAll();
            korisnici = korisniciFacade.pretvori(korisniciEntiteti);
        } catch (Exception e) {
            System.err.println("Greška pri dohvaćanju korisnika: " + e.getMessage());
        }
    }

    /**
     * Pretražuje zapise na temelju kriterija
     */
    public String pretraziZapise() {
        try {
            if (odabraniKorisnik == null || odabraniKorisnik.trim().isEmpty()) {
                poruka = "Morate odabrati korisnika.";
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
            
            var zapisiEntiteti = zapisiFacade.findByUserAndTimeRange(odabraniKorisnik, vrijemeOd, vrijemeDo);
            zapisi = zapisiFacade.pretvori(zapisiEntiteti);
            
            poruka = "Pronađeno je " + zapisi.size() + " zapisa.";
            porukaKlasa = "uspjeh";
            pretrazeno = true;
            
        } catch (Exception e) {
            poruka = "Greška pri pretraživanju zapisa: " + e.getMessage();
            porukaKlasa = "greska";
            pretrazeno = true;
        }
        return null;
    }

    /**
     * Parsira datum string u millisekunde
     */
    private long parseDate(String datum) {
        if (datum == null || datum.trim().isEmpty()) {
            return System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L);
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

    // Getteri i setteri
    public List<Korisnik> getKorisnici() {
        return korisnici;
    }

    public void setKorisnici(List<Korisnik> korisnici) {
        this.korisnici = korisnici;
    }

    public List<Zapis> getZapisi() {
        return zapisi;
    }

    public void setZapisi(List<Zapis> zapisi) {
        this.zapisi = zapisi;
    }

    public String getOdabraniKorisnik() {
        return odabraniKorisnik;
    }

    public void setOdabraniKorisnik(String odabraniKorisnik) {
        this.odabraniKorisnik = odabraniKorisnik;
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
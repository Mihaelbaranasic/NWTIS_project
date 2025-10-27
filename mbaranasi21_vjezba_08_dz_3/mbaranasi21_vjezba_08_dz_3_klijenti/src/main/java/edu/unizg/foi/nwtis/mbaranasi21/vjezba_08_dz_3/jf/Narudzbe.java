package edu.unizg.foi.nwtis.mbaranasi21.vjezba_08_dz_3.jf;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import edu.unizg.foi.nwtis.podaci.Narudzba;
import edu.unizg.foi.nwtis.mbaranasi21.vjezba_08_dz_3.ServisPartnerKlijent;
import edu.unizg.foi.nwtis.mbaranasi21.vjezba_08_dz_3.GlobalniPodaci;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * Backing bean za upravljanje narudžbama
 * 
 * @author mbaranasi21
 */
@SessionScoped
@Named("narudzbe")
public class Narudzbe implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Inject
    @RestClient
    private ServisPartnerKlijent servisPartner;
    
    @Inject
    private GlobalniPodaci globalniPodaci;
    
    @Inject
    private PrijavaKorisnika prijavaKorisnika;
    
    private boolean imaAktivnuNarudzbu = false;
    private List<NarudzbaStavka> narudzbaStavke = new ArrayList<>();
    private List<NarudzbaStavka> jelaStavke = new ArrayList<>();
    private List<NarudzbaStavka> picaStavke = new ArrayList<>();
    
    private int jeloId;
    private int jeloKolicina = 1;
    private int piceId;
    private int piceKolicina = 1;
    
    private String poruka;
    private String porukaKlasa;

    /**
     * Kreira novu narudžbu
     */
    public String kreirajNovuNarudzbu() {
        try {
            String korisnickoIme = prijavaKorisnika.getKorisnickoIme();
            String lozinka = prijavaKorisnika.getLozinka();
            
            Narudzba novaNarudzba = new Narudzba(
                korisnickoIme,
                "0",
                true,
                0.0f,
                0.0f,
                System.currentTimeMillis()
            );
            
            var odgovor = servisPartner.postNarudzba(korisnickoIme, lozinka, novaNarudzba);
            int status = odgovor.getStatus();
            
            if (status == 201) {
                imaAktivnuNarudzbu = true;
                narudzbaStavke.clear();
                jelaStavke.clear();
                picaStavke.clear();
                
                if (prijavaKorisnika.getOdabraniPartner() != null) {
                    globalniPodaci.povecajBrojOtvorenihNarudzbiPartnera(
                        prijavaKorisnika.getOdabraniPartner().id());
                }
                
                poruka = "Nova narudžba je uspješno kreirana.";
                porukaKlasa = "uspjeh";
            } else {
                poruka = "Greška pri kreiranju narudžbe. Status: " + status;
                porukaKlasa = "greska";
            }
        } catch (Exception e) {
            poruka = "Greška pri komunikaciji s REST servisom: " + e.getMessage();
            porukaKlasa = "greska";
        }
        return null;
    }

    /**
     * Dodaje jelo u narudžbu
     */
    public String dodajJelo() {
        try {
            String korisnickoIme = prijavaKorisnika.getKorisnickoIme();
            String lozinka = prijavaKorisnika.getLozinka();
            
            // Kreiraj Narudzba objekt s podacima jela
            Narudzba jeloNarudzba = new Narudzba(
                String.valueOf(jeloId),
                "jelo",
                true,
                0.0f,
                (float) jeloKolicina,
                System.currentTimeMillis()
            );
            
            var odgovor = servisPartner.postJelo(korisnickoIme, lozinka, jeloNarudzba);
            int status = odgovor.getStatus();
            
            if (status == 201) {
                jelaStavke.add(new NarudzbaStavka(
                    "Jelo ID: " + jeloId, 
                    jeloKolicina, 
                    0.0
                ));
                
                poruka = "Jelo je dodano u narudžbu.";
                porukaKlasa = "uspjeh";
                jeloId = 0;
                jeloKolicina = 1;
            } else {
                poruka = "Greška pri dodavanju jela. Status: " + status;
                porukaKlasa = "greska";
            }
        } catch (Exception e) {
            poruka = "Greška pri komunikaciji s REST servisom: " + e.getMessage();
            porukaKlasa = "greska";
        }
        return null;
    }

    /**
     * Dodaje piće u narudžbu
     */
    public String dodajPice() {
        try {
            String korisnickoIme = prijavaKorisnika.getKorisnickoIme();
            String lozinka = prijavaKorisnika.getLozinka();
            
            // Kreiraj Narudzba objekt s podacima pića
            Narudzba piceNarudzba = new Narudzba(
                String.valueOf(piceId),
                "pice",
                true,
                0.0f,
                (float) piceKolicina,
                System.currentTimeMillis()
            );
            
            var odgovor = servisPartner.postPice(korisnickoIme, lozinka, piceNarudzba);
            int status = odgovor.getStatus();
            
            if (status == 201) {
                picaStavke.add(new NarudzbaStavka(
                    "Piće ID: " + piceId, 
                    piceKolicina, 
                    0.0
                ));
                
                poruka = "Piće je dodano u narudžbu.";
                porukaKlasa = "uspjeh";
                piceId = 0;
                piceKolicina = 1;
            } else {
                poruka = "Greška pri dodavanju pića. Status: " + status;
                porukaKlasa = "greska";
            }
        } catch (Exception e) {
            poruka = "Greška pri komunikaciji s REST servisom: " + e.getMessage();
            porukaKlasa = "greska";
        }
        return null;
    }

    /**
     * Plaća narudžbu
     */
    public String platiNarudzbu() {
        try {
            String korisnickoIme = prijavaKorisnika.getKorisnickoIme();
            String lozinka = prijavaKorisnika.getLozinka();
            
            var odgovor = servisPartner.postRacun(korisnickoIme, lozinka);
            int status = odgovor.getStatus();
            
            if (status == 201) {
                imaAktivnuNarudzbu = false;
                narudzbaStavke.clear();
                jelaStavke.clear();
                picaStavke.clear();
                
                if (prijavaKorisnika.getOdabraniPartner() != null) {
                    globalniPodaci.smaniBrojOtvorenihNarudzbiPartnera(
                        prijavaKorisnika.getOdabraniPartner().id());
                    globalniPodaci.povecajBrojRacunaPartnera(
                        prijavaKorisnika.getOdabraniPartner().id());
                }
                
                poruka = "Narudžba je uspješno plaćena.";
                porukaKlasa = "uspjeh";
            } else {
                poruka = "Greška pri plaćanju narudžbe. Status: " + status;
                porukaKlasa = "greska";
            }
        } catch (Exception e) {
            poruka = "Greška pri komunikaciji s REST servisom: " + e.getMessage();
            porukaKlasa = "greska";
        }
        return null;
    }

    /**
     * Osvježava pregled narudžbe
     */
    public String osvjeziNarudzbu() {
        try {
            String korisnickoIme = prijavaKorisnika.getKorisnickoIme();
            String lozinka = prijavaKorisnika.getLozinka();
            
            var odgovor = servisPartner.getNarudzba(korisnickoIme, lozinka);
            int status = odgovor.getStatus();
            
            if (status == 200) {
                poruka = "Narudžba je osvježena.";
                porukaKlasa = "uspjeh";
            } else {
                poruka = "Greška pri osvježavanju narudžbe. Status: " + status;
                porukaKlasa = "greska";
            }
        } catch (Exception e) {
            poruka = "Greška pri komunikaciji s REST servisom: " + e.getMessage();
            porukaKlasa = "greska";
        }
        return null;
    }

    // Getter i setter metode
    
    public boolean isImaAktivnuNarudzbu() {
        return imaAktivnuNarudzbu;
    }

    public void setImaAktivnuNarudzbu(boolean imaAktivnuNarudzbu) {
        this.imaAktivnuNarudzbu = imaAktivnuNarudzbu;
    }

    public List<NarudzbaStavka> getNarudzbaStavke() {
        return narudzbaStavke;
    }

    public List<NarudzbaStavka> getJelaStavke() {
        return jelaStavke;
    }

    public List<NarudzbaStavka> getPicaStavke() {
        return picaStavke;
    }

    public int getJeloId() {
        return jeloId;
    }

    public void setJeloId(int jeloId) {
        this.jeloId = jeloId;
    }

    public int getJeloKolicina() {
        return jeloKolicina;
    }

    public void setJeloKolicina(int jeloKolicina) {
        this.jeloKolicina = jeloKolicina;
    }

    public int getPiceId() {
        return piceId;
    }

    public void setPiceId(int piceId) {
        this.piceId = piceId;
    }

    public int getPiceKolicina() {
        return piceKolicina;
    }

    public void setPiceKolicina(int piceKolicina) {
        this.piceKolicina = piceKolicina;
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

    /**
     * Pomoćna klasa za prikaz stavki narudžbe u UI
     */
    public static class NarudzbaStavka {
        private String naziv;
        private int kolicina;
        private double cijena;
        private double iznos;

        public NarudzbaStavka(String naziv, int kolicina, double cijena) {
            this.naziv = naziv;
            this.kolicina = kolicina;
            this.cijena = cijena;
            this.iznos = kolicina * cijena;
        }

        public String getNaziv() { 
            return naziv; 
        }
        
        public void setNaziv(String naziv) { 
            this.naziv = naziv; 
        }
        
        public int getKolicina() { 
            return kolicina; 
        }
        
        public void setKolicina(int kolicina) { 
            this.kolicina = kolicina; 
        }
        
        public double getCijena() { 
            return cijena; 
        }
        
        public void setCijena(double cijena) { 
            this.cijena = cijena; 
        }
        
        public double getIznos() { 
            return iznos; 
        }
        
        public void setIznos(double iznos) { 
            this.iznos = iznos; 
        }
    }
}
package edu.unizg.foi.nwtis.konfiguracije;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Apstraktna klasa za rad s postavkama iz konfiguracijske datoteke Implementira dio mentoda iz
 * sučelja Konfiguracija.
 */
public abstract sealed class KonfiguracijaApstraktna implements Konfiguracija
    permits KonfiguracijaBin, KonfiguracijaJson, KonfiguracijaTxt, KonfiguracijaXml {

  /** verzija konfiguracije. */
  protected final static String verzija = "NWTiS 2024.";

  /** naziv datoteke konfiguracije. */
  protected String nazivDatoteke;

  /** kolekcija postavki. */
  protected Properties postavke;

  /**
   * Konstruktor klase.
   *
   * @param nazivDatoteke naziv datoteke
   */
  public KonfiguracijaApstraktna(String nazivDatoteke) {
    this.nazivDatoteke = nazivDatoteke;
    this.postavke = new Properties();
  }

  /**
   * Daj sve postavke.
   *
   * @return objekt klase Properties
   */
  @Override
  public Properties dajSvePostavke() {
    return this.postavke;
  }

  /**
   * Obrisi sve postavke.
   *
   * @return true, ako postoje postavke, false ako nema postavki
   */
  @Override
  public boolean obrisiSvePostavke() {
    if (this.postavke.isEmpty()) {
      return false;
    } else {
      this.postavke.clear();
      return true;
    }
  }

  /**
   * Daj postavku.
   *
   * @param kljuc ključ postavke
   * @return vrijednost vrijednost postavke
   */
  @Override
  public String dajPostavku(String kljuc) {
    return this.postavke.getProperty(kljuc);
  }

  /**
   * Daj postavku, ako nema ključa vraća osnovnu vrijednost
   *
   * @param kljuc ključ postavke
   * @param osnovnaVrijednost osnovna vrijednost
   * @return ako postoji postavka s ključem vraća njenu vrijednost, inače vraća osnovnaVrijednost
   */
  @Override
  public String dajPostavkuOsnovno(String kljuc, String osnovnaVrijednost) {
    if (postojiPostavka(kljuc)) {
      return this.postavke.getProperty(kljuc);
    } else {
      return osnovnaVrijednost;
    }
  }

  /**
   * Spremi postavku.
   *
   * @param kljuc ključ postavke
   * @param vrijednost vrijednostpostavke
   * @return true, ako ne postoji postavka, false, ako postoji postavka
   */
  @Override
  public boolean spremiPostavku(String kljuc, String vrijednost) {
    if (this.postavke.containsKey(kljuc)) {
      return false;
    } else {
      this.postavke.setProperty(kljuc, vrijednost);
      return true;
    }
  }

  /**
   * Azuriraj postavku.
   *
   * @param kljuc ključ postavke
   * @param vrijednost vrijednost postavke
   * @return true, ako postoji postavka, false, ako ne postoji postavka
   */
  @Override
  public boolean azurirajPostavku(String kljuc, String vrijednost) {
    if (!this.postavke.containsKey(kljuc)) {
      return false;
    } else {
      this.postavke.setProperty(kljuc, vrijednost);
      return true;
    }
  }

  /**
   * Postoji postavka.
   *
   * @param kljuc ključ postavke
   * @return true, ako postoji postavka s ključem, inače false
   */
  @Override
  public boolean postojiPostavka(String kljuc) {
    return postavke.containsKey(kljuc);
  }

  /**
   * Obrisi postavku.
   *
   * @param kljuc ključ postavke
   * @return true, ako postoji postavka s ključem, inače false
   */
  @Override
  public boolean obrisiPostavku(String kljuc) {
    if (!this.postavke.containsKey(kljuc)) {
      return false;
    } else {
      this.postavke.remove(kljuc);
      return true;
    }
  }

  /**
   * Spremi konfiguraciju.
   *
   * @param datoteka the datoteka
   * @throws NeispravnaKonfiguracija ako tip nije podržan ili se javi problem kod spremanja datoteke
   *         konfiguracije
   */
  public abstract void spremiKonfiguraciju(String datoteka) throws NeispravnaKonfiguracija;

  /**
   * Sprema konfiguraciju.
   *
   * @throws NeispravnaKonfiguracija ako se javi problem kod spremanja datoteke konfiguracije
   */
  public abstract void ucitajKonfiguraciju() throws NeispravnaKonfiguracija;

  /**
   * Sprema konfiguraciju pod danim nazivom datoteke.
   *
   * @throws NeispravnaKonfiguracija ako se javi problem kod spremanja datoteke konfiguracije
   */
  public void spremiKonfiguraciju() throws NeispravnaKonfiguracija {
    this.spremiKonfiguraciju(this.nazivDatoteke);
  }


  /**
   * Kreira objekt konfiguracije i sprema u datoteku pod zadanim nazivom.
   *
   * @param nazivDatoteke the naziv datoteke
   * @return objekt konfiguracije bez postavki
   * @throws NeispravnaKonfiguracija ako tip konfiguracije nije podržan ili je došlo do pogreške kod
   *         spremanja u datoteku
   */
  public static Konfiguracija kreirajKonfiguraciju(String nazivDatoteke)
      throws NeispravnaKonfiguracija {
    Konfiguracija konfig = dajKonfiguraciju(nazivDatoteke);
    konfig.spremiKonfiguraciju();
    return konfig;
  }


  /**
   * Kreira objekt konfiguracije, ako postoji datoteka postavki zadanog naziva učitava podatke inače
   * kreira datoteku pod zadanim nazivom.
   *
   * @param nazivDatoteke the naziv datoteke
   * @return objekt konfiguracije bez postavki
   * @throws NeispravnaKonfiguracija ako tip konfiguracije nije podržan ili je došlo do pogreške kod
   *         spremanja u datoteku
   */
  public static Konfiguracija preuzmiKreirajKonfiguraciju(String nazivDatoteke)
      throws NeispravnaKonfiguracija {
    if (Files.exists(Path.of(nazivDatoteke))) {
      return preuzmiKonfiguraciju(nazivDatoteke);
    } else {
      return kreirajKonfiguraciju(nazivDatoteke);
    }
  }

  /**
   * Kreira objekt konfiguraciju i u njega učitava datoteku postavki zadanog naziva.
   *
   * @param nazivDatoteke naziv datoteke
   * @return objekt konfiguracije s postavkama
   * @throws NeispravnaKonfiguracija ako tip konfiguracije nije podržan ili datoteka zadanog naziva
   *         ne postoji ili je došlo do pogreške kod čitanja datoteke
   */
  public static Konfiguracija preuzmiKonfiguraciju(String nazivDatoteke)
      throws NeispravnaKonfiguracija {
    Konfiguracija konfig = dajKonfiguraciju(nazivDatoteke);
    konfig.ucitajKonfiguraciju();
    return konfig;
  }

  /**
   * Vraća objekt konfiguracije.
   *
   * @param nazivDatoteke naziv datoteke
   * @return objekt konfiguracije
   * @throws NeispravnaKonfiguracija ako tip konfiguracije nije podržan
   */
  public static Konfiguracija dajKonfiguraciju(String nazivDatoteke)
      throws NeispravnaKonfiguracija {
    String tip = Konfiguracija.dajTipKonfiguracije(nazivDatoteke);

    return switch (tip) {
      case KonfiguracijaTxt.TIP -> new KonfiguracijaTxt(nazivDatoteke);
      case KonfiguracijaJson.TIP -> new KonfiguracijaJson(nazivDatoteke);
      case KonfiguracijaXml.TIP -> new KonfiguracijaXml(nazivDatoteke);
      case KonfiguracijaBin.TIP -> new KonfiguracijaBin(nazivDatoteke);
      default -> throw new NeispravnaKonfiguracija(
          "Datoteka: '" + nazivDatoteke + "' nema podržani tip datoteke.");
    };
  }

}


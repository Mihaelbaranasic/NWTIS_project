package edu.unizg.foi.nwtis.konfiguracije;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Klasa za konfiguraciju s tekstualnim zapisom podataka (Properties) u datoteku..
 */
public non-sealed class KonfiguracijaTxt extends KonfiguracijaApstraktna {

  /** Konstanta za tip konfiguracije. */
  public static final String TIP = "txt";

  /**
   * Instancira novi objekt klase.
   *
   * @param nazivDatoteke the naziv datoteke
   */
  public KonfiguracijaTxt(String nazivDatoteke) {
    super(nazivDatoteke);
  }

  /**
   * Spremi konfiguraciju.
   *
   * @param datotekaNaziv naziv datoteke
   * @throws NeispravnaKonfiguracija iznimka kada je neispravna konfiguracija
   */
  @Override
  public void spremiKonfiguraciju(String datotekaNaziv) throws NeispravnaKonfiguracija {
    var datoteka = Path.of(datotekaNaziv);
    var tip = Konfiguracija.dajTipKonfiguracije(datotekaNaziv);
    if (tip == null || tip.compareTo(KonfiguracijaTxt.TIP) != 0) {
      throw new NeispravnaKonfiguracija(
          "Datoteka: " + datotekaNaziv + " nema tip: " + KonfiguracijaTxt.TIP);
    } else if (Files.exists(datoteka)
        && (!Files.isRegularFile(datoteka) || !Files.isWritable(datoteka))) {
      throw new NeispravnaKonfiguracija(
          "Datoteka: " + datotekaNaziv + " nije datoteka/ne može se u nju pisati");
    }
    try {
      this.postavke.store(Files.newOutputStream(datoteka), KonfiguracijaApstraktna.verzija);
    } catch (IOException ex) {
      throw new NeispravnaKonfiguracija(
          "Problem kod spremanja u datoteku: '" + nazivDatoteke + "'.");
    }
  }

  /**
   * Učitaj konfiguraciju.
   *
   * @throws NeispravnaKonfiguracija iznimka kada je neispravna konfiguracija
   */
  @Override
  public void ucitajKonfiguraciju() throws NeispravnaKonfiguracija {
    var datoteka = Path.of(this.nazivDatoteke);
    var tip = Konfiguracija.dajTipKonfiguracije(this.nazivDatoteke);
    if (tip == null || tip.compareTo(KonfiguracijaTxt.TIP) != 0) {
      throw new NeispravnaKonfiguracija(
          "Datoteka: " + this.nazivDatoteke + " nema tip: " + KonfiguracijaTxt.TIP);
    } else if (!Files.exists(datoteka) || !Files.isRegularFile(datoteka)
        || !Files.isReadable(datoteka)) {
      throw new NeispravnaKonfiguracija(
          "Datoteka: " + this.nazivDatoteke + " nije ispravnog tipa/ne postoji/ne može se učitati");
    }
    try {
      this.postavke.load(Files.newInputStream(datoteka));
    } catch (IOException ex) {
      throw new NeispravnaKonfiguracija(
          "Problem kod učitavanja datoteke: '" + this.nazivDatoteke + "'.");
    }
  }

}
